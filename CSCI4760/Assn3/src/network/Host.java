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
 * @see DistanceTable
 * @see MinimumDistanceTable
 * @see Network
 * @see OrderedPair
 * 
 * 
 * 
 */
public class Host {

	/**
	 * A unique name given to the Host.
	 */
	private String hostName = "";

	/**
	 * A list of all neighbors' {@link Host#hostName}s.
	 */
	private ArrayList<String> neighbors = new ArrayList<String>();

	/**
	 * A list of all Hosts' {@link Host#hostName}s.
	 */
	private ArrayList<String> knownHosts = new ArrayList<String>();

	/**
	 * An extended {@link Hashtable} where the keys are {@link OrderedPair}s and
	 * the values are the distances corresponding to these OrderedPairs.
	 */
	private DistanceTable distanceTable = new DistanceTable();

	/**
	 * A {@link Hashtable} with the hostnames as keys and the corresponding
	 * {@link MinimumDistanceTable}s as values.
	 * 
	 */
	private Hashtable<String, MinimumDistanceTable> neighborTables = new Hashtable<String, MinimumDistanceTable>();

	/**
	 * Creates a new Host with hostName set to 'name'.
	 * 
	 * @param name
	 *            the unique hostName
	 */
	public Host(String name) {
		this.setHostName(name);
	}// Host(String)

	/**
	 * 
	 * @return the unique hostname associated with this Host.
	 */
	public String getHostName() {
		return hostName;
	}// getHostName

	/**
	 * Updates the value of {@link Host#hostName} to 'hostName'.
	 * 
	 * @param hostName
	 *            the new value for the hostName
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}// setHostName

	/**
	 * 
	 * @return knownHosts
	 */
	public ArrayList<String> getKnownHosts() {
		return knownHosts;
	}// getKnownHosts

	/**
	 * Shallow copies the parameter 'knownHosts' into the field
	 * {@link Host#knownHosts}. Also updates the {@link #distanceTable} to
	 * include all sensible OrderedPairs.
	 * 
	 * @param knownHosts
	 *            a list of all hosts in the network.
	 */
	public void setKnownHosts(ArrayList<String> knownHosts) {
		// shallow copy
		this.knownHosts = knownHosts;

		/*
		 * Add all possible OrderedPairs for which the destination Host is not
		 * this Host and the neighbor host is actually one of this Host's
		 * neighbors. Initialize the OrderedPairs' distances to infinity.
		 */
		for (String dest : knownHosts) {
			/*
			 * Create all possible OrderedPairs with the destination hostname
			 * set to 'dest'.
			 */

			// if dest is not a neighbor or this host
			if (!(dest.equals(this.getHostName()) || (this.neighbors
					.contains(dest)))) {

				/*
				 * Generate all possible OrderedPairs with destination set to
				 * 'dest' and neighbor being any neighboring Host
				 */
				for (String via : this.getNeighbors()) {
					/*
					 * Create a new OrderedPair with the destination of 'dest'
					 * and the neighbor of 'via'. Add this OrderedPair to this
					 * Host's distance table
					 */
					this.distanceTable.put(new OrderedPair(dest, via),
							Double.POSITIVE_INFINITY);
				}// for(via)

			}// if(!dest...)

		}// for(dest)
	}// setKnownHosts

	/**
	 * 
	 * @param dest
	 *            the destination Host's hostname.
	 * @return the shortest distance to 'dest' from this Host.
	 */
	public double getMinDistanceTo(String dest) {
		return this.distanceTable.getMinDistanceTo(dest);
	}// getMinDistanceTo

	/**
	 * 
	 * @return this Host's distance table.
	 */
	public DistanceTable getDistanceTable() {
		return this.distanceTable;
	}// getDistanceTable

	/**
	 * Set {@link #neighbors} to the values stored in 'neighbors' parameter
	 * 
	 * @param neighbors
	 *            the new values for {@link #neighbors}
	 */
	public void setNeighbors(ArrayList<String> neighbors) {
		this.neighbors = neighbors;
	}// setNeighbors

