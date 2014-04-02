package structure;

import java.util.ArrayList;
import java.util.Hashtable;

import network.Host;

/**
 * Represents the distance-vector table at each {@link Host}. Each key in the
 * DistanceTable is an {@link OrderedPair} of destination and neighbor host
 * names. The value is the distance from the calling {@link Host} to the
 * <i>destination</i> host via the <i>neighbor</i> host.
 * 
 * @author Elliott Tanner
 * @see OrderedPair
 */
public class DistanceTable extends Hashtable<OrderedPair, Double> {

	/**
	 * The serialVersionUID for hashing.
	 */
	private static final long	serialVersionUID	= 1004834155510223051L;

	/**
	 * Default constructor.
	 */
	public DistanceTable() {
		// default
	}

	/**
	 * 
	 * @return all neighboring hosts' names as an {@link ArrayList} of
	 *         {@link String}s.
	 */
	public ArrayList<String> getNeighbors() {

		ArrayList<String> neighbors = new ArrayList<String>();
		for (OrderedPair pair : this.getOrderedPairs()) {
			if (!neighbors.contains(pair.getNeighbor())) {
				neighbors.add(pair.getNeighbor());
			}
		}
		return neighbors;
	}// getNeighbors

	/**
	 * 
	 * @param dest
	 *            the destination host
	 * @return the length of the shortest (known) path to "dest"
	 */
	public double getMinDistanceTo(String dest) {
		double dist = Double.POSITIVE_INFINITY;
		for (OrderedPair pair : this.getOrderedPairs()) {
			if (pair.getDest().equals(dest)) {
				if (this.get(pair) < dist) {
					dist = this.get(pair);
				}

			}
		}
		return dist;
	}// getMinDistanceTo

	/**
	 * 
	 * @param dest
	 *            the destination Host
	 * @return the name of the neighboring node that will result in the shortest
	 *         (known) path to "dest".
	 */
	public String getNextHop(String dest) {
		String hop = null;
		double dist = Double.POSITIVE_INFINITY;
		for (OrderedPair pair : this.getOrderedPairs()) {
			if (pair.getDest().equals(dest)) {
				if (this.get(pair) < dist) {
					dist = this.get(pair);
					hop = pair.getNeighbor();
				}

			}
		}
		return hop;
	}// getNextHop

	/**
	 * 
	 * @return the {@link MinimumDistanceTable} for the calling {@link Host}.
	 */
	public MinimumDistanceTable getMinDistaneTable() {

		MinimumDistanceTable minDistanceTable = new MinimumDistanceTable();
		for (String dest : this.getDestinations()) {
			double dist = this.getMinDistanceTo(dest);
			minDistanceTable.put(dest, dist);
		}

		return minDistanceTable;
	}// getMinDistanceTable

	/**
	 * 
	 * @return all destination Host names as an {@link ArrayList} of
	 *         {@link String}s.
	 */
	public ArrayList<String> getDestinations() {

		ArrayList<String> dests = new ArrayList<String>();
		for (OrderedPair pair : this.getOrderedPairs()) {
			if (!dests.contains(pair.getDest())) {
				dests.add(pair.getDest());
			}
		}
		return dests;
	}// getDestinations

	/**
	 * 
	 * @return the {@link #keySet()} as an {@link ArrayList} of
	 *         {@link OrderedPair}s.
	 */
	public ArrayList<OrderedPair> getOrderedPairs() {
		return new ArrayList<OrderedPair>(this.keySet());
	}// getOrderedPairs

}// DistanceTable.java
