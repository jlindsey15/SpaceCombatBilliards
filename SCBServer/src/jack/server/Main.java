package jack.server;

/*
 * Authors: Jack Lindsey and Richard Randal
 * Date: 3/7/13
 * Version: 1.1
 * Description: Server for 3DSCBI. Holds the authorative gamestate, sends it out to all clients periodically, and accepts client input.
 * 				On each loop it applies physics and game logic to the gamestate.
 * 
 */

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

class Main // main server loop
{
	
	public static void main(String args[]) throws Exception {
		byte[] receiveData = new byte[1024]; // holds packets received from
												// client
		byte[] sendData; // holds packets sent to clients
		DatagramSocket serverSocket = new DatagramSocket(9882); // 9882 is the
																// port the
																// program uses
		DatagramPacket receivePacket;
		int port;
		InetAddress IPAddress;
		String sentence;
		DatagramPacket sendPacket;
		Vector3f perpVector = new Vector3f(); // used later to hold the
												// perpendicular to to vectors
		final Vector3f INITIAL_UP_VECTOR = new Vector3f(0, 1, 0); // the
																	// camera's
																	// initial
																	// "up"
																	// vector
		final double cameraOffsetConstant = 1.5; // how far "above" the ship the
													// camera is
		double startTime;
		double endTime;

		startTime = System.nanoTime() / 1000000.0;

		double timeDelta = 0;
		double currentStart = 0;
		int currentPlayer = 0;
		JOptionPane pane = new JOptionPane("", JOptionPane.QUESTION_MESSAGE);
	
	    String numPlay = JOptionPane.showInputDialog(null, "How many players will join this game?");
	    int numberPlaying;
	    try {
	    	numberPlaying = Integer.parseInt(numPlay);
	    }
	    catch (Exception e) {
	    	numberPlaying = 2;
	    }
		ArrayList<InetAddress> ips = new ArrayList<InetAddress>(); // holds all
																	// the ips
																	// of the
																	// various
																	// players
		ArrayList<Integer> ports = new ArrayList<Integer>(); // same as above,
																// but for ports
		for (int i = 0; i < numberPlaying; i++) {
			// creates a new ship and camera for each player. Waits until all
			// players have joined before proceeding
			double partial = 50.0 / numberPlaying; // used to space out initial
													// spawn points
			Spaceship character = new Spaceship(i * partial + (partial / 2.0),
					i * partial + (partial / 2.0), i * partial
							+ (partial / 2.0), 0, 0, 0, 1, 1); // creates new
																// ship
			character.myLock = null;
			Camera theCamera = new Camera(0, 0, 0, 0, 1, 0, 0,
					Spaceship.allTheSpaceships.get(i)); // creates camera for
														// ship
			// makes sure the client has joined:
			receiveData = new byte[1024];
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			IPAddress = receivePacket.getAddress();
			port = receivePacket.getPort();
			String toBeSent = "yep";
			sendData = toBeSent.getBytes();

			sendPacket = new DatagramPacket(sendData, sendData.length,
					IPAddress, port);
			serverSocket.send(sendPacket);
			// *********************************************************************************************

			// tells the client its "player number" (ID assigned to each client)
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			sentence = new String(receivePacket.getData());
			IPAddress = receivePacket.getAddress();
			port = receivePacket.getPort();
			toBeSent = Integer.toString(i);
			sendData = toBeSent.getBytes();

			sendPacket = new DatagramPacket(sendData, sendData.length,
					IPAddress, port);
			serverSocket.send(sendPacket);
			ips.add(IPAddress);
			ports.add(port);

		}

		for (int i = 0; i < ports.size(); i++) {
			// Tells all the clients that the server is ready to start the game
			String notification = "ready";
			sendData = notification.getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length,
					ips.get(i), ports.get(i));
			serverSocket.send(sendPacket);

		}

