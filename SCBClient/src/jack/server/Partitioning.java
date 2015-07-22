package jack.server;

import java.util.*;

public class Partitioning {// Contains spatial partitioning methods used in
							// Physics

	public static int voxelSize = 4;// The default size of each cube into which
									// space is divided
	// Must be more than the radius of the largest entity

	public static ArrayList<Long> allTheCubes = new ArrayList<Long>();// Contains
																		// long-formatted
																		// indices
																		// for
																		// the
																		// voxels
	public static ArrayList<ArrayList<Entity>> allSortedEnts = new ArrayList<ArrayList<Entity>>();// Contains
																									// each
																									// voxel's
																									// list
																									// of
																									// entities
																									// contained

	public static void indexEntity(Entity entity) {// Indexes a single entity in
													// the arraylist

		// Rounds the entity's X,Y,Z
		int x, y, z;
		x = (int) Math.round(entity.x);
		y = (int) Math.round(entity.y);
		z = (int) Math.round(entity.z);

		// Calculates the voxel's coordinates for indexing based on X,Y,Z
		int modx, mody, modz;
		modx = (x - x % voxelSize) / voxelSize;
		mody = (y - y % voxelSize) / voxelSize;
		modz = (z - z % voxelSize) / voxelSize;

		// The following segment adds the entity to the voxel in which it
		// exists, but also the adjacent voxels
		// This eliminates troublesome special cases.
		for (int n = -1; n < 2; n++) {
			for (int m = -1; m < 2; m++) {
				for (int o = -1; o < 2; o++) {
					attemptIndex(modx + n, mody + m, modz + o, entity);
				}
			}
		}
	}

	public static void attemptIndex(int x, int y, int z, Entity entity) {// Adds
																			// an
																			// entity
																			// to
																			// a
																			// cetain
																			// voxel;
																			// creates
																			// voxel
																			// if
																			// needed

		// Ensures it's not a voxel below zero; the coordinate system doesn't
		// allow for that (it's outside of the box)
		if (x < 0 || y < 0 || z < 0)
			return;

		// Another convoluted way to jam 3 ints into a long for lookups (must
		// use types passed by reference)
		long index = x | (y << 21) | (z << 42);

		if (allTheCubes.contains(index)) {// If the voxel existsÉ
			allSortedEnts.get(allTheCubes.indexOf(index)).add(entity);// Add the
																		// entity
																		// to
																		// the
																		// voxel
		} else {// If it doesn't existÉ
			allTheCubes.add(index);// Create a new voxel, THEN add the entity to
									// it
			allSortedEnts.add(new ArrayList<Entity>());
			allSortedEnts.get(allTheCubes.indexOf(index)).add(entity);
		}
	}

	public static void indexAllTheEntities() {// The umbrella method which
												// indexes everything

		// Clear all the arraylists
		allTheCubes = new ArrayList<Long>();
		allSortedEnts = new ArrayList<ArrayList<Entity>>();

		// Step through entities and index
		for (int n = 0; n < Entity.allTheEntities.size(); n++) {
			indexEntity(Entity.allTheEntities.get(n));
		}
	}

}
