
import java.net.InetAddress;

public class RoutingEntry implements java.io.Serializable {
	// Constants
	private static final long serialVersionUID = 704757447742120126L;
	public static final int INFINITE_METRIC = 16;
	// Parameters
	private InetAddress ip = null;
	private InetAddress mascara = null;
	private InetAddress nextHop = null;
	private int metric = 0;  //If metric =0, it means flag G=0
	// Constructor
	public RoutingEntry(InetAddress ip, InetAddress masc, InetAddress next, int n) {
		this.ip = ip;
		this.mascara = masc;
		this.nextHop = next;
		this.metric = n;
	}
	// Gets ip as a String
	public InetAddress getIp() {
		return ip;
	}
	
	public InetAddress getMascara() {
		return mascara;
	}
	// Gets next hop as an InetAddress
	public InetAddress getNextHop() {
		return nextHop;
	}
	// Gets metric
	public int getMetric() {
		return metric;
	}

}
