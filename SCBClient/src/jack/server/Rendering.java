package jack.server;

//import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.*;
import org.newdawn.slick.util.ResourceLoader;
import org.newdawn.slick.opengl.TextureLoader;//Beware the "Jump To Infinity" bug :(
//import org.newdawn.slick.Color;

//import java.awt.Color;
import java.io.*;
import java.util.*;

public class Rendering {
	// Some constants:
	public static double moveCoefficient = 0.1;
	public static double spinCoefficient = 0.024;
	public static double fireCoefficient = 10;
	public static final Vector3f INITIAL_UP_VECTOR = new Vector3f(0, 1, 0);

	public static String windowTitle = "3D Space Combat Billiards: The Power Within: Inferno [BETA] Client";
	public static boolean closeRequested = false;

	public static float spinAngle, altitude = (float) (-Math.PI / 2.0), x = 1,
			y = 1, z = 1;
	public static float distance = 5;
	// Textures:
	public static Texture block1;
	public static Texture block2;
	public static Texture block3;
	public static Texture block4;

	public static double time;

	public static void beginGL() {

		createWindow();
		initGL();
		
	}

	public static void updateGL(ArrayList<Entity> allTheEntities,
			Camera camera, boolean check) { // updates the window
		if (closeRequested) { // If you exit, then the program finishes...
			cleanup();
		} else {
			int width = Display.getDisplayMode().getWidth();
			int height = Display.getDisplayMode().getHeight();
			pollInput();
			updateLogic(); // checks for exiting the program
			renderGL(width, height, allTheEntities, camera, check); // renders
																	// the list
																	// of
																	// entities
																	// with the
																	// camera
			Display.update(); // updates window
		}
	}

	public static void endGL() { // ends rendering
		closeRequested = true;
		cleanup();
	}

	private static void init3D(int width, int height) { // initializes some gl
														// stuff
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glViewport(0, 0, width, height); // Reset The Current Viewport
		GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
		GL11.glLoadIdentity(); // Reset The Projection Matrix
		GLU.gluPerspective(45.0f, ((float) width / (float) height), 0.1f,
				500.0f); // Calculate The Aspect Ratio Of The Window
		GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix
		GL11.glLoadIdentity(); // Reset The Modelview Matrix

		GL11.glEnable(GL11.GL_BLEND);
		// GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		// GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH); // Enables Smooth Shading
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
		GL11.glClearDepth(1.0f); // Depth Buffer Setup
		GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
		GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Test To Do
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST); // Really
																			// Nice
																			// Perspective
																			// Calculations

		// BAI

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	public static void init2D(int width, int height) { // initializes some more
														// gl stuff
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluOrtho2D(0, width, 0, height); // left,right,bottom,top

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	// Some textures:
	public static ArrayList<Texture> textures = new ArrayList<Texture>();
	public static ArrayList<Texture> forcefield = new ArrayList<Texture>();

	public static Texture enemyText, yourText, ball;

	public static Texture asteroid, win, lose, poster;

	private static void initGL() {
		// Initializes the textures:
		try {

			String address;
			for(int n=1;n<16;n++){//Load arraylist of ball tex's with special name format ball[number].jpg
				address = "scb/Ball"+Integer.toString(n)+".jpg";
				textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream(
					address)));
			}
			
			block2 = TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream(
					"scb/wood.jpg"));//Load the wall texture

			
			block4 = TextureLoader
					.getTexture(
							"JPG",
							ResourceLoader
									.getResourceAsStream("scb/black.jpg"));

			block3 = TextureLoader
					.getTexture(
							"JPG",
							ResourceLoader
									.getResourceAsStream("scb/red.jpg"));

			

			block1 = TextureLoader
					.getTexture(
							"JPG",
							ResourceLoader
									.getResourceAsStream("scb/borg.jpg"));


			win = TextureLoader
					.getTexture(
							"JPG",
							ResourceLoader
									.getResourceAsStream("scb/win.jpg"));

			lose = TextureLoader
					.getTexture(
							"JPG",
							ResourceLoader
									.getResourceAsStream("scb/lose.jpg"));

			poster = TextureLoader
					.getTexture(
							"JPG",
							ResourceLoader
									.getResourceAsStream("scb/poster.jpg"));
			ball = TextureLoader
					.getTexture(
							"JPG",
							ResourceLoader
									.getResourceAsStream("scb/buffalo.jpg"));

			/*
			 * enemyText = TextureLoader.getTexture("PNG",
			 * ResourceLoader.getResourceAsStream(
			 * "/Users/Richard/Desktop/CombatBilliards/EnemyHealth.png"));
			 * 
			 * yourText = TextureLoader.getTexture("PNG",
			 * ResourceLoader.getResourceAsStream(
			 * "/Users/Richard/Desktop/CombatBilliards/YourHealth.png"));
			 */

