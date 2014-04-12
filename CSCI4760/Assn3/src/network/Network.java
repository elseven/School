/**
 * 
 * 
 */
package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

/**
 * This acts as the driving class for the Distance-Vector routing algorithm. It
 * takes in a text file containing all known {@link Host}s and the distance from
 * each {@link Host} to all neighboring Hosts. When a Host detects a change in
 * distance to any Host, that Host broadcasts its distances to the neighboring
 * Hosts. In this way any change in the network should propagate through the
 * entire network.
 * 
 * <p>
 * Note that the Hosts are not omniscient in that any given Host only knows the
 * distance to each neighboring Host and must query its neighbors to find how
 * for it is to any Hosts that are more than one hop away (non-neighboring
 * Hosts).
 * 
 * @author Elliott Tanner
 * @see Host
 * 
 */
public class Network {

	/**
	 * The keys are the names of the Hosts and the values are {@link Host}s.
	 */
	private Hashtable<String, Host> knownHosts = new Hashtable<String, Host>();

	/**
	 * Default constructor
	 */
	public Network() {
	}

	/**
	 * Takes in the path to a network-initializing text file, runs the
	 * distance-vector routing algorithm, and prints out the shortest distance
	 * and next-hops for each Host.
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.err.println("ERROR: No initialization file specified!");
			System.exit(1);
		}
		String filename = args[0];

		Network network = new Network();

		// handle IOExceptions and all other exceptions
		try {
			network.init(filename);

			network.run();

			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("==========A F T E R=========");

			network.printNextHopTables();

		} catch (IOException ioe) {
			System.err.println("ERROR: Issue reading file!");
		} catch (Exception e) {
			System.err.println("ERROR: Unknown issue!");
		}

	}

	/**
	 * Takes in the filename of a text file to be used for initialization and
	 * initializes the network based on this file.
	 * 
	 * @param filename
	 *            the path to the text file to be used for initialization
	 * @throws IOException
	 *             thrown when issue reading in file.
	 */
	public void init(String filename) throws IOException {

		// create a BufferedReader to read the file in
		File networkFile = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(networkFile));
		String initLine = null;

		/*
		 * Read in the file and initialize the network
		 */
		while ((initLine = reader.readLine()) != null) {
			/*
			 * Use initLine to make appropriate updates to the network. The line
			 * will be of the form: "<HostName1> <HostName2> <distance>". If
			 * either host is not already known to the network, add it. Also add
			 * Host2 to Host1's neighbors and vice-versa. Furthermore, store the
			 * distance between Host1 and Host2 in both hosts.
			 */
			this.initializeNeighbors(initLine);
		}// while(initLine...)

		// close the file reader
		reader.close();

		// inform all hosts of all knownHosts (neighbors and non-neighbors)
		this.informHostsOfNetwork();

		// System.out.println("------Initial Tables------");
		// this.printAllTables();

