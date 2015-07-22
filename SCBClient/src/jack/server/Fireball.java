package jack.server;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;
import org.newdawn.slick.opengl.TextureImpl;

public class Fireball extends Entity {
	// Class for cue balls. Cue balls are shot by space ships.
	static final long serialVersionUID = 4L;
	public int extra = 0;// Can be used for.... anything, really. TBD.
	public static final double INITIAL_SEPARATION_CONSTANT = 4.5; // How far cue
																	// balls
																	// start
																	// from the
																	// ship
	public Spaceship myLock; // is locked onto a ship if and only if it is shot
								// from a spaceship that is locked onto another
								// ship
								// Cue balls don't heat-seek, but they transfer
								// the lock to billiards which do.

	public Fireball(double x, double y, double z, double xvel, double yvel,
			double zvel, Spaceship lock) { // constructor
		this.x = x;
		this.y = y;
		this.z = z;
		this.xvel = xvel;
		this.yvel = yvel;
		this.zvel = zvel;
		radius = 2; // cue balls are half as big as billiards for some reason
		mass = 1; // cue balls have a mass of one
		myLock = lock;
		Entity.allTheEntities.add(this); // adds this to list of all entities

	}

	public void destroy() { // destroys the cue - called when the cue hits
							// another object
		Entity.allTheEntities.remove(this);
		// TODO: animation?
	}

	public void renderMe() {

		Sphere sphere = new Sphere();
		sphere.setOrientation(GLU.GLU_OUTSIDE);
		sphere.setTextureFlag(true);
		TextureImpl.bindNone();
		GL11.glPushMatrix();
		// System.out.println(this.spinV*90+"   "+this.spin1+"   "+this.spin2+"   "+this.spin3);
		GL11.glTranslated(x, y, z);
		sphere.draw((float) (this.radius), 30, 30);
		GL11.glPopMatrix();
	}
}
