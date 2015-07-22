package jack.server;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.*;
import org.newdawn.slick.opengl.TextureImpl;

public class Spaceship extends Entity {
	// Class for spaceships, a type of entity.
	static final long serialVersionUID = 7L; // For client-server communication.
	public static ArrayList<Spaceship> allTheSpaceships = new ArrayList<Spaceship>(); // list
																						// of
																						// all
																						// spaceships
																						// in
																						// world.
	public ArrayList<Double> myHistory = new ArrayList<Double>(); // spaceship
																	// history -
																	// used for
																	// camera
																	// position
	public int health = 5; // start with 5 health
	public double starePointX;
	public double starePointY;
	public double starePointZ;
	public double speed;
	public static final Vector3f v = new Vector3f(0, 0, 1); // a constant vector
															// used later - read
															// on for an
															// explanation
	public static final double ACCELERATION_CONSTANT = 0.0070;// how fast you
																// accelerate
	// Spaceship orientation are stored in Quaternions. These are weird
	// math-things that I now sort of understand.
	// Basically they define a rotation of a certain about a certain vector, and
	// can be handily multiplied to compose rotations.
	public Quaternion orientation = new Quaternion(1, 0, 0, 0); // initial
																// orientation
	public static double Radius = 2; // spaceships have a radius of 2 (and yes,
										// they're spheres)
	public boolean isTurning;
	public boolean justHitWall; // for spaceship auto-turn after hitting a wall
	public Spaceship myLock = null;
	public boolean chargerHasStarted = false;
	public Charger currentCharger = new Charger();

	public Spaceship() {
		;
	}

	public Spaceship(double x, double y, double z, double xvec, double yvec,
			double zvec, double w, double speed) { // constructor
		this.x = x;
		this.y = y;
		this.z = z;
		this.speed = speed;
		orientation = new Quaternion((float) xvec, (float) yvec, (float) zvec,
				(float) w);
		Vector3f theVector = vectorTimesQuaternion(v, orientation);
		xvel = theVector.getX() * speed;
		yvel = theVector.getY() * speed;
		zvel = theVector.getZ() * speed;
		radius = Radius;
		mass = 10;
		Entity.allTheEntities.add(this); // adds to list of all entities
		for (int i = 0; i < 1000; i++) { // adds to running spaceship history
											// list so that camera can work
			myHistory.add(0.0);
		}
		Spaceship.allTheSpaceships.add(this); // adds to list of all spaceships
	}

	public static Vector3f vectorTimesQuaternion(Vector3f vector,
			Quaternion quaternion) { // multiplies a vector by a quaternion (a
										// rotation)

		float w = quaternion.getW();
		float x = quaternion.getX();
		float y = quaternion.getY();
		float z = quaternion.getZ();
		float k0 = w * w - 0.5f;
		float k1;
		float rx, ry, rz;

		// k1 = Q.V
		k1 = vector.x * x;
		k1 += vector.y * y;
		k1 += vector.z * z;

		// (qq-1/2)V+(Q.V)Q
		rx = vector.x * k0 + x * k1;
		ry = vector.y * k0 + y * k1;
		rz = vector.z * k0 + z * k1;

		// (Q.V)Q+(qq-1/2)V+q(QxV)
		rx += w * (y * vector.z - z * vector.y);
		ry += w * (z * vector.x - x * vector.z);
		rz += w * (x * vector.y - y * vector.x);

		// 2((Q.V)Q+(qq-1/2)V+q(QxV))
		rx += rx;
		ry += ry;
		rz += rz;
		return new Vector3f(rx, ry, rz); // this is the new vector once rotated
											// by the quaternion

	}

	public void turn(Vector3f theVector, double angle) {
		// turns the ship about a vector by an angle
		double s = Math.sin(angle / 2);
		float qx = (float) (theVector.getX() * s);
		float qy = (float) (theVector.getY() * s);
		float qz = (float) (theVector.getZ() * s);
		float qw = (float) (Math.cos(angle / 2));
		Quaternion localRotation1 = new Quaternion(qx, qy, qz, qw);
		Quaternion.normalise(localRotation1, localRotation1);
		Quaternion.mul(localRotation1, orientation, orientation); // multiplying
																	// two
																	// rotations
																	// gives
																	// their
																	// composition
																	// in
																	// Quaternion-world
	}

