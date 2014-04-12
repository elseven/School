package structure;

import network.Host;

/**
 * Represents a pair of hostnames. The first hostname in the pair is the
 * destination {@link Host}'s hostname and the second hostname is the
 * neighboring Host's hostname.
 * 
 * Note that two {@link OrderedPair}s are considered equal when the two have the
 * same {@link #dest} and {@link #neighbor} hostnames.
 * 
 * @author Elliott Tanner
 * 
 */
public class OrderedPair {

	/**
	 * The hostname of the destination {@link Host}.
	 */
	private String dest;

	/**
	 * The hostname of the neighbor {@link Host}.
	 */
	private String neighbor;

	/**
	 * Creates a new {@link OrderedPair} with the destination hostname set to
	 * 'dest' and the neighbor hostname set to 'neighbor'.
	 * 
	 * @param dest
	 * @param neighbor
	 */
	public OrderedPair(String dest, String neighbor) {
		this.setDest(dest);
		this.setNeighbor(neighbor);
	}// OrderedPair(dest,neighbor)

	@Override
	/**
	 * @param o
	 *            an {@link Object} to compare to this {@link OrderedPair}
	 * @return true when this {@link OrderedPair} and 'o' have the same 'dest'
	 *         and 'neighbor' hostnames
	 */
	public boolean equals(Object o) {

		OrderedPair other = (OrderedPair) o;
		return (this.dest.equals(other.dest) && this.neighbor
				.equals(other.neighbor));

	}// equals

	@Override
	/**
	 * Returns the hashed value of this {@link OrderedPair}
	 */
	public int hashCode() {
		int hash = 0;
		int index = 0;

		/*
		 * Add up all of hashed integer values of the destination hostname
		 * characters.
		 */
		for (Character destChar : dest.toCharArray()) {
			// add the hashed value of destChar to the value of 'hash'
			hash += (((int) destChar) + dest.length())
					* (index + dest.length());
			index++;
		}

		/*
		 * Add up all of hashed integer values of the neighbor hostname
		 * characters.
		 */
		for (Character neighborChar : neighbor.toCharArray()) {
			// add the hashed value of neighborChar to the value of 'hash'
			hash += (((int) neighborChar) + neighbor.length())
					* (index + neighbor.length());
		}

		return hash;
	}// hashCode

	// ===============getters and setters===============//

	/**
	 * 
	 * @return the name of the destination hostname
	 */
	public String getDest() {
		return dest;
	}// getDest

	/**
	 * Set the destination hostname to 'dest'
	 * 
	 * @param dest
	 *            the new hostname for the destination {@link Host}
	 */
	public void setDest(String dest) {
		this.dest = dest;
	}// setDest

	/**
	 * 
	 * @return the name of the neighbor hostname
	 */
	public String getNeighbor() {
		return neighbor;
	}// getNeighbor

	/**
	 * Set the neighbor hostname to 'neighbor'
	 * 
	 * @param neighbor
	 *            the new hostname for the neighbor {@link Host}
	 */
	public void setNeighbor(String neighbor) {
		this.neighbor = neighbor;
	}// setNeighbor

}// OrderedPair.java
