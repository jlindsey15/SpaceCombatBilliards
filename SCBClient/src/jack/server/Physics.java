package jack.server;

import java.util.*;

//Holds all the physics methods

//Holds all the physics methods
import java.util.*;

public class Physics {

	public static void doPhysics(double time) {
		// Runs all the physics simulation methods at once

		Billiard.spin += time;
		// Increments a counter used only for rendering the Billiards
		// Doesn't impact anything else

		Partitioning.indexAllTheEntities();
		// Sorts entities into which areas they are in (spatial partitioning)

		Physics.collisionResponse(time);
		// Checks entities near each other for collisions
		// Also responds to the collisions and changes velocities

		Physics.edgeResponse(time);
		// Rectifies any billiards which try to stray outside the walls of the
		// container

		Entity.moveEntities(time);
		// Change entity locations based on time and velocities
	}

	// Parameters for the size of the box containing the billiards
	public static int halfthickness = 49; // Distance from center to each wall
	public static int centerx = 50;// Center point X
	public static int centery = 50;// Center point Y
	public static int centerz = 50;// Center point Z

	public static void edgeResponse(double time) {// Checks to make sure you
													// don't pass the edges of
													// the map.
		Entity patient; // It's the patient because it gets operated on. Get it?
						// GET IT?
		for (int n = 0; n < Entity.allTheEntities.size(); n++) {// Steps through
																// all the
																// entities in
																// existence
			patient = Entity.allTheEntities.get(n);
			if (patient.x >= (centerx + halfthickness - (patient.radius))) {
				patient.lastThingHit = null;
				if ((patient.lastWallHit == 1)
						&& (System.nanoTime() - patient.lastHitTime < 50000)) {
					// Makes it so you can't keep hitting the same wall if
					// you're stuck in it
					;
				} else {
					patient.xvel *= -1;
					patient.x = centerx + halfthickness - (patient.radius)
							- 0.05;
				}
				patient.lastWallHit = 1; // So you don't get stuck in a wall
				patient.lastHitTime = System.nanoTime();
				if (patient instanceof Fireball)
					((Fireball) patient).destroy(); // cue balls die when they
													// hit walls

				if (patient instanceof Spaceship)
					((Spaceship) patient).justHitWall = true;
			}
			if (patient.x <= ((centerx - halfthickness) + (patient.radius))) {
				patient.lastThingHit = null;
				if ((patient.lastWallHit == 1)
						&& (System.nanoTime() - patient.lastHitTime < 50000)) {
					;
				} else {
					patient.xvel *= -1;
					patient.x = centerx - halfthickness + (patient.radius)
							+ 0.05;
				}
				patient.lastWallHit = 2;
				patient.lastHitTime = System.nanoTime();
				if (patient instanceof Fireball)
					((Fireball) patient).destroy();
				if (patient instanceof Spaceship)
					((Spaceship) patient).justHitWall = true;
			}
			if (patient.y >= (centery + halfthickness - (patient.radius))) {
				patient.lastThingHit = null;
				if ((patient.lastWallHit == 1)
						&& (System.nanoTime() - patient.lastHitTime < 50000)) {
					;
				} else {
					patient.yvel *= -1;
					patient.y = centery + halfthickness - (patient.radius)
							- 0.05;
				}
				patient.lastWallHit = 3;
				patient.lastHitTime = System.nanoTime();
				if (patient instanceof Fireball)
					((Fireball) patient).destroy();
				if (patient instanceof Spaceship)
					((Spaceship) patient).justHitWall = true;
			}
			if (patient.y <= ((centery - halfthickness) + (patient.radius))) {
				patient.lastThingHit = null;
				if ((patient.lastWallHit == 1)
						&& (System.nanoTime() - patient.lastHitTime < 50000)) {
					;
				} else {
					patient.yvel *= -1;
					patient.y = centery - halfthickness + (patient.radius)
							+ 0.05;
				}
				patient.lastWallHit = 4;
				patient.lastHitTime = System.nanoTime();
				if (patient instanceof Fireball)
					((Fireball) patient).destroy();
				if (patient instanceof Spaceship)
					((Spaceship) patient).justHitWall = true;
			}
			if (patient.z >= (centerz + halfthickness - (patient.radius))) {
				patient.lastThingHit = null;
				if ((patient.lastWallHit == 1)
						&& (System.nanoTime() - patient.lastHitTime < 50000)) {
					;
				} else {
					patient.zvel *= -1;
					patient.z = centerz + halfthickness - (patient.radius)
							- 0.05;
				}
				patient.lastWallHit = 5;
				patient.lastHitTime = System.nanoTime();
				if (patient instanceof Fireball)
					((Fireball) patient).destroy();
				if (patient instanceof Spaceship)
					((Spaceship) patient).justHitWall = true;
			}
			if (patient.z <= ((centerz - halfthickness) + (patient.radius))) {
				patient.lastThingHit = null;
				if ((patient.lastWallHit == 1)
						&& (System.nanoTime() - patient.lastHitTime < 50000)) {
					;
				} else {
					patient.zvel *= -1;
					patient.z = centerz - halfthickness + (patient.radius)
							+ 0.05;
				}
				patient.lastWallHit = 6;
				patient.lastHitTime = System.nanoTime();
				if (patient instanceof Fireball)
					((Fireball) patient).destroy();
				if (patient instanceof Spaceship)
					((Spaceship) patient).justHitWall = true;
			}
		}
		// Considers each possible case for each of the 6 walls
		// Also ensures that an entity doesn't hit the same wall twice, so they
		// don't get stuck in walls
	}

