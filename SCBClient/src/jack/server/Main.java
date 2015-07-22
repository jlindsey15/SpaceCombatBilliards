package jack.server;

import java.io.*;
import java.net.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.*;
import org.lwjgl.util.Timer;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;

public class Main {
	public static void main(String[] args) throws IOException, LWJGLException {
        //authors: Jack Lindsey and Richard Randall
        //version: 1.2
        //date: 3/10/13
		// Client side of the 3DSCBI. Accepts user keyboard and mouse input,
		// renders images, communicates with server.

		// The Client runs two threads, called MyComp and Client

		new MyComp();
		new Client();

	}
}