		// initialize the distance tables for all hosts
		for (Host host : this.knownHosts.values()) {
			this.exchangeTablesForHost(host);
		}
	}// init(filename)

	/**
	 * Instruct each {@link Host} to calculate its distance table and notify all
	 * neighbors of changes. This will run until the tables converge.
	 */
	public void run() {

		boolean changeDetected = true;

		/*
		 * Continue instructing all Hosts to recalculate distance vectors until
		 * all of the tables converge. If a Host detects a change since the last
		 * iteration, notify all of said Host's neighbors of this change.
		 */
		do {
			/*
			 * Calculate the distance vectors for all Hosts and keep track of
			 * the Hosts that change. Notify the neighbors of all changed hosts
			 * of the new distance vectors.
			 */

			// assume none of the hosts have changed
			changeDetected = false;
			ArrayList<Host> updatedHosts = new ArrayList<Host>();

			/*
			 * Calculate the distance vectors for all hosts and add any hosts
			 * that have changed to 'updatedHosts'
			 */
			for (Host host : this.knownHosts.values()) {
				/*
				 * Recalculate the distance table for 'host' and add 'host' to
				 * 'updatedHosts' if it is different from the previous
				 * iteration.
				 */

				boolean tempChangeDetected = host.recalculate();
				if (tempChangeDetected) {
					changeDetected = true;
					updatedHosts.add(host);
				}// if(tempChangeDetected)
			}// for(Host host : this.knownHosts.values())

			// Notify the neighbors of all hosts that have changed.
			for (Host host : updatedHosts) {
				// Notify host's neighbors of host's new distance tables.
				this.exchangeTablesForHost(host);
			}// for(Host host : updatedHosts)

			/*
			 * System.out
			 * .println("--------------------------------------------------");
			 * System.out
			 * .println("--------------------------------------------------");
			 * System.out
			 * .println("--------------------------------------------------");
			 * this.printAllTables();
			 */

		} while (changeDetected);
		// end do-while

	}// run()

	/**
	 * Prints all of the distance tables
	 */
	private void printAllTables() {

		// print all of the distance tables
		for (Host host : this.knownHosts.values()) {
			// print host's tables
			host.printTable();
		}// for

	}// printAllTables

	/**
	 * Print all of the next-hop tables
	 */
	private void printNextHopTables() {

		// print all of the next hop tables
		for (Host host : this.knownHosts.values()) {
			// print host's next-hop tables
			host.printNextHopTable();
		}// for(Host host...)
	}// printNextHopTables()

	/**
	 * Sends host's table to host's neighbors.
	 * 
	 * @param host
	 *            the {@link Host} that must send its distance table to its
	 *            neighbors
	 * @return true when a change in hosts' distance table is detected
	 */
	private boolean exchangeTablesForHost(Host host) {
		boolean changeDetected = false;

		// Add all of host's neighbor's distance tables to host.
		for (String neighborName : host.getNeighbors()) {
			/*
			 * Add neighbor's minDistanceTable to host's neighborTable
			 */
			Host neighbor = this.knownHosts.get(neighborName);
			changeDetected |= host.putNeighborTable(neighborName, neighbor
					.getDistanceTable().getMinDistaneTable());
		}// for(neighborName)

		return changeDetected;
	}// exchangeTablesForHost

	/**
	 * Sends all of the hosts a list of the hosts in the network.
	 */
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
		}// for(Host...)
	}// informHostsOfNetwork

	/**
	 * 
	 * @return the hosts in the network in a hashtable with the key as the
	 *         hostname and the value as the {@link Host}.
	 */
	public Hashtable<String, Host> getKnownHosts() {
		return this.knownHosts;
	}// getKnownHosts

	/**
	 * Takes in a line of text of the form
	 * "&ltHostName1&gt &ltHostName2&gt &ltdistance&gt". If either host is not
	 * already known to the network, it is added. It also adds Host2 to Host1's
	 * neighbors and vice-versa. Furthermore, it stores the distance between
	 * Host1 and Host2 in both {@link Host}s.
	 * 
	 * @param hostLine
	 *            a line of text of the form
	 *            "&ltHostName1&gt &ltHostName2&gt &ltdistance&gt"
	 */
	private void initializeNeighbors(String hostLine) {

		/*
		 * Break the line into 3 parts: 1) HostName1 2) HostName2 3) Distance
		 * between the hosts
		 */
		String parts[] = hostLine.split(" ");
		String hostName = parts[0];
		String neighborName = parts[1];
		double neighborDistance = Double.parseDouble(parts[2]);

		Host neighbor = null;
		Host host = null;

		/*
		 * If the network already knows about 'neighborName', store the neighbor
		 * corresponding to 'neighborName' in 'neighbor', otherwise create a new
		 * Host with the hostname 'neighborName' and add it to 'knownHosts'.
		 */
		if (this.knownHosts.containsKey(neighborName)) {
			neighbor = knownHosts.get(neighborName);
		} else {
			neighbor = new Host(neighborName);
			knownHosts.put(neighborName, neighbor);
		}// if-else

		/*
		 * If the network already knows about 'hostName', store the neighbor
		 * corresponding to 'hostName' in 'host', otherwise create a new Host
		 * with the hostname 'hostName' and add it to 'knownHosts'.
		 */
		if (this.knownHosts.containsKey(hostName)) {
			host = this.knownHosts.get(hostName);
		} else {
			host = new Host(hostName);
			this.knownHosts.put(hostName, host);
		}

		// Add host to neighbor's neighbors
		neighbor.addNeighbor(host.getHostName(), neighborDistance);

		// Add neighbor to host's neighbors
		host.addNeighbor(neighbor.getHostName(), neighborDistance);

	}// initializeNeighbors

}// Network.java
