package jack.server;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.*;

public class Billiard extends Entity {
	// Class for all billiard balls (excluding cue balls). A type of Entity.
	static final long serialVersionUID = 1L; // Important for server-client
												// communication

	public int color = 0;// Used in renderMe

	public double spin1, spin2, spin3, spinV; // To make billiards spin

	public static double spinConstant = 0.5;

	public static double spin;
	public Spaceship myLock = null; // Billiard starts out not locked on to
									// anyone.
									// Can become locked on if hit by a
									// locked-on cue ball

	public Billiard(double x, double y, double z, double xvel, double yvel,
			double zvel, double spin1, double spin2, double spin3,
			double spinV, int color) { // constructor
		this.x = x;
		this.y = y;
		this.z = z;
		this.xvel = xvel;
		this.yvel = yvel;
		this.zvel = zvel;
		this.spin1 = spin1;
		this.spin2 = spin2;
		this.spin3 = spin3;
		this.spinV = spinV;
		radius = 4; // Billiards have a radius of 4
		mass = 1; // and a mass of 1
		Entity.allTheEntities.add(this); // add to list of all entities
	}

	public static int textureNumber = 0; // for texture rendering

}