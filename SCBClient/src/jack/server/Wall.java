package jack.server;

import org.lwjgl.opengl.GL11;
import java.util.*;
import org.newdawn.slick.opengl.*;
//import org.newdawn.slick.util.ResourceLoader;
import org.newdawn.slick.Color;
//import org.newdawn.slick.opengl.TextureLoader;

//Not only has a wall object, but also an arraylist of them and collision detection methods :)
public class Wall { // Used to render the walls...

	public static ArrayList<Wall> allWalls = new ArrayList<Wall>();

	public double corner1x;
	public double corner1y;
	public double corner1z;
	public double corner2x;
	public double corner2y;
	public double corner2z;
	public double corner3x;
	public double corner3y;
	public double corner3z;
	public double corner4x;
	public double corner4y;
	public double corner4z;

	public Wall(
			// wall constructor
			double a, double b, double c, double d, double e, double f,
			double g, double h, double i, double j, double k, double l) {
		corner1x = a;
		corner1y = b;
		corner1z = c;
		corner2x = d;
		corner2y = e;
		corner2z = f;
		corner3x = g;
		corner3y = h;
		corner3z = i;
		corner4x = j;
		corner4y = k;
		corner4z = l;
	}

	public static void addWall(double a, double b, double c, double d,
			double e, double f, double g, double h, double i, double j,
			double k, double l, Texture tex) {
		TextureImpl.bindNone(); // texture rendering stuff

		tex.bind();

		// Creates wall in the proper position given the parameters:
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex3f((float) a, (float) b, (float) c);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex3f((float) d, (float) e, (float) f);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex3f((float) g, (float) h, (float) i);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex3f((float) j, (float) k, (float) l);
		GL11.glEnd();
		allWalls.add(new Wall(a, b, c, d, e, f, g, h, i, j, k, l));
	}

	public static void addBox(double x1, double y1, double z1, double x2,
			double y2, double z2, Texture tex) { // a box consists of 6 walls...
		addWall(x1, y1, z1, x1, y2, z1, x1, y2, z2, x1, y1, z2, tex);// X1 stays
																		// the
																		// same
		addWall(x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, tex);// X2 stays
																		// the
																		// same
		addWall(x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, tex);// Y1 stays
																		// the
																		// same
		addWall(x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, tex);// Y2 stays
																		// the
																		// same
		addWall(x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, tex);// Z1 stays
																		// the
																		// same
		addWall(x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, tex);// Z2 stays
																		// the
																		// same
	}
}
