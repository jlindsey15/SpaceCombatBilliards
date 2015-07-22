package jack.server;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.lwjgl.opengl.Display;

public class Searching {
	// This class contains a bunch of static methods used to search for servers
	// on the LAN

	public static List<InetAddress> getAllLocalIPs() {
		// Finds the computer's subnet to use in lan search
		LinkedList<InetAddress> listAdr = new LinkedList<InetAddress>();
		try {
			Enumeration<NetworkInterface> nifs = NetworkInterface
					.getNetworkInterfaces();
			if (nifs == null)
				return listAdr;

			while (nifs.hasMoreElements()) {
				NetworkInterface nif = nifs.nextElement();

				Enumeration<InetAddress> adrs = nif.getInetAddresses();
				while (adrs.hasMoreElements()) {
					InetAddress adr = adrs.nextElement();
					if (adr != null
							&& !adr.isLoopbackAddress()
							&& (nif.isPointToPoint() || !adr
									.isLinkLocalAddress())) {
						listAdr.add(adr);
					}
				}
			}
			return listAdr;
		} catch (SocketException ex) // trust me here...
		{
			return listAdr;
		}
	}

	public static String checkHosts(String subnet) {
		// checks for servers on the network. Takes a while to stop if there
		// isn't one.
		JOptionPane pane = new JOptionPane("", JOptionPane.QUESTION_MESSAGE);
		
	    String numPlay = JOptionPane.showInputDialog(null, "Please specify a connection timeout speed by entering a positive integer.  \nHigher numbers increase the chance that you will connect with the server, but also increase the time it takes to do so.  \nA recommended default value is 100.");
	    int timeout;
	    
	    try {
	    	timeout = Integer.parseInt(numPlay);
	    }
	    catch (Exception e) {
	    	timeout = 100;
	    }

		for (int n = 1; n < 10; n++) { // hashtag infinite loop
			// Some string parsing stuff with subnets and such:

			String[] allDaParts = subnet.split("\\.");
			String temp = allDaParts[allDaParts.length - 1];
			int integerTemp = Integer.parseInt(temp);
			int toBeAdded;
			if (n % 2 == 0)
				toBeAdded = n / 2;
			else
				toBeAdded = -(n - 1) / 2;
			integerTemp += toBeAdded;
			temp = Integer.toString(integerTemp);
			String daRealSubnet = "";
			for (int m = 0; m < allDaParts.length - 1; m++) {
				daRealSubnet += (allDaParts[m] + ".");
			}
			daRealSubnet += temp;

			for (int i = 0; i < 254; i++) {
				if (Display.isCloseRequested()) {
					System.exit(0);
				}
				// queries potential server for response

				String host = daRealSubnet + "." + i;
				System.out.println(host);

				try {
					byte[] receiveData = new byte[1000];
					InetAddress IPAddress = InetAddress.getByName(host);
					DatagramSocket clientSocket = new DatagramSocket();
					clientSocket.setSoTimeout(timeout);
					DatagramPacket sendPacket = new DatagramPacket(
							"hi".getBytes(), 2, IPAddress, 9882);
					clientSocket.send(sendPacket);
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					clientSocket.receive(receivePacket);

					return host;
				} catch (IOException e) {

				}

			}

		}
		return "fail"; // If it doesn't find anything...
	}
}