			/*
			 * for(int n = 0;n<4;n++){ address =
			 * "/Users/Richard/Desktop/CombatBilliards/FF"
			 * +Integer.toString(n)+".jpg";
			 * forcefield.add(TextureLoader.getTexture("JPG",
			 * ResourceLoader.getResourceAsStream( address))); }
			 */

		} catch (IOException e) {
		}
	}

	public static boolean isSpaceDown = false;

	private static void updateLogic() {

		// Ends program is you press X

		if (Display.isCloseRequested()) {
			closeRequested = true;
			System.out.println("done");
			System.exit(0);

		}

	}

	private static void renderGL(int width, int height,
			ArrayList<Entity> allTheEntities, Camera camera, boolean check) {
		// renders the window based on entities and camera
		Vector3f upVector = Spaceship.vectorTimesQuaternion(INITIAL_UP_VECTOR,
				camera.ship.orientation); // used ot find the "up vector" of the
											// gl camera
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();

		Rendering.init3D(width, height);
		// Turns the programs camera into LWJGL-speak
		GLU.gluLookAt((float) camera.x, (float) camera.y, (float) camera.z,
				(float) camera.starePointX, (float) camera.starePointY,
				(float) camera.starePointZ, upVector.x, upVector.y, upVector.z);

		// Renders the big box around everything
		Wall.addBox((Physics.centerx - (Physics.halfthickness + 0.5)),
				(Physics.centery - (Physics.halfthickness + 0.5)),
				(Physics.centerz - (Physics.halfthickness + 0.5)),
				(Physics.centerx + (Physics.halfthickness + 0.5)),
				(Physics.centery + (Physics.halfthickness + 0.5)),
				(Physics.centerz + (Physics.halfthickness + 0.5)),
				Rendering.block2);
		for (int n = 0; n < allTheEntities.size(); n++) {
			// Renders all the entities
			Billiard.textureNumber = n;
			allTheEntities.get(n).renderMe();
		}

		Rendering.init2D(width, height);

		if (check) {
			// Renders the Hud unless you're on a menu screen or something
			// similar
			Hud.renderHud();
		}
	}

	public static void Menu(Texture texture, int key) {

		// Renders menu screens and win/loss screens
		try {
			// Rendering stuff:
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GLU.gluOrtho2D(0, 0, 0, 0); // left,right,bottom,top

			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			while (!(Keyboard.isKeyDown(key) || Mouse.isButtonDown(0))) {
				if (Display.isCloseRequested()) {
					System.exit(0);
				}
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT
						| GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glLoadIdentity();
				Rendering.init2D(Rendering.width(), Rendering.height());
				texture.bind();
				GL11.glBegin(GL11.GL_QUADS);
				TextureImpl.bindNone();
				GL11.glTexCoord2d(0, 1);
				GL11.glVertex2d(0, 0);
				GL11.glTexCoord2d(0, 0);
				GL11.glVertex2d(0, Rendering.height());
				GL11.glTexCoord2d(1, 0);
				GL11.glVertex2d(Rendering.width(), Rendering.height());
				GL11.glTexCoord2d(1, 1);
				GL11.glVertex2d(Rendering.width(), 0);
				TextureImpl.bindNone();
				GL11.glEnd();
				Display.update();

				try {
					Thread.sleep(100);// Jack, you can get rid of this if you
										// want.
										// Richard, I'm too lazy to bother
										// getting rid of it.
				} catch (Exception e) {
				}
				
				  {
					
				

					  
						

																			// entities
																			// with the
																			// camera
				}
				
				
			}
			if (Mouse.isButtonDown(0)) {
				// If you click on the menu screen, then you are saying you want
				// to play with the mouse
				MyComp.usingMouse = true;
			}
		} catch (Exception e) {
		}
		

	} // Gotta catch 'em all

	public static void pollInput() {

		// scroll through key events
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
					// Escape exits the program
					closeRequested = true;
			}
		}

		if (Display.isCloseRequested()) {
			// as does the "X" button (I think I have this in like 3 places but
			// it can't hurt)
			System.exit(0);
			closeRequested = true;
		}
	}

	private static void createWindow() { // Creates the rendering window
		try {
			Display.setDisplayMode(new DisplayMode(1280, 800));
			Display.setVSyncEnabled(true);
			Display.setTitle(windowTitle);

			Display.create();
			Keyboard.create();
		} catch (Exception e) {
			Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
			System.exit(0);
		}
	}

	// Some obvious stuff:
	public static int width() {
		return Display.getDisplayMode().getWidth();
	}

	public static int height() {
		return Display.getDesktopDisplayMode().getHeight();
	}

	private static void cleanup() {
		Display.destroy();
	}
}