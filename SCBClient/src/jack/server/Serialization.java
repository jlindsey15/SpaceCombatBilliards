package jack.server;

import java.io.*;

public class Serialization {
	public static Object deserialize(byte[] bytes) throws IOException,
			ClassNotFoundException {
		// deserializes a byte array from the server to a Gamestate object
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o = new ObjectInputStream(b);

		return o.readObject();

	}

}
