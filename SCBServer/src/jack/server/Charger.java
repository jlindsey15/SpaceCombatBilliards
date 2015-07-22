package jack.server;

import java.io.Serializable;

public class Charger implements Serializable {
	// This class is used to charge up cue balls. Each ship has a charger
	public double charge = 0;
	public final double CHARGE_CONSTANT = 0.20; // determines how fast cues
												// charge up

	public void chargeUp(double timeDelta) { // charges up the charger
		if (charge < 100) { // Max charge is 100
			charge += timeDelta * CHARGE_CONSTANT;
		}
	}

}