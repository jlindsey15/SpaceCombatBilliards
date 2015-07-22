package jack.server;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
	// A gamestate contains all the info about the game at a snapshot in time.
	// This is what gets sent from the server to the client
	static final long serialVersionUID = 7983846558486202773L; // for
																// server-client
																// communications
	ArrayList<Entity> entities;
	ArrayList<Spaceship> allDemShips;
	Camera camera;
	double spin;

	GameState(ArrayList<Entity> allTheEntities, Camera theCamera,
			double spinCounter, ArrayList<Spaceship> ships) {
		// The gamestate contains the list of entities, the player's camera, a
		// thing that gets billiards to spin, and a redundant list of ships
		// Yeah, the redundancy isn't efficient, but it's not inefficient enough
		// to matter

		entities = allTheEntities;
		camera = theCamera;
		spin = spinCounter;
		allDemShips = ships;
	}

	GameState() { // default constructor
		;
	}

}
