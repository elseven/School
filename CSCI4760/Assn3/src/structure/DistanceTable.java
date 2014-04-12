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
 * @see MinimumDistanceTable
 */
public class DistanceTable extends Hashtable<OrderedPair, Double> {

	/**
	 * The serialVersionUID for hashing.
	 */
	private static final long serialVersionUID = 1004834155510223051L;

	/**
	 * Creates a default {@link DistanceTable}
	 */
	public DistanceTable() {
		// default
	}// default constructor

	/**
	 * 
	 * @return all neighboring hosts' names as an {@link ArrayList} of
	 *         {@link String}s.
	 */
	public ArrayList<String> getNeighbors() {

		ArrayList<String> neighbors = new ArrayList<String>();

		// Store all of the OrderedPair keys in an the ArrayList 'neighbors'
		for (OrderedPair pair : this.getOrderedPairs()) {
			/*
			 * If 'pair' has not yet been added to the ArrayList 'neighbors',
			 * add it.
			 */
			if (!neighbors.contains(pair.getNeighbor())) {
				neighbors.add(pair.getNeighbor());
			}// if(!neighbors...)

		}// for (OrderedPair pair...)
		return neighbors;
	}// getNeighbors

	/**
	 * 
	 * @param dest
	 *            the destination hostname
	 * @return the length of the shortest (known) path to 'dest'
	 * @see #getNextHop(String)
	 */
	public double getMinDistanceTo(String dest) {
		// initialize dist to infinity
		double dist = Double.POSITIVE_INFINITY;

		// find the minimum distance to 'dest'
		for (OrderedPair pair : this.getOrderedPairs()) {
			/*
			 * If 'pair' provides a shorter distance than the previous minimum
			 * distance, update the minimum distance to reflect this.
			 */

			/*
			 * check if 'pair' is an OrderedPair of interest (if its destination
			 * field has the value of 'dest')
			 */
			if (pair.getDest().equals(dest)) {

				// If the distance for 'pair' is less than 'dist', update 'dist'
				if (this.get(pair) < dist) {
					dist = this.get(pair);
				}// if(this.get(pair)...)
			}// if(pair.getDest()...)
		}
		return dist;
	}// getMinDistanceTo

	/**
	 * 
	 * @param dest
	 *            the destination Host
	 * @return the name of the neighboring node that will result in the shortest
	 *         (known) path to 'dest'.
	 * @see #getMinDistanceTo(String)
	 */
	public String getNextHop(String dest) {

		String hop = null;
		double dist = Double.POSITIVE_INFINITY;

		/*
		 * Find an OrderedPair with the destination hostname of 'dest' that will
		 * provide the shortest path to 'dest'. In other words, determine what
		 * neighbor will provide the shortest path to 'dest'
		 */
		for (OrderedPair pair : this.getOrderedPairs()) {
			/*
			 * If 'pair' has a shorter path than the current minimum distance,
			 * update both the minimum distance and 'hop' to reflect this.
			 */

			// Only consider OrderedPairs with destination 'dest'
			if (pair.getDest().equals(dest)) {

				if (this.get(pair) < dist) {
					dist = this.get(pair);
					hop = pair.getNeighbor();
				}// if (this.get(pair)...)

			}// if (pair.getDest()...)
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