	public void turn(double cursorX, double cursorY, double time) {
		// turn is an overloaded method... this turns the ship given a cursor
		// position and a time Delta
		// The arrow keys are interpreted as constant cursor positions
		isTurning = true; // duh...

		Quaternion localRotation1 = new Quaternion(0,
				1 * (float) (Math.sin(cursorX * time * TURN_SPEED_CONSTANT)),
				0,
				-(float) (Math
						.cos((float) (cursorX * time * TURN_SPEED_CONSTANT))));
		Quaternion localRotation2 = new Quaternion(
				1 * (float) (Math.sin(cursorY * time * TURN_SPEED_CONSTANT)),
				0, 0,
				-(float) (Math
						.cos((float) (cursorY * time * TURN_SPEED_CONSTANT))));
		Quaternion.mul(orientation, localRotation1, orientation); // turns the
																	// ship
																	// left/right
		Quaternion.mul(orientation, localRotation2, orientation); // turns the
																	// ship
																	// up/down

		Vector3f theVector = vectorTimesQuaternion(v, orientation); // quaternions
																	// are
																	// rotations,
																	// not
																	// orientations.
																	// So you
																	// must
																	// multiply
																	// them
																	// by a
																	// starting
																	// vector to
																	// get an
																	// orientation.

		xvel = theVector.getX() * speed; // multiply unitvector by speed to get
											// velocities...
		yvel = theVector.getY() * speed;
		zvel = theVector.getZ() * speed;

	}

	public void refreshStarePoint() {
		// sets the ship's "stare point" based on its position and velocity -
		// this determines the camera's stare point
		// and also the direction that cues are fired
		speed = (Math.sqrt(Math.pow(xvel, 2) + Math.pow(yvel, 2)
				+ Math.pow(zvel, 2)));
		double time = STARE_POINT_DISTANCE / speed;
		starePointX = x + (time * xvel);
		starePointY = y + (time * yvel);
		starePointZ = z + (time * zvel);

	}

	public void destroy() { // destroys the spaceship
		Entity.allTheEntities.remove(this);
		// TODO: animation?
	}

	public void hurt(int x) { // hurts the spaceship by reducing its healt
		if (x > 0.5)
			health -= x;
		if (health <= 0)
			radius = 0;
	}

	public void addToHistory() {
		// adds spaceship to the history. Kind of a weird system here: multiples
		// of three are x coordinates, 1 mod 3 is y, and 2 mod 3 is zero.
		myHistory.add(0, this.z);
		myHistory.add(0, this.y);
		myHistory.add(0, this.x);
		myHistory.remove(1000);
		myHistory.remove(1000);
		myHistory.remove(1000);

	}

	public boolean isAimingAtShip(Spaceship otherShip, double r) {
		// determines whether ship is aiming at another object (a ship) with
		// given radius
		// Uses formulas for lines and spheres and tries to find an intersection
		double distance = Math.sqrt(Math.pow(this.x - otherShip.x, 2)
				+ Math.pow(this.y - otherShip.y, 2)
				+ Math.pow(this.z - otherShip.z, 2));
		double starePointDistance = Math.sqrt(Math.pow(this.starePointX
				- otherShip.x, 2)
				+ Math.pow(this.starePointY - otherShip.y, 2)
				+ Math.pow(this.starePointZ - otherShip.z, 2));
		if (starePointDistance > distance) { // not aiming at ship if aiming
												// away from ship!!! Duh!!!
			return false;
		}
		Vector3f Direction = new Vector3f((float) (starePointX - x),
				(float) (starePointY - y), (float) (starePointZ - z));
		Vector3f unitDirection = (Vector3f) Direction.normalise();
		Vector3f centerDifference = new Vector3f((float) (otherShip.x - x),
				(float) (otherShip.y - y), (float) (otherShip.z - z));
		double discriminant = Math.pow(
				(Vector3f.dot(unitDirection, centerDifference)), 2)
				- Vector3f.dot(centerDifference, centerDifference)
				+ Math.pow(r, 2);
		// if discriminant is positive or zero they intersect. Otherwise no
		// dice.

		return (discriminant >= 0);

	}

