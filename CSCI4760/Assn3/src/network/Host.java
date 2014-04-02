package network;

import java.util.ArrayList;
import java.util.Hashtable;

import structure.DistanceTable;
import structure.MinimumDistanceTable;
import structure.OrderedPair;

public class Host {

	// private Map<String, Double> distanceTable = new HashMap<String,
	// Double>();
	// private ArrayList<String> knownHosts = new ArrayList<String>();

	private String									hostName		= "";
	private ArrayList<String>						neighbors		= new ArrayList<String>();
	private ArrayList<String>						knownHosts		= new ArrayList<String>();
	private DistanceTable							distanceTable	= new DistanceTable();
	private Hashtable<String, MinimumDistanceTable>	neighborTables	= new Hashtable<String, MinimumDistanceTable>();

	public Host() {
		// TODO Auto-generated constructor stub
	}

	public Host(String name) {
		this.setHostName(name);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getNextHop(String dest) {

		String nextHop = this.neighbors.get(0);
		double minDistance = Double.POSITIVE_INFINITY;
		for (String neighbor : this.neighbors) {
			double tempDistance = toDestViaNeighbor(dest, neighbor);
			if (tempDistance < minDistance) {
				minDistance = tempDistance;
				nextHop = neighbor;
			}
		}

		return nextHop;
	}

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

	public double toDestViaNeighbor(String dest, String neighbor) {

		double distance = Double.POSITIVE_INFINITY;
		try {
			distance = distanceTable.get(new OrderedPair(dest, neighbor));
		} catch (NullPointerException npe) {
			// System.out.println("INF!");
		}

		return distance;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public ArrayList<String> getKnownHosts() {
		return knownHosts;
	}

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
					}

				}

			}

		}
	}

	public boolean recalculate() {

		System.out.println("RECALC FOR " + this.getHostName());
		boolean changeDetected = false;
		for (String dest : this.knownHosts) {
			if (dest.equals(this.hostName)) {
				continue;
			}
			// System.out.println("TO " + dest);
			double oldDistance = this.getMinDistanceTo(dest);
			for (String neighbor : this.neighbors) {
				// System.out.println("\tVIA " + neighbor);
				MinimumDistanceTable minNeighborTable = this.neighborTables
						.get(neighbor);
				double distToNeighbor = this.distanceTable.get(new OrderedPair(
						neighbor, neighbor));
				double newDist = distToNeighbor;
				if (!dest.equals(neighbor)) {
					newDist += minNeighborTable.get(dest);
				}
				// System.err.println("3");
				if (oldDistance > newDist) {
					this.distanceTable.put(new OrderedPair(dest, neighbor),
							newDist);
					changeDetected = true;
				}
			}// for neighbors
		}// for knownHosts
		return changeDetected;
	}// recalculate

	public double getMinDistanceTo(String dest) {
		return this.distanceTable.getMinDistanceTo(dest);
	}

	public void clearNeighborTable() {
		this.neighborTables.clear();
	}

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

	public DistanceTable getDistanceTable() {
		return this.distanceTable;
	}

	public void setNeighbors(ArrayList<String> neighbors) {
		this.neighbors = neighbors;
	}

	public ArrayList<String> getNeighbors() {

		return neighbors;
	}

	// DistanceTable = public Hashtable<OrderedPair, Double> getDistanceTable()
	// {
}
