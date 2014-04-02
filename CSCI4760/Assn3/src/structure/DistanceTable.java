package structure;

import java.util.ArrayList;
import java.util.Hashtable;

public class DistanceTable extends Hashtable<OrderedPair, Double> {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1004834155510223051L;

	// private MinimumDistanceTable minDistanceTable = new
	// MinimumDistanceTable();

	public DistanceTable() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String> getNeighbors() {

		ArrayList<String> neighbors = new ArrayList<String>();
		for (OrderedPair pair : this.getOrderedPairs()) {
			if (!neighbors.contains(pair.getNeighbor())) {
				neighbors.add(pair.getNeighbor());
			}
		}
		return neighbors;
	}

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
	}

	public MinimumDistanceTable getMinDistaneTable() {

		MinimumDistanceTable minDistanceTable = new MinimumDistanceTable();
		for (String dest : this.getDestinations()) {
			double dist = this.getMinDistanceTo(dest);
			minDistanceTable.put(dest, dist);
		}

		return minDistanceTable;
	}

	public ArrayList<String> getDestinations() {

		ArrayList<String> dests = new ArrayList<String>();
		for (OrderedPair pair : this.getOrderedPairs()) {
			if (!dests.contains(pair.getDest())) {
				dests.add(pair.getDest());
			}
		}
		return dests;
	}

	public ArrayList<OrderedPair> getOrderedPairs() {
		return new ArrayList<OrderedPair>(this.keySet());
	}

}