	public static void collisionResponse(double time) {// Checks lists of
														// entities in given
														// regions of space for
														// collisions (See
														// Partitioning.java)
		ArrayList<Long> collisions = new ArrayList<Long>();// We'll add all
															// collisions to
															// arraylist to be
															// dealt with later
															// in the method
		ArrayList<Entity> patient;// Gets operated on :P lol
		Entity thingOne, thingTwo;
		int indOne, indTwo;
		long index;
		for (int n = 0; n < Partitioning.allSortedEnts.size(); n++) {// For each
																		// non-empty
																		// section
																		// of
																		// space…
			if ((Partitioning.allSortedEnts.get(n).size()) > (1)) {// If the
																	// region
																	// has more
																	// than one
																	// entity
				patient = Partitioning.allSortedEnts.get(n);// The "patient" is
															// the region's list
															// of contained
															// entities
				for (int i = 0; i < patient.size(); i++) {// For each entity in
															// the region…
					for (int j = 0; j < patient.size(); j++) {// For each entity
																// in the region
																// which isn't
																// the first
																// one…
						if (j != i) {// Thus we get each possible unique pair of
										// entities in the region. W/o
										// partitioning we'd have O(n^2)
							thingOne = patient.get(i);
							thingTwo = patient.get(j);
							indOne = Entity.allTheEntities.indexOf(thingOne);
							indTwo = Entity.allTheEntities.indexOf(thingTwo);
							// Eliminate Duplicates
							index = ((long) (indOne)) + ((long) (indTwo) << 31);
							if (!collisions.contains(index)) {
								collisions.add(index);
								index = ((long) (indTwo))
										+ ((long) (indOne) << 31);
								collisions.add(index);
								// This is all a convoluted process to jam the
								// indices of 2 objects in an arraylist into one
								// long
								// It's necessary because you only do lookup on
								// things passed by value to the list. Hence
								// longs.
								// Just trust me… it works… don't as me why.
							}
							// Onward :D
						}
					}
				}
				// So we've got all possible pairs
			}
		}
		for (int q = 0; q < collisions.size(); q += 2) {
			// Does the actual collision checking for each pair found previously
			collide(collisions.get(q));
		}
	}

	// Method: Respond to a collision (arguments: 2 objects by reference)

