package jack.server;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.*;

public class Camera implements Serializable {
	// Camera class - the position and direction of the camera are obviously
	// important in rendering
	static final long serialVersionUID = 2L; // for server-client communications
	public final Vector3f v = new Vector3f(0, 0, -1);
	public double starePointX;
	public double starePointY;
	public double starePointZ;
	public Spaceship ship = new Spaceship();
	public static ArrayList<Camera> allTheCameras = new ArrayList<Camera>();

	public Camera() { // default constructor

	}

	public Camera(double x, double y, double z, double xvec, double yvec,
			double zvec, double w, Spaceship theShip) {
		this.x = x;
		this.y = y;
		this.z = z;
		orientation = new Quaternion((float) xvec, (float) yvec, (float) zvec,
				(float) w); // this isn't used in the current program, but might
							// be useful so I'll keep it around
		ship = theShip; // each camera is assigned to a ship that it follows
		Camera.allTheCameras.add(this); // adds it to list of all cameras

	}

	public void setCoordinates(double x, double y, double z) { // straightforward
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setVectorStarePoint(double x, double y, double z) { // sets the
																	// "stare point"
																	// of the
																	// camera -
																	// determines
																	// it's
																	// direction
		starePointX = x;
		starePointY = y;
		starePointZ = z;

	}

	public static final double CAMERA_DISTANCE = 10; // a constant - determines
														// how far camera is
														// from ship
	public double x;
	public double y;
	public double z;
	public Quaternion orientation = new Quaternion(1, 0, 0, 0); // again, not
																// used but
																// could be in a
																// later version

}
