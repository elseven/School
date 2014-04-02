package structure;

public class OrderedPair {

	private String	dest;
	private String	neighbor;

	public OrderedPair(String dest, String neighbor) {
		this.setDest(dest);
		this.setNeighbor(neighbor);
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getNeighbor() {
		return neighbor;
	}

	public void setNeighbor(String neighbor) {
		this.neighbor = neighbor;
	}

	public boolean equals(Object o) {

		OrderedPair other = (OrderedPair) o;
		return (this.dest.equals(other.dest) && this.neighbor
				.equals(other.neighbor));

	}

	public int hashCode() {
		int hash = 0;
		int index = 0;
		for (Character destChar : dest.toCharArray()) {
			hash += (((int) destChar) + dest.length())
					* (index + dest.length());
			index++;
		}

		for (Character neighborChar : neighbor.toCharArray()) {
			hash += (((int) neighborChar) + neighbor.length())
					* (index + neighbor.length());

		}

		return hash;
	}
}
