/**
 * 
 * 
 */
package network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

/**
 * 
 * @author Elliott Tanner
 * 
 */
public class Network {
	// TODO FINISH COMMENTING
	private Hashtable<String, Host>	knownHosts	= new Hashtable<String, Host>();

	public Network() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Network network = new Network();
		network.init();

		boolean changeDetected = true;
		while (changeDetected) {
			changeDetected = false;
			ArrayList<Host> updatedHosts = new ArrayList<Host>();
			for (Host host : network.knownHosts.values()) {
				boolean tempChangeDetected = host.recalculate();
				if (tempChangeDetected) {
					changeDetected = true;
					updatedHosts.add(host);
				}
			}

			for (Host host : updatedHosts) {
				network.exchangeTablesForHost(host);
			}

			System.out
					.println("--------------------------------------------------");
			System.out
					.println("--------------------------------------------------");
			System.out
					.println("--------------------------------------------------");
			network.printAllTables();
		}

		network.exchangeAllTables();
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("==========A F T E R=========");

		network.printNextHopTables();

	}

	public void init() {

		// TODO read in file
		this.initializeNeighbors("A B 3.8");
		this.initializeNeighbors("A C 1.2");
		this.initializeNeighbors("B D 5.1");
		this.initializeNeighbors("C D 4.2");
		this.initializeNeighbors("B E 1.1");
		this.initializeNeighbors("D E 3.2");
		this.initializeNeighbors("E F 4.4");
		// this.initializeNeighbors("H I 9.9");

		this.informHostsOfNetwork();
		System.out.println("------Initial Tables------");
		this.printAllTables();
		this.exchangeAllTables();

	}

	private void printAllTables() {

		for (Host host : this.knownHosts.values()) {

			host.printTable();

		}

	}// printAllTables

	private void printNextHopTables() {
		for (Host host : this.knownHosts.values()) {

			host.printNextHopTable();

		}
	}

	private boolean exchangeTablesForHost(Host host) {
		boolean changeDetected = false;
		for (String neighborName : host.getNeighbors()) {
			Host neighbor = this.knownHosts.get(neighborName);
			changeDetected |= host.putNeighborTable(neighborName, neighbor
					.getDistanceTable().getMinDistaneTable());
		}

		return changeDetected;
	}

	private void exchangeAllTables() {

		for (Host host : this.knownHosts.values()) {
			this.exchangeTablesForHost(host);
		}

	}

	private void informHostsOfNetwork() {
		Collection<Host> hosts = this.getKnownHosts().values();
		ArrayList<String> names = new ArrayList<String>(this.getKnownHosts()
				.keySet());

		// Inform all hosts of all host names
		for (Host host : hosts) {
			/*
			 * Inform host of all hosts in network and initialize distances to
			 * all non-neighboring hosts to inf
			 */
			host.setKnownHosts(names);
		}
	}

	public Hashtable<String, Host> getKnownHosts() {
		return this.knownHosts;
	}

	private void initializeNeighbors(String hostLine) {
		String parts[] = hostLine.split(" ");
		String hostName = parts[0];
		String neighborName = parts[1];
		double neighborDistance = Double.parseDouble(parts[2]);
		Host neighbor = null;
		Host host = null;

		if (this.knownHosts.containsKey(neighborName)) {
			neighbor = knownHosts.get(neighborName);
		} else {
			neighbor = new Host(neighborName);
			knownHosts.put(neighborName, neighbor);
		}

		if (this.knownHosts.containsKey(hostName)) {
			host = this.knownHosts.get(hostName);
			host.addNeighbor(neighbor.getHostName(), neighborDistance);
		} else {
			host = new Host(hostName);
			this.knownHosts.put(hostName, host);
		}

		neighbor.addNeighbor(host.getHostName(), neighborDistance);
		host.addNeighbor(neighbor.getHostName(), neighborDistance);

	}

}
