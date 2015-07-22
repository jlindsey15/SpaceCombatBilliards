package jack.server;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable{
	static final long serialVersionUID = 7983846558486202773L;
	ArrayList<Entity> entities;
	ArrayList<Spaceship> allDemShips;
	Camera camera;
	double spin;
	GameState(ArrayList<Entity> allTheEntities, Camera theCamera, double spinCounter, ArrayList<Spaceship> ships) {
		entities = allTheEntities;
		camera = theCamera;
		spin = spinCounter;
		allDemShips = ships;
	}
	GameState() {
		;
	}
	

}
