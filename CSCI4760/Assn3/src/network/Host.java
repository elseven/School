package network;

import java.util.ArrayList;
import java.util.Hashtable;

import structure.DistanceTable;
import structure.MinimumDistanceTable;
import structure.OrderedPair;

/**
 * Represents a node/host device in a {@link Network}. Each host has a unique
 * hostName, a list of neighbors, a list of knownHosts, a {@link DistanceTable},
 * and all neighboring hosts' {@link MinimumDistanceTable}s. The host will
 * calculate the paths to each knownHost and determine which neighboring host
 * will result in the shortest path to the final destination. Note that these
 * calculations are not guaranteed to be accurate if something strange happens
 * to the connections in the network, as the host has limited knowledge of the
 * network as a whole.
 * 
 * @author Elliott Tanner
 * 
 */
public class Host {

	// TODO FINISH COMMENTING
	private String									hostName		= "";
	private ArrayList<String>						neighbors		= new ArrayList<String>();
	private ArrayList<String>						knownHosts		= new ArrayList<String>();
	private DistanceTable							distanceTable	= new DistanceTable();
	private Hashtable<String, MinimumDistanceTable>	neighborTables	= new Hashtable<String, MinimumDistanceTable>();

	/**
	 * Default constructor
	 */
	public Host() {
		// default
	}

	/**
	 * Creates a new Host with hostName set to "name"
	 * 
	 * @param name
	 *            the unique hostName
	 */
	public Host(String name) {
		this.setHostName(name);
	}

	/**
	 * Returns the name of the neighboring host that provides the shortest path
	 * to "dest".
	 * 
	 * @param dest
	 *            the name of the destination host
	 * @return the name of the neighboring host
	 */
	public String getNextHop(String dest) {

		return this.distanceTable.getNextHop(dest);
	}

	/**
	 * 
	 * @param neighbor
	 * @param distance
	 */
	public void addNeighbor(String neighbor, double distance) {

		if (!this.neighbors.contains(neighbor)) {
			this.neighbors.add(neighbor);
		}
		if (!this.knownHosts.contains(neighbor)) {
			this.knownHosts.add(neighbor);
		}

		OrderedPair pair = new OrderedPair(neighbor, neighbor);
		this.distanceTable.put(pair, distance);

	}

	/**
	 * 
	 * @param dest
	 * @param neighbor
	 * @return
	 */
	public double toDestViaNeighbor(String dest, String neighbor) {

		double distance = Double.POSITIVE_INFINITY;
		try {
			distance = distanceTable.get(new OrderedPair(dest, neighbor));
		} catch (NullPointerException npe) {
			// System.out.println("INF!");
		}

		return distance;
	}

	/**
	 * 
	 * @return
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * 
	 * @param hostName
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<String> getKnownHosts() {
		return knownHosts;
	}

	/**
	 * 
	 * @param knownHosts
	 */
	public void setKnownHosts(ArrayList<String> knownHosts) {
		this.knownHosts = knownHosts;

		for (String dest : knownHosts) {

			// if dest is not a neighbor or this host
			if (!(dest.equals(this.getHostName()) || (this.neighbors
					.contains(dest)))) {

				for (String via : knownHosts) {
					if (!via.equals(this.getHostName())) {
						this.distanceTable.put(new OrderedPair(dest, via),
								Double.POSITIVE_INFINITY);
					}// if(!via...)

				}// for(via)

			}// if(!dest...)

		}// for(dest)
	}// setKnownHosts

	/**
	 * 
	 * @return
	 */
	public boolean recalculate() {

		// System.out.println("RECALC FOR " + this.getHostName());
		boolean changeDetected = false;
		for (String dest : this.knownHosts) {
			if (dest.equals(this.hostName)) {
				continue;
			}

			for (String neighbor : this.neighbors) {
				// System.out.print("\tVIA " + neighbor);
				double oldDistance = Double.POSITIVE_INFINITY;
				try {
					oldDistance = this.distanceTable.get(new OrderedPair(dest,
							neighbor));
				} catch (NullPointerException npe) {
					// TODO check
				}
				MinimumDistanceTable minNeighborTable = this.neighborTables
						.get(neighbor);
				double distToNeighbor = this.distanceTable.get(new OrderedPair(
						neighbor, neighbor));
				double newDist = distToNeighbor;
				if (!dest.equals(neighbor)) {
					newDist += minNeighborTable.get(dest);
				}

				if (oldDistance > newDist) {
					this.distanceTable.put(new OrderedPair(dest, neighbor),
							newDist);
					changeDetected = true;
				}
			}// for neighbors
		}// for knownHosts
		return changeDetected;
	}// recalculate

	/**
	 * 
	 * @param dest
	 * @return
	 */
	public double getMinDistanceTo(String dest) {
		return this.distanceTable.getMinDistanceTo(dest);
	}

	/**
	 * 
	 */
	public void clearNeighborTable() {
		this.neighborTables.clear();
	}

	/**
	 * 
	 * @param neighbor
	 * @param minNeighborTable
	 * @return
	 */
	public boolean putNeighborTable(String neighbor,
			MinimumDistanceTable minNeighborTable) {

		boolean changeDetected = false;
		MinimumDistanceTable oldTable = this.neighborTables.get(neighbor);

		for (String dest : this.getKnownHosts()) {
			if (!dest.equals(this.hostName)) {
				try {
					if (minNeighborTable.get(dest) != oldTable.get(dest)) {
						changeDetected = true;
						break;
					}// if
				} catch (NullPointerException npe) {
					changeDetected = true;
					break;
				}// try-catch
			}// if(!dest...)
		}// for dest

		this.neighborTables.put(neighbor, minNeighborTable);

		return changeDetected;
	}

	/**
	 * 
	 * @return
	 */
	public DistanceTable getDistanceTable() {
		return this.distanceTable;
	}

	/**
	 * 
	 * @param neighbors
	 */
	public void setNeighbors(ArrayList<String> neighbors) {
		this.neighbors = neighbors;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<String> getNeighbors() {

		return neighbors;
	}

	/**
	 * 
	 */
	public void printNextHopTable() {
		System.out.println();
		System.out
				.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		System.out
				.println("==================================================");
		System.out.println("TABLE " + this.hostName + ":");
		System.out.println("DEST\t|\tNEXT HOP\tDIST");
		System.out.println("-----------------------------------------");
		for (String dest : this.knownHosts) {
			if (dest.equals(this.hostName)) {
				continue;
			}

			String nextHop = this.getNextHop(dest);
			double dist = this.getMinDistanceTo(dest);
			System.out.printf("%s\t|\t%s\t\t%.2f\n", dest, nextHop, dist);
		}
		System.out
				.println("==================================================");
		System.out
				.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		System.out.println();
	}

	/**
	 * 
	 */
	public void printTable() {
		System.out.println("***********************");
		System.out.println("Host " + this.hostName + " table:");
		System.out.print("dest\\via  ");
		for (String neighbor : this.neighbors) {
			if (!neighbor.equals(this.hostName)) {
				System.out.print(neighbor + "      ");
			}

		}
		System.out.println();
		for (String dest : this.knownHosts) {

			if (!dest.equals(this.hostName)) {
				System.out.print("  " + dest + "   | ");
				for (String neighbor : this.neighbors) {
					if (!neighbor.equals(this.hostName)) {
						double dist = this.toDestViaNeighbor(dest, neighbor);
						if (dist == Double.POSITIVE_INFINITY) {
							System.out.print("  inf  ");
						} else {
							System.out.printf("  %.2f  ", dist);
						}
					}
				}
				System.out.println();
			}

		}

	}

}// Host.java
