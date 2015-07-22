package jack.server;

import java.io.*;
import java.net.*;
import java.sql.Time;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.*;
import org.lwjgl.util.Timer;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;

class MyComp implements Runnable {

	public static boolean isSpaceDown = false;

	public static String message = ""; // message to be sent to server
	public static double timeDelta;
	Thread t;
	public static boolean usingMouse = false;// whether or not you're using
												// mouse controls

	public static boolean pleaseProceed = true;// main loop variable

	MyComp() {
		// This is how threads work..
		t = new Thread(this, "My Comp Thread");
		t.start(); // Start the thread
	}

	// This is the entry point for the second thread.
	public void run() {
		MyComp.pleaseProceed = true;

		double startTime;
		double endTime;

		startTime = System.nanoTime() / 1000000.0;
		/*
		 * try { Display.setDisplayMode(new DisplayMode(800, 600));
		 * Display.create(); Keyboard.create(); Mouse.create(); } catch
		 * (LWJGLException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		MyWaitNotify notifier = new MyWaitNotify(); // For inter-thread
												// communication
		Rendering.beginGL(); // start rendering
		Rendering.Menu(Rendering.poster, Keyboard.KEY_RETURN); // welcome screen
		if (Display.isCloseRequested()) {
			System.exit(0);
		}
		notifier.doNotify();
		// Some screen size constants:
		int screenWidth = 1280;
		int screenHeight = 800;
		int centerX = screenWidth / 2;
		int centerY = screenHeight / 2;
		int zeros = 0;
		int nonzeros = 11; // Used to transition from real time to interpolation
		boolean realTime = false;// whether or not you're rendering realtime or
									// interpolated delay time
		while (MyComp.pleaseProceed) {

			// Rendering.updateGL(Client.myEntities, Client.myCamera, true);
			try {
				if (Client.delayRate > 40) { // Keeps frame rate at at least
												// around 25 fps.
					nonzeros++;
					zeros = 0;
					if (nonzeros > 10) {
						realTime = false; // start rendering with a delay if
											// consistently laggy
											// emphasis on consistently,
											// otherwise you get get some
											// jitteriness if it waffles back
											// and forth

					}
				} else {
					zeros++;
					nonzeros = 0;
					if (zeros > 10) {
						realTime = true;// start rendering real time if
										// consistently on time
						// emphasis on consistently, otherwise you get get some
						// jitteriness if it waffles back and forth

					}
				}
				if (realTime) { // renders in real time
					Rendering
							.updateGL(Client.myEntities, Client.myCamera, true);
				} else {// renders with a delay to allow for interpolation
					Rendering.updateGL(Client.gameStates.get(0).entities,
							Client.gameStates.get(0).camera, true);
				}
				// The following checks if you've won (i.e. no other ships alive
				// besides you)
				int aliveShips = 0;
				for (Spaceship ship : Client.gameStates.get(0).allDemShips) {
					if (ship.health > 0) {
						aliveShips++;
					}
				}
				if (aliveShips <= 1 && Client.playerShip.health > 0) { // You
																		// win!!!
																		// Game's
																		// over...
					Rendering.Menu(Rendering.win, Keyboard.KEY_RETURN);
					System.exit(0);
				}

			} catch (Exception e) { // just in case something horrible happens
									// like nullpointerexceptions and such,
									// render in real time
				Rendering.updateGL(Client.myEntities, Client.myCamera, true);
			}

			endTime = System.nanoTime() / 1000000.0;
			timeDelta = endTime - startTime; // calculates timeDelta
			startTime = endTime;
			MyWaitNotify waiter = new MyWaitNotify();
			waiter.doNotify();
			waiter.doWait(); // wait for client to send message before
								// continuing
			message = ""; // reset message so it doesn't keep growing

			Keyboard.poll(); // poll the Keyboard
			// The following constructs the message to be sent to the server
			message += (Client.playerNumber + " ");

			if (usingMouse) {
				message += ("yesmouse ");// yes, I'm using the mouse
				message += ((Mouse.getX() - centerX) / (double) centerX + " "
						+ (Mouse.getY() - centerY) / (double) centerY + " "); // add
																				// mouse
																				// coordinates
																				// to
																				// message

			} else {
				message += ("nomouse ");// I'm not using the mouse
			}

			// The folowing is pretty straightforward:
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				message += "up ";

			}
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				message += "down ";
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				message += "left ";

			}
			if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
				message += "right ";

			}

			if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
				message += "accelerate ";
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
				message += "decelerate ";
			}

			if ((Keyboard.isKeyDown(Keyboard.KEY_SPACE))) { // Space is fire

				isSpaceDown = true;
				message += "fire ";

			} else {
				if (Hud.ballsLeft > 0) { // Doesn't let you shoot if you are out
											// of ammo
					if (isSpaceDown) {
						Hud.ballsLeft--; // subtracts one from amo
						times.add(System.nanoTime()); // For ammo regeneration
														// system
						isSpaceDown = false;
						;

					}
					message += "nofire ";
				}
				isSpaceDown = false;

			}

			message += "setnaoitnearsinaorsientairsontoae"; // This is actually
															// necessary to
															// protect the
															// message from
															// getting its end
															// cut off

			for (int n = 0; n < times.size(); n++) { // Ammo regeneration system
				long dif = System.nanoTime() - times.get(n);
				if (dif >= (10000000000l)) {
					times.remove(0);
					Hud.ballsLeft++;
				}
			}

		}

	}

	public static ArrayList<Long> times = new ArrayList<Long>();
}