		Random gen = new Random();
		for (int n = 0; n < 15; n++) {
			// Spawns billiards randomly inside the box
			Billiard temp = new Billiard(
					((gen.nextInt(2 * Physics.halfthickness - 2) - (Physics.halfthickness - 2))
							+ Physics.centerx - 1),
					((gen.nextInt(2 * Physics.halfthickness - 2) - (Physics.halfthickness - 2))
							+ Physics.centery - 1),
					((gen.nextInt(2 * Physics.halfthickness - 2) - (Physics.halfthickness - 2))
							+ Physics.centerz - 1), (3 * gen.nextGaussian()),
					(3 * gen.nextGaussian()), (3 * gen.nextGaussian()),
					(gen.nextGaussian() * 180), (gen.nextGaussian() * 180),
					(gen.nextGaussian() * 180), (gen.nextGaussian() * 180), n);

		}

		// if no messages are received for 10 seconds, the program stops:
		serverSocket.setSoTimeout(10000);

		while (true) {
			// This is the main program loop. Each time through it gets a
			// message from a client and applies physics/game logic.
			// Then it sends back a gamestate to that client.

			// Receives client packet, begins scanning it:
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			sentence = new String(receivePacket.getData());
			IPAddress = receivePacket.getAddress();
			port = receivePacket.getPort();
			Scanner scanner = new Scanner(sentence);

			try {

				currentPlayer = scanner.nextInt(); // now the server knows which
													// player it got the message
													// from

				// each pass through the loop, a timeDelta is calculated.
				// Physics is done based on how long it took between "frames"
				// so as to create smooth-looking physics
				endTime = System.nanoTime() / 1000000.0;
				timeDelta = endTime - startTime;
				startTime = endTime;
			} catch (InputMismatchException e) { // in case message gets mangled
				e.printStackTrace();
			}
			Spaceship currentShip = Spaceship.allTheSpaceships
					.get(currentPlayer); // now the server knows which ship to
											// act on
			Camera currentCamera = Camera.allTheCameras.get(currentPlayer); // ""
																			// ""
			currentShip.refreshStarePoint(); // sets the ship's "stare point"
												// based on it's movement vector
			// Applies physics. if the timeDelta is huge, don't do physics
			// because things might start to go through walls.
			if (timeDelta < 2000)
				Physics.doPhysics(timeDelta / 1000.0);
			// The camera's position is based on the ship's old positions:
			double xDif = currentShip.myHistory.get(102) - currentShip.x;
			double yDif = currentShip.myHistory.get(103) - currentShip.y;
			double zDif = currentShip.myHistory.get(104) - currentShip.z;
			double theDistance = Math.sqrt(Math.pow(xDif, 2)
					+ Math.pow(yDif, 2) + Math.pow(zDif, 2));
			// But it stays at a constant distance:
			double cameraMultFactor = Camera.CAMERA_DISTANCE / theDistance;
			double newCameraX = currentShip.x + xDif * cameraMultFactor;
			double newCameraY = currentShip.y + yDif * cameraMultFactor;
			double newCameraZ = currentShip.z + zDif * cameraMultFactor;
			Vector3f upVector = Spaceship.vectorTimesQuaternion(
					INITIAL_UP_VECTOR, currentCamera.ship.orientation);
			newCameraX += cameraOffsetConstant * upVector.getX();
			newCameraY += cameraOffsetConstant * upVector.getY();
			newCameraZ += cameraOffsetConstant * upVector.getZ();
			// And it shouldn't ever be outside the box walls:
			if (newCameraX >= (Physics.centerx + Physics.halfthickness)) {
				newCameraX = Physics.centerx + Physics.halfthickness - 0.01;
			}
			if (newCameraX <= ((Physics.centerx - Physics.halfthickness))) {
				newCameraX = Physics.centerx - Physics.halfthickness + 0.01;
			}
			if (newCameraY >= (Physics.centery + Physics.halfthickness)) {
				newCameraY = Physics.centery + Physics.halfthickness - 0.01;
			}
			if (newCameraY <= (Physics.centery - Physics.halfthickness)) {
				newCameraY = Physics.centery - Physics.halfthickness + 0.01;
			}
			if (newCameraZ >= (Physics.centerz + Physics.halfthickness)) {
				newCameraZ = Physics.centerz + Physics.halfthickness - 0.01;
			}
			if (newCameraZ <= (Physics.centerz - Physics.halfthickness)) {
				newCameraZ = Physics.centerz - Physics.halfthickness + 0.01;
			}
			currentCamera.setCoordinates(newCameraX, newCameraY, newCameraZ); // sets
																				// the
																				// new
																				// camera
																				// coordinates
			try {
				if (scanner.hasNext()) { // now we scan the rest of the client
											// message for cool tidbits
					String isUsingMouse = scanner.next();
					if (isUsingMouse.equals("nomouse")) { // if the client's not
															// using the
															// mouse...
						;
					} else { // if the client is using the mouse, then turn
								// based on mouse position
						double xTemp = 0, yTemp = 0;
						if (scanner.hasNext()) {
							xTemp = scanner.nextDouble();
						}
						if (scanner.hasNext()) {
							yTemp = scanner.nextDouble();
						}
						if (!(currentShip.justHitWall)) { // prevents turning
															// just after a wall
															// collision.
							currentShip.turn(xTemp, yTemp, timeDelta); // turns
																		// based
																		// on
																		// mouse
																		// coordinates
						}
					}
				}

				while (scanner.hasNext()) { // keep scanning through client
											// message

					String temp = scanner.next();
					if (!(currentShip.justHitWall)) { // again, prevents turning
														// after wall collision
						if (temp.equals("up")) {
							currentShip.turn(0, 0.5, timeDelta); // turns up
						}
						if (temp.equals("down")) {
							currentShip.turn(0, -0.5, timeDelta); // turns down
						}

						if (temp.equals("left")) {
							currentShip.turn(-0.5, 0, timeDelta); // turns left
						}
						if (temp.equals("right")) {
							currentShip.turn(0.5, 0, timeDelta);// turns right
						}
					}
					if (temp.equals("accelerate")) {
						// increases ship speed, changes motion vector
						// accordingly
						double multFactor = (timeDelta
								* Spaceship.ACCELERATION_CONSTANT + currentShip.speed)
								/ currentShip.speed;
						currentShip.changeVelocity(currentShip.xvel
								* multFactor, currentShip.yvel * multFactor,
								currentShip.zvel * multFactor);

					}

					if (temp.equals("decelerate")) {
						// decreases ship speed, changes motion vector
						// accordingly
						double multFactor = (-timeDelta
								* Spaceship.ACCELERATION_CONSTANT + currentShip.speed)
								/ currentShip.speed;
						currentShip.changeVelocity(currentShip.xvel
								* multFactor, currentShip.yvel * multFactor,
								currentShip.zvel * multFactor);
					}

					if (temp.equals("fire")) {
						// Interestingly, this doesn't actually fire a cue.
						// Rather, it means the user is holding the fire button.
						// So the cue gets charged up.
						if (currentShip.chargerHasStarted) {
							currentShip.currentCharger.chargeUp(timeDelta);
						}

						else {
							currentShip.chargerHasStarted = true; // begins
																	// charge
																	// sequence
																	// if it
																	// hadn't
																	// started
																	// already
						}

					}

					if (temp.equals("nofire")) {
						if (!currentShip.chargerHasStarted) { // duh
							;
						} else { // fires cue based on how charged it was, ship
									// velocity, and ship position.
							currentShip.chargerHasStarted = false;
							double multFactor = (currentShip.currentCharger.charge + currentShip.speed)
									/ currentShip.speed; // helps determine cue
															// speed
							Fireball cue = new Fireball(
									currentShip.x
											+ currentShip.xvel
											/ currentShip.speed
											* Fireball.INITIAL_SEPARATION_CONSTANT,
									currentShip.y
											+ currentShip.yvel
											/ currentShip.speed
											* Fireball.INITIAL_SEPARATION_CONSTANT,
									currentShip.z
											+ currentShip.zvel
											/ currentShip.speed
											* Fireball.INITIAL_SEPARATION_CONSTANT,
									currentShip.xvel * multFactor,
									currentShip.yvel * multFactor,
									currentShip.zvel * multFactor,
									currentShip.myLock); // creates the new cue
							currentShip.currentCharger.charge = 0;// reset the
																	// charger
																	// to 0

							// Destroys the cue immediately if it's starting
							// outside the box walls:

							if (cue.x >= (Physics.centerx
									+ Physics.halfthickness - (cue.radius))) {
								cue.destroy();
							}

							if (cue.x <= ((Physics.centerx - Physics.halfthickness) + (cue.radius))) {
								cue.destroy();
							}

							if (cue.y >= (Physics.centery
									+ Physics.halfthickness - (cue.radius))) {
								cue.destroy();
							}

							if (cue.y <= ((Physics.centery - Physics.halfthickness) + (cue.radius))) {
								cue.destroy();
							}

							if (cue.z >= (Physics.centerz
									+ Physics.halfthickness - (cue.radius))) {
								cue.destroy();
							}

							if (cue.z <= ((Physics.centerz - Physics.halfthickness) + (cue.radius))) {
								cue.destroy();

							}
						}
					}

				}
			}

			catch (InputMismatchException e) { // again, in case of mangled
												// message
				System.out.println(sentence);
				e.printStackTrace();
			}

			for (Spaceship ship : Spaceship.allTheSpaceships) {
				// All this stuff handles the automatic ship turning after it
				// has "just hit" a wall.
				if (ship.justHitWall) {
					Vector3f pointingVector = Spaceship.vectorTimesQuaternion(
							Spaceship.v, ship.orientation);
					Vector3f movingVector = new Vector3f((float) ship.xvel,
							(float) ship.yvel, (float) ship.zvel);
					pointingVector.normalise();
					movingVector.normalise();
					// turns the ship so that it's facing the way it's moving
					if (!((Math.abs(pointingVector.x - movingVector.x) < 0.05)
							&& (Math.abs(pointingVector.y - movingVector.y) < 0.05) && (Math
							.abs(pointingVector.z - movingVector.z) < 0.05))) {

						Vector3f.cross(pointingVector, movingVector, perpVector); // perpendicular
																					// vector
																					// -
																					// the
																					// axis
																					// you
																					// turn
																					// around

						try {
							perpVector.normalise();
						} catch (IllegalStateException e) { // in case you get
															// some divide by
															// zero errors

							System.out.println("difference too small");
						}

						ship.turn(perpVector, 0.05); // turns the ship around
														// the perpendicular
														// vector at a constant
														// speed
					} else { // if the ship is facing close enough to it's
								// motion direction...
						ship.justHitWall = false; // then stop this whole monkey
													// business
					}
				}
			}
			for (Spaceship ship1 : Spaceship.allTheSpaceships) {
				// All this stuff is to determine whether a ship is "locked on"
				// to another ship.
				for (Spaceship ship2 : Spaceship.allTheSpaceships) {
					if ((ship1.isAimingAtShip(ship2, Spaceship.Radius))
							&& ship1 != ship2) { // if you're pointing at
													// another ship
						ship1.myLock = ship2;

					}

					double xDistance = ship1.x - ship2.x;
					double yDistance = ship1.y - ship2.y;
					double zDistance = ship1.z - ship2.z;
					double shipToShip = Math.sqrt(Math.pow(xDistance, 2)
							+ Math.pow(yDistance, 2) + Math.pow(zDistance, 2));
					if (!(ship1.isAimingAtShip(ship2, shipToShip - 0.5))) {
						// if you were locked onto a ship, but you point far
						// enough away, then you unlock from it
						if (ship1.myLock == ship2) {
							ship1.myLock = null;
						}
					}

				}
				if (ship1.speed > 5) { // ships shouldn't be going faster than
										// this for too long
					ship1.changeVelocity(ship1.xvel * 0.995,
							ship1.yvel * 0.995, ship1.zvel * 0.995);
				}
			}
			for (Entity entity : Entity.allTheEntities) {
				if (entity instanceof Billiard) { // Goes through all billiards
													// in the world

					Billiard patient = (Billiard) entity;
					if (patient.myLock != null) {
						// If the billiard is locked onto a ship, change its
						// velocity a little bit to "heat seek" towards the ship

						Vector3f patientVelocity = new Vector3f(
								(float) patient.xvel, (float) patient.yvel,
								(float) patient.zvel);
						double patientSpeed = patientVelocity.length();
						Vector3f patientDirection = (Vector3f) patientVelocity
								.normalise();
						Vector3f desiredVelocity = new Vector3f(
								(float) (patient.myLock.x - patient.x),
								(float) (patient.myLock.y - patient.y),
								(float) (patient.myLock.z - patient.z));
						Vector3f desiredDirection = (Vector3f) desiredVelocity
								.normalise();
						double angle = Math
								.toDegrees(Math.acos((Vector3f.dot(
										patientDirection, desiredDirection) / ((patientDirection
										.length() * desiredDirection.length())))));
						if (angle < 90) { // But only heat seek if you're going
											// somewhat towards the ship.
							// otherwise, the game would require no skill
							double newXVel = patientDirection.getX()
									+ 0.1
									* (desiredDirection.getX() - patientDirection
											.getX());
							double newYVel = patientDirection.getY()
									+ 0.1
									* (desiredDirection.getY() - patientDirection
											.getY());
							double newZVel = patientDirection.getZ()
									+ 0.1
									* (desiredDirection.getZ() - patientDirection
											.getZ());
							Vector3f patientInfo = new Vector3f(
									(float) newXVel, (float) newYVel,
									(float) newZVel);
							patientDirection = (Vector3f) patientInfo
									.normalise();

							patient.changeVelocity(patientSpeed
									* patientDirection.getX(), patientSpeed
									* patientDirection.getY(), patientSpeed
									* patientDirection.getZ());
						} else { // undoes lock if the billiard isn't going
									// towards the ship, so as to achieve
									// previously mentioned easiness-reduction
									// measures
							patient.myLock = null;
						}
					}
					double speed = Math.sqrt(Math.pow(patient.xvel, 2)
							+ Math.pow(patient.yvel, 2)
							+ Math.pow(patient.zvel, 2));
					if (speed > 2.5) { // Billiards shouldn't be going too fast
										// either
						patient.changeVelocity(patient.xvel * 0.995,
								patient.yvel * 0.995, patient.zvel * 0.995);
					}
				}
			}
			currentShip.addToHistory(); // Adds the ship to the "history" - used
										// by the camera
			// sets the camera's "stare point" - basically the same as the
			// ship's, but a little bit above
			currentCamera.setVectorStarePoint(currentShip.x
					+ cameraOffsetConstant * upVector.getX(), currentShip.y
					+ cameraOffsetConstant * upVector.getY(), currentShip.z
					+ cameraOffsetConstant * upVector.getZ());

			// The following section determines entities that are visible to the
			// camera, so that you can reduce the sent packet size.
			// Right now this isn't being used in the program because I could
			// figure out how to get it to work with client-side interpolation
			// (you can check out how that works in the client program). But it
			// could be used in a later version so I'll keep it around...
			ArrayList<Entity> visibleEntities = new ArrayList<Entity>();
			for (Entity entity : Entity.allTheEntities) {
				Vector3f cameraVelocity = new Vector3f(
						(float) (currentCamera.starePointX - currentCamera.x),
						(float) (currentCamera.starePointY - currentCamera.y),
						(float) (currentCamera.starePointZ - currentCamera.z));
				Vector3f cameraDirection = (Vector3f) cameraVelocity
						.normalise();
				Vector3f objectRelativeLocation = new Vector3f(
						(float) (entity.x - currentCamera.x),
						(float) (entity.y - currentCamera.y),
						(float) (entity.z - currentCamera.z));
				Vector3f objectDirection = (Vector3f) objectRelativeLocation
						.normalise();
				double angle = Math.toDegrees(Math.acos((Vector3f.dot(
						cameraDirection, objectDirection) / ((cameraDirection
						.length() * objectDirection.length())))));
				if (angle < 50) {
					visibleEntities.add(entity);
				}

			}
			// Okay, now we're back to things that are actually being used:
			GameState gameState = new GameState(Entity.allTheEntities,
					currentCamera, Billiard.spin, Spaceship.allTheSpaceships); // creates
																				// new
																				// gamestate
																				// to
																				// send,
																				// based
																				// on
																				// current
																				// conditions

			sendData = Serialization.serialize(gameState); // serializes game
															// state into a byte
															// array to be sent

			sendPacket = new DatagramPacket(sendData, sendData.length,
					IPAddress, port);
			serverSocket.send(sendPacket);// sends gamestate TO THE CLIENT WHO
											// ASKED FOR IT

		}
	}
}