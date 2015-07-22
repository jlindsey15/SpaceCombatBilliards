package jack.server;

import org.newdawn.slick.opengl.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;

public class Hud {
	// Used to display the user's hud;
	public static int ballsLeft = 5; // You start with 5 ammo

	public static void renderHud() {
		int width = Display.getDisplayMode().getWidth();
		int height = Display.getDisplayMode().getHeight();

		addBallBar(
				// Shows how much ammo you have left
				width / 3 + 10, height - 75, 2 * width / 3 - 10, height - 10,
				5, ballsLeft, 5, 0, 1, 0, 0, 0, 1, Rendering.ball);

		if (Client.playerShip.health <= 0) { // If you have no health, then you
												// lose.
			Rendering.Menu(Rendering.lose, Keyboard.KEY_RETURN);
			Rendering.endGL();
			Client.pleaseProceed = false;
			MyComp.pleaseProceed = false;
			System.exit(0);

		}
		addHealthBar(
				// Add your health bar
				10, height - 75, width / 3 - 10, height - 10, 5,
				Client.playerShip.health, 5,// JACK, CHANGE THIS
				1, 0, 0, 1, 0.5, 0, Rendering.yourText);
	}

	public static void addHealthBar(
			// Adds a health bar based on your health. Too complicated and
			// hard-codey to bother commenting, but
			double corner1x, double corner1y, double corner2x, double corner2y,
			double margin, double actualValue, double maxValue,
			double backColor0, double backColor1, double backColor2,
			double barColor0, double barColor1, double barColor2, Texture title) {
		if (actualValue < 0)
			actualValue = 0;
		TextureImpl.bindNone();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor3d(backColor0, backColor1, backColor2);
		GL11.glVertex2d(corner1x, corner1y);
		GL11.glVertex2d(corner2x, corner1y);
		GL11.glVertex2d(corner2x, corner2y);
		GL11.glVertex2d(corner1x, corner2y);
		GL11.glColor3d(0, 0, 0);
		GL11.glVertex2d(corner1x + margin, corner1y + margin);
		GL11.glVertex2d(corner2x - margin, corner1y + margin);
		GL11.glVertex2d(corner2x - margin, corner2y - margin);
		GL11.glVertex2d(corner1x + margin, corner2y - margin);
		double n;
		n = ((1 - (actualValue / maxValue)) * (corner2x - (corner1x + 2 * margin)));
		GL11.glColor3d(barColor0, barColor1, barColor2);
		GL11.glVertex2d(corner1x + margin, corner1y + margin);
		GL11.glVertex2d(corner2x - (margin + n), corner1y + margin);
		GL11.glVertex2d(corner2x - (margin + n), corner2y - margin);
		GL11.glVertex2d(corner1x + margin, corner2y - margin);
		GL11.glEnd();
		if (title != null) {
			double width = corner2x - corner1x;
			GL11.glColor3d(backColor0, backColor1, backColor2);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2d(corner1x + width / 6, corner1y);
			GL11.glVertex2d(corner1x + 5 * width / 6, corner1y);
			GL11.glVertex2d(corner1x + 5 * width / 6, corner1y - 30);
			GL11.glVertex2d(corner1x + width / 6, corner1y - 30);
			GL11.glEnd();
			TextureImpl.bindNone();
			title.bind();
			double k = 5;
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2d(0, 0);
			GL11.glVertex2d(k + corner1x + width / 6, corner1y - k);
			GL11.glTexCoord2d(1, 0);
			GL11.glVertex2d(corner1x - k + 5 * width / 6, corner1y - k);
			GL11.glTexCoord2d(1, 1);
			GL11.glVertex2d(corner1x - k + 5 * width / 6, corner1y - 30 + k);
			GL11.glTexCoord2d(0, 1);
			GL11.glVertex2d(k + corner1x + width / 6, corner1y - 30 + k);
			GL11.glEnd();
		}
		GL11.glColor3d(1, 1, 1);
	}

	public static void addBallBar(
			// Adds ball bar based on ammount of ammo. Again, not worth
			// commenting.
			double corner1x, double corner1y, double corner2x, double corner2y,
			double margin, double actualValue, double maxValue,
			double backColor0, double backColor1, double backColor2,
			double barColor0, double barColor1, double barColor2, Texture ball) {
		addHealthBar(corner1x, corner1y, corner2x, corner2y, margin, 0, 0,
				backColor0, backColor1, backColor2, barColor0, barColor1,
				barColor2, null);

		double x = corner1x + margin;
		double k = (corner2x - (2 * margin + corner1x)) / maxValue;
		double r = (corner2y - (2 * margin + corner1y)) / 2;
		for (int n = 0; n < actualValue; n++) {
			TextureImpl.bindNone();
			ball.bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2d(0, 0);
			GL11.glVertex2d(x + (k / 2) - r, corner1y + margin);
			GL11.glTexCoord2d(1, 0);
			GL11.glVertex2d(x + (k / 2) - r, corner2y - margin);
			GL11.glTexCoord2d(1, 1);
			GL11.glVertex2d(x + (k / 2) + r, corner2y - margin);
			GL11.glTexCoord2d(0, 1);
			GL11.glVertex2d(x + (k / 2) + r, corner1y + margin);
			GL11.glEnd();
			x += k;

		}

	}

}
