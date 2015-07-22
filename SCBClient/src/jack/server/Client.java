package jack.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

class Client implements Runnable {
	// The Client is the thread that talks with the server.
	static ArrayList<Entity> myEntities = new ArrayList<Entity>(); // all the
																	// Entities
	static Camera myCamera = new Camera(); // this player's camera
	static int playerNumber; // unique player ID number
	static Spaceship playerShip = new Spaceship();
	static Spaceship enemyShip = new Spaceship();
	final Vector3f INITIAL_UP_VECTOR = new Vector3f(0, 1, 0); // used later in
																// some vector
																// operations
	final double cameraOffsetConstant = 1.5; // how far "above" the ship is the
												// camera
	boolean send = true;
	public static boolean pleaseProceed = true; // main loop variable
	public static int delayRate = 3; // we're just initializing this at some
										// random number - doesn't really matter
										// what the number is
	public static ArrayList<GameState> gameStates = new ArrayList<GameState>();

	Thread t;

	Client() {

		// This is how threads work...
		t = new Thread(this, "Client Thread");

		t.start(); // Start the thread
	}

	// Basically like the "main" method for the thread...
	public void run() {
		Client.pleaseProceed = true;
		MyWaitNotify waiter = new MyWaitNotify(); // for interthread
													// communication
		waiter.doWait(); // wait for MyComp to accept some inputs before trying
							// to use those inputs
		int port;
		try {
			DatagramSocket clientSocket = new DatagramSocket(); // Socket to
																// communicate
																// with server

			// The following stuff determines this computer's subnet, which will
			// be used to search for others on the LAN.
			List<InetAddress> theList = Searching.getAllLocalIPs();
			String daAddress = theList.get(0).getHostAddress();
			String[] allDaParts = daAddress.split("\\.");
			String daSubnet = "";
			for (int m = 0; m < allDaParts.length - 1; m++) {
				daSubnet += (allDaParts[m] + ".");
			}
			System.out.println(daSubnet);

			InetAddress IPAddress = InetAddress.getByName(Searching
					.checkHosts(daSubnet)); // Finds a server on the LAN
											// (Hopefully)
			if (Display.isCloseRequested()) {
				System.exit(0);
			}

			byte[] sendData = new byte[1024]; // holds sent packets
			byte[] receiveData = new byte[1000000]; // holds received packets -
													// just don't want this
													// array to be too small or
													// things break...
			String sentence = "hi"; // says hi to the server to let it know that
									// this client exists
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, 9882);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			port = receivePacket.getPort();
			String received = new String(receivePacket.getData(),
					receivePacket.getOffset(), receivePacket.getLength(),
					"UTF-8"); // Receives player number from server

			Scanner receivedScanner = new Scanner(received);
			playerNumber = receivedScanner.nextInt(); // Remember your player
														// number!!!
			boolean keepGoing = true;
			while (keepGoing) { // waits until the server sends the "ready"
								// message, then starts the game
				receivePacket = new DatagramPacket(receiveData,
						receiveData.length);

				clientSocket.receive(receivePacket);
				received = new String(receivePacket.getData(),
						receivePacket.getOffset(), receivePacket.getLength(),
						"UTF-8");
				if (received.indexOf("ready") > -1) { // it's complicated, but
														// this is the easiest
														// way to parse socket
														// messages
					keepGoing = false;
				}
			}

			sendData = new byte[1024]; // 1 kb seems reasonable...

			MyWaitNotify notifier = new MyWaitNotify();
			notifier.doNotify();
			notifier.doWait();
			sentence = MyComp.message;
			// System.out.println("Sentence: " + sentence);
			MyComp.message = ""; // Clears the message so it doesn't get
									// arbitrarily long

			try {
				Thread.sleep(1); // Let's MyComp do it's thing for a little bit
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (sentence.length() != 0) {
				// If the sentence's length is zero, then we just ignore it.

			}

			clientSocket.setSoTimeout(1);

			int framesSince = 0;
			int interpolationProgress = 0;

			while (pleaseProceed) {
				// Main client loop - sends messages, receives messages, updates
				// stuff, etc.

				sendData = new byte[1024];

				notifier = new MyWaitNotify();
				notifier.doNotify();
				notifier.doWait(10); // Wait for mycomp to update message, but
										// don't wait forever
				sentence = MyComp.message;
				MyComp.message = ""; // reset the message so that the message
										// doesn't grow infinitely long over
										// time

				try {
					Thread.sleep(1); // Again, give myComp some time to do its
										// thing. Otherwise Client tends to
										// dominate.
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (sentence.length() != 0) {
					// If the length is zero it means something weird happened
					// with the multithreading,
					// so we don't send the blank message. Otherwise, sends the
					// message to server and waits for a response...
					if (send) {
						sendData = sentence.getBytes();
						sendPacket = new DatagramPacket(sendData,
								sendData.length, IPAddress, port);
						clientSocket.send(sendPacket);
						receivePacket = new DatagramPacket(receiveData,
								receiveData.length);
					}
					// Tries waiting for a response. If it times out, we do some
					// interpolation
					try {

						clientSocket.receive(receivePacket);
						port = receivePacket.getPort();
						byte[] data = receivePacket.getData(); // receives data
																// from server
						GameState gameState = new GameState();
						try {
							gameState = (GameState) Serialization
									.deserialize(data); // Assigns data from
														// server to client
														// gamestate
							gameStates.add(gameState); // Also adds this new
														// gamestate to rolling
														// gamestate history -
														// used for
														// interpolation

						} catch (ClassNotFoundException e) {
							// I don't think this can happen anymore, but it
							// can't hurt to keep the catch block
							e.printStackTrace();
						}
						myCamera = gameState.camera; // assigns client camera to
														// the camera in the
														// server message

						// same deal as with camera, but for the entities:
						myEntities.clear();
						for (Entity entity : gameState.entities) {
							myEntities.add(entity);
						}
						Billiard.spin = gameState.spin; // same deal but with
														// billiard spin

						playerShip = gameState.allDemShips.get(playerNumber); // same
																				// deal
																				// but
																				// with
																				// spaceship

						// Calculates how many frames have gone by since the
						// last server message.
						// This is used during interpolation.
						delayRate = framesSince;
						framesSince = 0;
						// resets framesSince so we can calculate a new
						// delayRate (how this works is you increment
						// framesSince every frame,
						// and then assign it to delayRate when you get your
						// next message)
						send = true; // makes it so you only send a message when
										// you've received one- ONE TO ONE
					} catch (SocketTimeoutException e) { // If it times out
															// (note: this isn't
															// like catching an
															// error. It's fine
															// if this happens);
						framesSince++; // used to find the delayRate
						send = false; // don't send a message next loop because
										// you didn't receive one
					}
					try {
						if (interpolationProgress < delayRate) { // the bigger
																	// the
																	// delayRate,
																	// the more
																	// frames of
																	// interpolation
																	// required
							// Here's the interpolation stuff. Basically, if
							// server messages are lagging behind the desired
							// FPS,
							// then interpolation kicks in. It smooths out the
							// transitions between server messages.
							// The problem is, I'm using very crude
							// interpolation techniques. So it's used as a last
							// resort.

							ArrayList<Entity> theEntities = gameStates.get(0).entities; // beginning
																						// entities
							ArrayList<Entity> theNewEntities = gameStates
									.get(1).entities; // final entities
							Camera theCamera = gameStates.get(0).camera; // beginning
																			// camera
							Camera theNewCamera = gameStates.get(1).camera; // final
																			// camera
							double theSpin = gameStates.get(0).spin; // beginning
																		// spin
							double theNewSpin = gameStates.get(1).spin;// final
																		// spin
							for (int i = 0; i < theEntities.size(); i++) {
								// Interpolates entity positions
								theEntities.get(i).x = ((double) 1 / (delayRate - interpolationProgress))
										* theNewEntities.get(i).x
										+ (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
										* theEntities.get(i).x;
								theEntities.get(i).y = ((double) 1 / (delayRate - interpolationProgress))
										* theNewEntities.get(i).y
										+ (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
										* theEntities.get(i).y;
								theEntities.get(i).z = ((double) 1 / (delayRate - interpolationProgress))
										* theNewEntities.get(i).z
										+ (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
										* theEntities.get(i).z;
								if (theEntities.get(i) instanceof Spaceship) {
									// interpolates spaceship orientations
									Spaceship thisShip = (Spaceship) (theEntities
											.get(i));
									Spaceship newShip = (Spaceship) (theNewEntities
											.get(i));
									thisShip.orientation.x = (float) (((double) 1 / (delayRate - interpolationProgress))
											* newShip.orientation.x + (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
											* thisShip.orientation.x);
									thisShip.orientation.y = (float) (((double) 1 / (delayRate - interpolationProgress))
											* newShip.orientation.y + (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
											* thisShip.orientation.y);
									thisShip.orientation.z = (float) (((double) 1 / (delayRate - interpolationProgress))
											* newShip.orientation.z + (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
											* thisShip.orientation.z);
									thisShip.orientation.w = (float) (((double) 1 / (delayRate - interpolationProgress))
											* newShip.orientation.w + (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
											* thisShip.orientation.w);
								}
							}
							// interpolates camera positions
							theCamera.x = ((double) 1 / (delayRate - interpolationProgress))
									* theNewCamera.x
									+ (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
									* theCamera.x;
							theCamera.y = ((double) 1 / (delayRate - interpolationProgress))
									* theNewCamera.y
									+ (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
									* theCamera.y;
							theCamera.z = ((double) 1 / (delayRate - interpolationProgress))
									* theNewCamera.z
									+ (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
									* theCamera.z;
							// Interpolates camera starePoints based on
							// positions of spaceships
							Vector3f upVector = Spaceship
									.vectorTimesQuaternion(INITIAL_UP_VECTOR,
											theCamera.ship.orientation);
							theCamera.setVectorStarePoint(theCamera.ship.x
									+ cameraOffsetConstant * upVector.getX(),
									theCamera.ship.y + cameraOffsetConstant
											* upVector.getY(),
									theCamera.ship.z + cameraOffsetConstant
											* upVector.getZ());
							// Interpolates billiard spin
							theSpin = ((double) 1 / (delayRate - interpolationProgress))
									* theNewSpin
									+ (1.0 - ((double) 1 / (delayRate - interpolationProgress)))
									* theSpin;
							interpolationProgress++;

						} else {
							interpolationProgress = 0;
							if (gameStates.size() > 2) { // keep the gameState
															// rolling list size
															// at around 2
								gameStates.remove(0);
							}
						}
					} catch (Exception e) { // lazy? yes. Gotta catch 'em all.
						e.printStackTrace();
					}
					if (gameStates.size() > 4) { // otherwise control response
													// would be way to delayed
						gameStates.remove(0);
					}

				}

			}
		}

		catch (IOException e) {// lots of things cause ioexceptions here...
			e.printStackTrace();
		}

	}

}
