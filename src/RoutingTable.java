import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class RoutingTable implements java.io.Serializable {

	private static final long serialVersionUID = -8759553627694971868L;
	public static final int INFINITE_METRIC = 16;

	// Initialize EntryTable
	private ArrayList<RoutingEntry> m_Rtable = null;
	// Empty constructor
	public RoutingTable() {
		this.m_Rtable = new ArrayList<RoutingEntry>();
	}
	// Gets a RoutingEntry
	public synchronized RoutingEntry getEntry(int paramInt) {
		return (RoutingEntry) this.m_Rtable.get(paramInt);
	}
	// Adds RoutingEntry to table
	public synchronized void addEntry(RoutingEntry paramRoutingEntry) {
		this.m_Rtable.add(paramRoutingEntry);
	}
	// Gets length of m_Rtable=ArrayList<RoutingEntry>
	public int getNumEntries() {
		return this.m_Rtable.size();
	}
	// Turns over the m_Rtable
	public ArrayList<RoutingEntry> getAllEntries() {
		return m_Rtable;
	}
	// Modifies EntryTable
	public void setM_Rtable(ArrayList<RoutingEntry> m_Rtable) {
		this.m_Rtable = m_Rtable;
	}
	// Cleans the RouteTable
	public synchronized void clear() {
		this.m_Rtable.clear();
	}
	// Cleans an Entry
	public synchronized void deleteEntry(RoutingEntry paramRoutingEntry) {
		for (int i = 0; i < this.m_Rtable.size(); i++) {
			if ((RoutingEntry) this.m_Rtable.get(i) == paramRoutingEntry) {
				this.m_Rtable.remove(i);
				return;
			}
		}
	}
	// Implements an ip searching in local table
	public RoutingEntry getForIP(String ip, String mask) {
		for (Iterator<RoutingEntry> iterator = this.m_Rtable.iterator(); iterator.hasNext();) {
			RoutingEntry routingEntry = (RoutingEntry) iterator.next();
			if (routingEntry.getIp().getHostAddress().equals(ip)&& routingEntry.getMascara().getHostAddress().equals(mask)) {
				return routingEntry;
			}
		}
		return null;
	}
		

	// Implements a parser of local table to Bytes	
	public byte[] toByteArray(Object obj, InetAddress dest) {
		
		byte[] toret = {
			2, 2, 0, 0,
	//		-1, -1, 0, 2,
		//	0, 0, 0, 0,
	//		0, 0, 0, 0,
		//	0, 0, 0, 0,
		//	0, 0, 0, 0
		};

		for (RoutingEntry entry : this.m_Rtable) {
			byte[] arrayDestino = entry.getIp().getAddress();
			byte[] mascara = entry.getMascara().getAddress();
			byte coste = (byte) entry.getMetric();
			// Split Horizon
			InetAddress ss = entry.getNextHop();
			if (ss != null){
				if (entry.getNextHop().getHostAddress().equals(dest.getHostAddress())) {
					coste = (byte) RoutingEntry.INFINITE_METRIC;
				}
			}
			
			byte[] entryarray = new byte[] {
				0, 2, 0, 0,
				arrayDestino[0],
				arrayDestino[1],
				arrayDestino[2],
				arrayDestino[3],
				mascara[0],
				mascara[1],
				mascara[2],
				mascara[3],
				0, 0, 0, 0,
				0, 0, 0, coste
			};
			byte[] aux = new byte[toret.length + entryarray.length];
			System.arraycopy(toret, 0, aux, 0, toret.length);
			System.arraycopy(entryarray, 0, aux, toret.length, entryarray.length);
			toret = aux;
		}
		return toret;
	}
	
	// Implements a parser from bytes to object
	public static RoutingTable toObject (byte[] bytes) {
		RoutingTable table = new RoutingTable();
		int entradas = (bytes.length - 4) / 20;
		for (int i = 0; i < entradas; i++) {
			byte[] dest = new byte[] {
				bytes[20 * i + 4 + 4],
				bytes[20 * i + 4 + 5],
				bytes[20 * i + 4 + 6],
				bytes[20 * i + 4 + 7]
			};
			byte[] masc = new byte[] {
				bytes[20 * i + 4 + 8],
				bytes[20 * i + 4 + 9],
				bytes[20 * i + 4 + 10],
				bytes[20 * i + 4 + 11]
			};
			byte[] sigs = new byte[] {
				bytes[20 * i + 4 + 12],
				bytes[20 * i + 4 + 13],
				bytes[20 * i + 4 + 14],
				bytes[20 * i + 4 + 15]
			};
			int cost = bytes[20 * i + 4 + 19];
			try {
				InetAddress destino = InetAddress.getByAddress(dest);
				InetAddress mascara = InetAddress.getByAddress(masc);
				InetAddress siguienteSalto = InetAddress.getByAddress(sigs);
				if (!destino.getHostAddress().equals("0.0.0.0")) {
					table.addEntry(new RoutingEntry(destino, mascara, siguienteSalto, cost));
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return table;
	}
}
