package jack.server;

import java.io.Serializable;
import java.util.*;

//The entity superclass from which all the things are derived

public abstract class Entity implements Serializable {
	static final long serialVersionUID = 3L;// for server-client communications
	public static ArrayList<Entity> allTheEntities = new ArrayList<Entity>();// Contains
																				// all
																				// mobile
																				// entities
																				// of
																				// any
																				// sort

	public final static void moveEntities(double time) { // moves all the
															// entities based on
															// their positions
															// and velocities
															// and the time that
															// has elapsed since
															// the last "frame"
		for (int n = 0; n < allTheEntities.size(); n++) {
			allTheEntities.get(n).move(time);
		}
	}

	// This stuff is used to help avoid binary systems and things getting stuck
	// in walls:
	public Entity lastThingHit = null;
	public int lastWallHit = 0;
	public long lastHitTime = 0;
	// Obviously important variables to have:
	public double x;
	public double y;
	public double z;
	public double xvel;
	public double yvel;
	public double zvel;

	public double radius = 0;
	public double mass = 0;

	public void move(double time) {// Moves the entity
		x += (xvel * time);
		y += (yvel * time);
		z += (zvel * time);
	}

	public void changeVelocity(double newx, double newy, double newz) {// Modifies
																		// the
																		// velocity
																		// of
																		// the
																		// entity;
																		// called
																		// by
																		// physics
		xvel = newx;
		yvel = newy;
		zvel = newz;
	}
}