	public static void collide(long index) {// Checks a pair of entities for
											// collisions; responds
		// Remember, the index holds the indices of both entities
		// Convert to indices

		// The following segment turns the long index into the original
		// entities, e1 and e2
		Entity e1, e2;
		int i1, i2;
		i1 = (int) (index >> 31);
		i2 = (int) (index - ((index >> 31) << 31));
		e1 = null;
		e2 = null;
		try {
			e1 = Entity.allTheEntities.get(i1);
			e2 = Entity.allTheEntities.get(i2);
		} catch (IndexOutOfBoundsException e) {
			;
		}
		// The following eliminates the binary system error
		try {
			if (e1.lastThingHit == e2 || e2.lastThingHit == e1) {
				return;
			}

			// The following determines coords for each entity
			double x1, y1, z1, x2, y2, z2;
			x1 = e1.x;
			x2 = e2.x;
			y1 = e1.y;
			y2 = e2.y;
			z1 = e1.z;
			z2 = e2.z;
			// The following checks for a collision
			if (Math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1)
					* (z2 - z1))) > (e1.radius + e2.radius))
				return;
			// Method terminates if they're not colliding
			//
			// The following modifies the lastThingHit records; this is the
			// other part of preventing binaries
			e1.lastThingHit = e2;
			e2.lastThingHit = e1;
			e1.lastWallHit = 0;
			e2.lastWallHit = 0;

			// Now things get disgusting….

			// The following finds a vector normal to both (same as the
			// coefficients for the tangent plane to both)
			double dx, dy, dz;
			dx = x1 - x2;
			dy = y1 - y2;
			dz = z1 - z2;// No intercept needed.

			// The following sets velocities and makes some variables for use
			// later
			double newx1, newy1, newz1, newx2, newy2, newz2;
			double xv1, xv2, yv1, yv2, zv1, zv2, m1, m2;
			xv1 = e1.xvel;
			xv2 = e2.xvel;
			yv1 = e1.yvel;
			yv2 = e2.yvel;
			zv1 = e1.zvel;
			zv2 = e2.zvel;
			m1 = e1.mass;
			m2 = e2.mass;

			// So we have xv,yv,zv 1/2, e1/e2.mass, dx,dy,dz. Set.
			// The following defines some numbers to make the equations less
			// nasty
			double k1, k2, q1, q2;
			k1 = ((xv1 * dx) + (yv1 * dy) + (zv1 * dz))
					/ (dx * dx + dy * dy + dz * dz);
			k2 = ((xv2 * dx) + (yv2 * dy) + (zv2 * dz))
					/ (dx * dx + dy * dy + dz * dz);
			q1 = ((-(m1 - m2)) + 2 * m2) / (m1 + m2);
			q2 = ((-(m2 - m1)) + 2 * m1) / (m1 + m2);

			// Finally, we define the new outgoing velocity vectors
			newx1 = ((xv1 - (k1 * dx)) + (q1 * k2 * dx));
			newx2 = ((xv2 - (k2 * dx)) + (q2 * k1 * dx));
			newy1 = ((yv1 - (k1 * dy)) + (q1 * k2 * dy));
			newy2 = ((yv2 - (k2 * dy)) + (q2 * k1 * dy));
			newz1 = ((zv1 - (k1 * dz)) + (q1 * k2 * dz));
			newz2 = ((zv2 - (k2 * dz)) + (q2 * k1 * dz));

			// DISCLAIMER: The reason the above equations work is far too
			// involved to put in any documentation
			// I have the derivation lying around someplace if you're really
			// interested. Sorry it looks so nasty, but it's a necessary evil.

			// Now, we set motion vectors (.changeVelocity)
			// Set motion vectors (.changeVelocity)
			System.out.println("Physics: " + newx1 + " " + newy1 + " " + newz1);
			e1.changeVelocity(newx1, newy1, newz1);
			e2.changeVelocity(newx2, newy2, newz2);

			if (e1 instanceof Fireball) {
				if (e2 instanceof Billiard) {
					((Billiard) e2).myLock = ((Fireball) e1).myLock; // transfers
																		// lock
																		// from
																		// cue
																		// to
																		// billiard
				}
				((Fireball) e1).destroy();
			}
			if (e2 instanceof Fireball) {
				if (e1 instanceof Billiard) {
					((Billiard) e1).myLock = ((Fireball) e2).myLock; // transfers
																		// lock
																		// from
																		// cue
																		// to
																		// billiard
				}
				((Fireball) e2).destroy();
			}

			if (e1 instanceof Billiard && e2 instanceof Spaceship) {
				((Billiard) e1).myLock = null; // when billiard hits spaceship,
												// get rid of its lock
			}

			if (e1 instanceof Billiard && e2 instanceof Spaceship) {
				((Billiard) e1).myLock = null; // when billiard hits spaceship,
												// gets rid of its lock
			}
		} catch (NullPointerException e) { // this can happen in various places
											// - it's best to just catch it
			;
		}
	}

}
