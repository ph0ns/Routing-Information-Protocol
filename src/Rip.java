import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Rip {
	// Global path for configuration text file
	final static String CONFIG_PARTIAL_PATH = "ripconf-";
	final static String TXT_FILE_EXTENSION = ".txt";
	// Constants
	private static InetAddress localhost = getIP();
	private static List<String> connectedNetworks;
	private static RIPRouting rip = null;

	public static void main(String[] args) {

		System.out.println("Starting application...");
		if (localhost != null) {
			try {
				// Reading configuration text file for local machine
				connectedNetworks = new ArrayList<String>();
				String path = new File(CONFIG_PARTIAL_PATH + localhost.getHostAddress()+ TXT_FILE_EXTENSION).getAbsolutePath();
				ArrayList<String> lines;
				lines = readFileLineByLine(path.toString());
				for (String str : lines) {
					if(str!=null)
						connectedNetworks.add(str);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			// Where magic happens
			if (!connectedNetworks.isEmpty()) {
				try {
					rip = new RIPRouting(localhost);
					rip.addLocalRoutes(connectedNetworks);
					rip.startRouting();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}

			// Printing routing table each 5 seconds
			while (true) {
				rip.printRoutingTable();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}
	// Returns the local IP
	public static InetAddress getIP() {
		Enumeration<InetAddress> ips = null;;
		try {
			ips = NetworkInterface.getByName("eth0").getInetAddresses();
		} catch (SocketException e) {
			System.out.println("Error obteniendo la dirección ip de 'eth0'.");
			System.exit(0);
		}
		while(ips.hasMoreElements()){
			InetAddress ip = ips.nextElement();
			if(ip instanceof Inet4Address)
				return ip;
		}

		return null;
	}
	// Reads configuration file and parses network as ArrayList<String>
	public static ArrayList<String> readFileLineByLine(String path) throws IOException{
		Path p = Paths.get(path);
		return (ArrayList<String>) Files.readAllLines(p, StandardCharsets.UTF_8);
		

		}
}
