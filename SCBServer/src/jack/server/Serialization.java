package jack.server;

import java.io.*;
import java.util.ArrayList;

public class Serialization {
	// Contains only one static method

	public static byte[] serialize(Object obj) throws IOException {
		// serializes a Gamestate object to a byte array to be sent from server
		// to client
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(obj);
		return b.toByteArray();
	}

}