	public void renderMe() {
		if (this.health <= 0) {
			;
		} else {
			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);
			double angle = 2 * 180 / Math.PI
					* Math.acos(this.orientation.getW());
			double xVector = this.orientation.getX()
					/ Math.sqrt(1 - this.orientation.getW()
							* this.orientation.getW());
			double yVector = this.orientation.getY()
					/ Math.sqrt(1 - this.orientation.getW()
							* this.orientation.getW());
			double zVector = this.orientation.getZ()
					/ Math.sqrt(1 - this.orientation.getW()
							* this.orientation.getW());
			GL11.glRotatef((float) angle, (float) xVector, (float) yVector,
					(float) zVector);
			System.out.println("ORIENTATION!!!: " + this.orientation.getW()
					+ "  " + this.orientation.getX() + "  "
					+ this.orientation.getY() + "  " + this.orientation.getZ());
			TextureImpl.bindNone();
			Rendering.block1.bind();
			GL11.glBegin(GL11.GL_TRIANGLES);

			GL11.glTexCoord3d(1, 1, 1);
			GL11.glVertex3d(0, 0, 2);
			GL11.glTexCoord3d(0, 1, 1);
			GL11.glVertex3d(0, 1, -1);
			GL11.glTexCoord3d(1, 0, 1);
			GL11.glVertex3d(1, 0, -1);

			GL11.glTexCoord3d(1, 1, 1);
			GL11.glVertex3d(0, 0, 2);
			GL11.glTexCoord3d(0, 1, 1);
			GL11.glVertex3d(0, 1, -1);
			GL11.glTexCoord3d(1, 0, 1);
			GL11.glVertex3d(-1, 0, -1);

			GL11.glTexCoord3d(1, 1, 1);
			GL11.glVertex3d(0, 0, 2);
			GL11.glTexCoord3d(0, 1, 1);
			GL11.glVertex3d(0, -1, -1);
			GL11.glTexCoord3d(1, 0, 1);
			GL11.glVertex3d(1, 0, -1);

			GL11.glTexCoord3d(1, 1, 1);
			GL11.glVertex3d(0, 0, 2);
			GL11.glTexCoord3d(0, 1, 1);
			GL11.glVertex3d(0, 1, -1);
			GL11.glTexCoord3d(1, 0, 1);
			GL11.glVertex3d(-1, 0, -1);

			GL11.glEnd();
			GL11.glPopMatrix();

			double xDifference = this.starePointX - this.x;
			double yDifference = this.starePointY - this.y;
			double zDifference = this.starePointZ - this.z;
			xVector = this.orientation.getX()
					/ Math.sqrt(1 - this.orientation.getW()
							* this.orientation.getW());
			yVector = this.orientation.getY()
					/ Math.sqrt(1 - this.orientation.getW()
							* this.orientation.getW());
			zVector = this.orientation.getZ()
					/ Math.sqrt(1 - this.orientation.getW()
							* this.orientation.getW());
			for (int i = 1; i < 10; i++) {
				double xPoint = this.x + i * xDifference;
				double yPoint = this.y + i * yDifference;
				double zPoint = this.z + i * zDifference;
				GL11.glPushMatrix();
				GL11.glTranslated(xPoint, yPoint, zPoint);
				angle = 2 * 180 / Math.PI * Math.acos(this.orientation.getW());

				GL11.glRotatef((float) angle, (float) xVector, (float) yVector,
						(float) zVector);

				TextureImpl.bindNone();
				// Color.red.bind();
				if (this.myLock == null) {
					Wall.addBox(-0.5, -0.5, -0.5, 0, 0, -0.3, Rendering.block4);
				} else {
					Wall.addBox(-0.5, -0.5, -0.5, 0, 0, -0.3, Rendering.block3);
				}
				GL11.glPopMatrix();
			}
		}
	}

	public static final double TURN_SPEED_CONSTANT = 0.0010; // how fast ship
																// can turn
	public static final double STARE_POINT_DISTANCE = 5.0;// how far the stare
															// point is from the
															// ship

}
