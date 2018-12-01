import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

// This class implements the full-duplex communication with two simply threads:
// Receiver parses Bytes to RoutingTables and check for the best route
// Sender parses 
public class RIPRouting {
	// Constants
	public static final int DEFAULT_PORT = 8129;
	public static final short ENTRY_SIZE = 20;
	public static final short DATAGRAM_SIZE = 504;
	// Parameters
	private InetAddress localhost= getIP();
	private RoutingTable r_routingTable;
	private static List<String> connectedNetworks;
	// Constructor
	public RIPRouting(InetAddress localhost) {
		this.localhost = localhost;
		r_routingTable = new RoutingTable();
	}
	// Adds connected nodes (Flag G=0)
	public void addLocalRoutes(List<String> nets) throws UnknownHostException {
		RIPRouting.connectedNetworks = nets;
		// Adding local ip node to directly connected routes
		RoutingEntry entry = new RoutingEntry(localhost, InetAddress.getByName("255.255.255.255"), null, 0);
		r_routingTable.addEntry(entry);

		// Adding directly connected routes
		for (Iterator<String> iterator = nets.iterator(); iterator.hasNext();) {
			String addr = (String) iterator.next();
			
			if (addr.contains("/")) {
				String[] subnet = addr.split("/");
				entry = new RoutingEntry(InetAddress.getByName(subnet[0]), getMascara(Integer.parseInt(subnet[1])), null, 1);
			} else {
				entry = new RoutingEntry(InetAddress.getByName(addr), InetAddress.getByName("255.255.255.255"), null, 1);
			}
			r_routingTable.addEntry(entry);
		}
	}
	
	private static InetAddress getMascara(int masc) throws UnknownHostException {
		InetAddress mascara = null;
		switch(masc){
		case 0:
			mascara = InetAddress.getByName("000.000.000.000");
			break;
		case 1:
			mascara = InetAddress.getByName("128.000.000.000");
			break;
		case 2:
			mascara = InetAddress.getByName("192.000.000.000");
			break;
		case 3:
			mascara = InetAddress.getByName("224.000.000.000");
			break;
		case 4:
			mascara = InetAddress.getByName("240.000.000.000");
			break;
		case 5:
			mascara = InetAddress.getByName("248.000.000.000");
			break;
		case 6:
			mascara = InetAddress.getByName("252.000.000.000");
			break;
		case 7:
			mascara = InetAddress.getByName("254.000.000.000");
			break;
		case 8:
			mascara = InetAddress.getByName("255.000.000.000");
			break;
		case 9:
			mascara = InetAddress.getByName("255.128.000.000");
			break;
		case 10:
			mascara = InetAddress.getByName("255.192.000.000");
			break;
		case 11:
			mascara = InetAddress.getByName("255.224.000.000");
			break;
		case 12:
			mascara = InetAddress.getByName("255.240.000.000");
			break;
		case 13:
			mascara = InetAddress.getByName("255.248.000.000");
			break;
		case 14:
			mascara = InetAddress.getByName("255.252.000.000");
			break;
		case 15:
			mascara = InetAddress.getByName("255.254.000.000");
			break;
		case 16:
			mascara = InetAddress.getByName("255.255.000.000");
			break;
		case 17:
			mascara = InetAddress.getByName("255.255.128.000");
			break;
		case 18:
			mascara = InetAddress.getByName("255.255.192.000");
			break;
		case 19:
			mascara = InetAddress.getByName("255.255.224.000");
			break;
		case 20:
			mascara = InetAddress.getByName("255.255.240.000");
			break;
		case 21:
			mascara = InetAddress.getByName("255.255.248.000");
			break;
		case 22:
			mascara = InetAddress.getByName("255.255.252.000");
			break;
		case 23:
			mascara = InetAddress.getByName("255.255.254.000");
			break;
		case 24:
			mascara = InetAddress.getByName("255.255.255.000");
			break;
		case 25:
			mascara = InetAddress.getByName("255.255.255.128");
			break;
		case 26:
			mascara = InetAddress.getByName("255.255.255.192");
			break;
		case 27:
			mascara = InetAddress.getByName("255.255.255.224");
			break;	
		case 28:
			mascara = InetAddress.getByName("255.255.255.240");
			break;
		case 29:
			mascara = InetAddress.getByName("255.255.255.248");
			break;
		case 30:
			mascara = InetAddress.getByName("255.255.255.252");
			break;
		case 31:
			mascara = InetAddress.getByName("255.255.255.254");
			break;
		case 32:
			mascara = InetAddress.getByName("255.255.255.255");
			break;
				}

		return mascara;
	}

	// Starts receiver and sender threads
	public void startRouting() {
		this.startSender();
		this.startReceiver();
	}