	/**
	 * 
	 * @return this Host's neighbors.
	 */
	public ArrayList<String> getNeighbors() {
		return neighbors;
	}// getNeighbors

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
	}// getNextHop

	/**
	 * Adds a new {@link OrderedPair} to the {@link #distanceTable}. Note that
	 * both the 'dest' and 'neighbor' values of the new OrderedPair will be set
	 * to 'neighbor'.
	 * 
	 * @param neighbor
	 *            the hostname to add to the Host's neighbors.
	 * @param distance
	 *            the direct distance from this Host to 'neighbor' via
	 *            'neighbor'.
	 */
	public void addNeighbor(String neighbor, double distance) {

		// Add 'neighbor' to 'neighbors' field.
		if (!this.neighbors.contains(neighbor)) {
			this.neighbors.add(neighbor);
		}

		// Add 'neighbor' to 'knownHosts' field.
		if (!this.knownHosts.contains(neighbor)) {
			this.knownHosts.add(neighbor);
		}

		OrderedPair pair = new OrderedPair(neighbor, neighbor);
		this.distanceTable.put(pair, distance);

	}// addNeighbor

	/**
	 * Takes in a destination and the next-hop and returns the distance from
	 * this Host to 'dest' via 'neighbor'.
	 * 
	 * @param dest
	 *            the destination Host's hostname
	 * @param neighbor
	 *            the next-hop Host's hostname
	 * @return the distance from this Host to 'dest' via 'neighbor'.
	 */
	public double toDestViaNeighbor(String dest, String neighbor) {

		double distance = Double.POSITIVE_INFINITY;
		try {
			distance = distanceTable.get(new OrderedPair(dest, neighbor));
		} catch (NullPointerException npe) {
			// do nothing
		}// try-catch

		return distance;
	}// toDestViaNeighbor

	/**
	 * Recalculates distance table.
	 * 
	 * @return true when any changes are detected in the distance table after
	 *         recalculation.
	 */
	public boolean recalculate() {

		boolean changeDetected = false;

		/*
		 * Recalculate the shortest distance from this Host to all destination
		 * Hosts.
		 */
		for (String dest : this.knownHosts) {
			/*
			 * Check if any neighbors provide a shorter distance to 'dest' than
			 * previously calculated.
			 */

			if (dest.equals(this.hostName)) {
				continue;// skip over this host
			}

			/*
			 * Check if any neighbors provide a shorter distance to 'dest' than
			 * previously calculated.
			 */
			for (String neighbor : this.neighbors) {
				/*
				 * Check if the previously stored distance from this Host to
				 * 'dest' via 'neighbor' is shorter than the distance to
				 * 'neighbor' plus the shortest distance from 'neighbor' to
				 * 'dest'
				 */

				double oldDistance = Double.POSITIVE_INFINITY;

				try {
					oldDistance = this.distanceTable.get(new OrderedPair(dest,
							neighbor));
				} catch (NullPointerException npe) {
					// do nothing
				}// try-catch

				MinimumDistanceTable minNeighborTable = this.neighborTables
						.get(neighbor);

				double distToNeighbor = this.distanceTable.get(new OrderedPair(
						neighbor, neighbor));

				double newDist = distToNeighbor;

				if (!dest.equals(neighbor)) {
					newDist += minNeighborTable.get(dest);
				}// if (!dest.equals(neighbor))

				/*
				 * update the distance and set 'changeDetected' to true if the
				 * newDist is shorter
				 */
				if (oldDistance > newDist) {
					this.distanceTable.put(new OrderedPair(dest, neighbor),
							newDist);
					changeDetected = true;
				}// if(oldDistance > newDist)
			}// for neighbors
		}// for knownHosts
		return changeDetected;
	}// recalculate

	/**
	 * Updates the {@link MinimumDistanceTable} corresponding to 'neighbor' to
	 * 'minNeighborTable'.
	 * 
	 * @param neighbor
	 *            the hostname of the neighbor Host that 'minNeighborTable'
	 *            belongs to.
	 * @param minNeighborTable
	 *            the new {@link MinimumDistanceTable} for 'neighbor'
	 * @return true if 'minNeighborTable' is different from the previously
	 *         calculated {@link MinimumDistanceTable}.
	 */
	public boolean putNeighborTable(String neighbor,
			MinimumDistanceTable minNeighborTable) {

		boolean changeDetected = false;
		MinimumDistanceTable oldTable = this.neighborTables.get(neighbor);

		/*
		 * Update all distance from this Host to all destinations via 'neighbor'
		 */
		for (String dest : this.getKnownHosts()) {
			/*
			 * Update the distance from this Host to 'dest' via 'neighbor'
			 */
			if (!dest.equals(this.hostName)) {
				try {
					if (minNeighborTable.get(dest) != oldTable.get(dest)) {
						changeDetected = true;
						break;
					}// if(minNeighborTable.get(dest) ...)
				} catch (NullPointerException npe) {
					changeDetected = true;
					break;
				}// try-catch
			}// if(!dest...)
		}// for dest

		this.neighborTables.put(neighbor, minNeighborTable);

		return changeDetected;
	}// putNeighborTable

	/**
	 * Print this Host's next-hop table using the following format:
	 * <ul>
	 * <i> <br>
	 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX <br>
	 * ======================================= <br>
	 * TABLE &ltHostName&gt: <br>
	 * DEST &nbsp &nbsp &nbsp &nbsp &nbsp | &nbsp &nbsp &nbsp &nbsp &nbsp NEXT
	 * HOP &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp DIST <br>
	 * -----------------------------------------<br>
	 * 
	 * &ltDest1&gt &nbsp &nbsp| &nbsp &nbsp &nbsp &nbsp &nbsp &ltNextHop&gt
	 * &nbsp &nbsp &nbsp &nbsp &ltdistance&gt <br>
	 * &ltDest2&gt &nbsp &nbsp| &nbsp &nbsp &nbsp &nbsp &nbsp &ltNextHop&gt
	 * &nbsp &nbsp &nbsp &nbsp &ltdistance&gt <br>
	 * &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
	 * &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp .&nbsp.&nbsp.<br>
	 * &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
	 * &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp .&nbsp.&nbsp.<br>
	 * &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
	 * &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp .&nbsp.&nbsp.<br>
	 * =======================================<br>
	 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX </i>
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

		/*
		 * Print out the next-hop rows for each destination
		 */
		for (String dest : this.knownHosts) {
			/*
			 * Print out the next-hop row for 'dest'
			 */
			if (dest.equals(this.hostName)) {
				continue;
			}

			String nextHop = this.getNextHop(dest);
			double dist = this.getMinDistanceTo(dest);
			System.out.printf("%s\t|\t%s\t\t%.2f\n", dest, nextHop, dist);
		}// for (String dest : this.knownHosts)

		System.out
				.println("==================================================");
		System.out
				.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		System.out.println();
	}// printNextHopTable

	/**
	 * (For debugging purposes) Prints out the entire distance-vector table
	 */
	public void printTable() {
		System.out.println("***********************");
		System.out.println("Host " + this.hostName + " table:");
		System.out.print("dest\\via  ");
		/**
		 * Print all of the neighbors
		 */
		for (String neighbor : this.neighbors) {
			// Print the name of 'neighbor' followed by spaces
			if (!neighbor.equals(this.hostName)) {
				System.out.print(neighbor + "      ");
			}

		}// for (String neighbor : this.neighbors)
		System.out.println();

		/*
		 * Print out the row for each destination Host
		 */
		for (String dest : this.knownHosts) {
			/*
			 * Print out the name of 'dest', followed by minimum distance to
			 * 'dest' via all of this Host's neighbors
			 */
			if (!dest.equals(this.hostName)) {
				System.out.print("  " + dest + "   | ");

				/*
				 * Print out the name of 'dest', followed by minimum distance to
				 * 'dest' via all of this Host's neighbors
				 */
				for (String neighbor : this.neighbors) {
					/*
					 * Print out the name of 'dest', followed by minimum
					 * distance to 'dest' via 'neighbor'
					 */
					if (!neighbor.equals(this.hostName)) {
						double dist = this.toDestViaNeighbor(dest, neighbor);
						if (dist == Double.POSITIVE_INFINITY) {
							System.out.print("  inf  ");
						} else {
							System.out.printf("  %.2f  ", dist);
						}// if (dist ==Double.POSITIVE_INFINITY) - else
					}// if (!neighbor.equals(this.hostName))

				}// for (String neighbor : this.neighbors)

				System.out.println();

			}// if (!dest.equals(this.hostName))

		}// for (String dest : this.knownHosts)

	}// printTable

}// Host.java