	// Implements 10 seconds periodic sender thread
	public void startSender() {
		(new Thread() {
			@Override
			public void run() {
				System.out.println("Starting client...");
				try {
					DatagramSocket s = new DatagramSocket();
					while (true) {
						sendUpdates(s);
						Thread.sleep(10000+(int)(Math.random()*2000));
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 

			}
		}).start();
	}
	// Sends our table data to connectedNetworks excepting subnets and our IP direction
	private void sendUpdates(DatagramSocket s) throws IOException {
		String check;
		for (Iterator<String> iterator = connectedNetworks.iterator(); iterator.hasNext();) {
			check =  iterator.next();
			if(!check.contains("/") && check!=localhost.getHostAddress()){
				InetAddress net = InetAddress.getByName(check);
				s.connect(net, DEFAULT_PORT);
				if (s.isConnected()) {
					byte[] data = r_routingTable.toByteArray(r_routingTable, net); //pasar net y comprobar split horizon
					DatagramPacket packet = new DatagramPacket(data, data.length, net, DEFAULT_PORT);
					try{
						s.send(packet);
					} catch(IOException e){
						e.printStackTrace();
					}
					System.out.println("Sending packet to "+ packet.getAddress().getHostAddress() + " ...");
				}
			}
		}
	}
	// Implements the receiver UDP socket running with a while(true) loop without temporizers
	public void startReceiver() {
		(new Thread() {
			@Override
			public void run() {
				DatagramSocket s = null;
				System.out.println("Starting server...");
				try {
				s = new DatagramSocket(DEFAULT_PORT, localhost);
				while (true) {					
						byte[] buf = new byte[DATAGRAM_SIZE];
						DatagramPacket dp = new DatagramPacket(buf, buf.length);
						s.receive(dp);
						RoutingTable table = RoutingTable.toObject(dp.getData());
						checkRoutingTable(table, dp.getAddress());
						//System.out.println(dp.getAddress());
				}
					} catch (IOException e) {
						e.printStackTrace();
					}
					s.close();
				}
		}).start();
	}
	// Checks a received table implementing an updater to local table
	public void checkRoutingTable(RoutingTable table, InetAddress source) {
		ArrayList<RoutingEntry> entries = table.getAllEntries();
		for (Iterator<RoutingEntry> iterator = entries.iterator(); iterator.hasNext();) {
			RoutingEntry entry = (RoutingEntry) iterator.next();
			RoutingEntry localEntry = this.r_routingTable.getForIP(entry.getIp().getHostAddress(), entry.getMascara().getHostAddress());
			//If new way exists and if it has less metric
			if (localEntry != null) {
				if (localEntry.getMetric() > (entry.getMetric()+1)) {
					this.r_routingTable.deleteEntry(localEntry);
					this.r_routingTable.addEntry(new RoutingEntry(entry.getIp(), entry.getMascara(), source, 1 + entry.getMetric()));
			//Triggered updates looks like RIP using a random temporizer
					try {
						DatagramSocket s;
						s = new DatagramSocket();
						s.setSoTimeout(1000 + (int)(Math.random()*4000)); //timeout 1-5 seconds
						sendUpdates(s);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else { // Then put a new entry in table
				this.r_routingTable.addEntry(new RoutingEntry(entry.getIp(), entry.getMascara(), source, 1 + entry.getMetric()));
				try {
					DatagramSocket s;
					s = new DatagramSocket();
					s.setSoTimeout(1000 + (int)(Math.random()*4000)); //timeout 1-5 seconds
					sendUpdates(s);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
	// Prints routing table
	public void printRoutingTable() {
		System.out.println("\n\n\nRouting table for "
				+ this.localhost.getHostAddress() + " : ("
				+ r_routingTable.getNumEntries() + " entries)");
		for (int i = 0; i < r_routingTable.getNumEntries(); i++) {
			RoutingEntry rip_entry = (RoutingEntry) r_routingTable.getEntry(i);
			if (rip_entry.getNextHop() != null) {
				System.out.println(rip_entry.getIp()+" mask: "+rip_entry.getMascara().getHostAddress() + " ["
						+ rip_entry.getMetric() + "] via "
						+ rip_entry.getNextHop().getHostAddress());
			} else {
				System.out.println(rip_entry.getIp()+" mask: "+rip_entry.getMascara().getHostAddress() +" [" +rip_entry.getMetric()+"]");
			}
		}
	}
	// Obtains all reachable networks
	public static List<String> getConnectedNetworks() {
		return connectedNetworks;
	}
	public static InetAddress getIP() {
		Enumeration<InetAddress> ips = null;
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
}

