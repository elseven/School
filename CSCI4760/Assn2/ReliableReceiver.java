/**
 * ReliableReceiver.java
 * @author Elliott Tanner
 */
package transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Receives packets sent by a ReliableSender
 * 
 * @author Elliott Tanner
 * 
 */
public class ReliableReceiver {

	// region fields
	public static final int	DATA_TRANSMIT_PORT	= 2015;

	public static final int	ACK_RECEIVE_PORT	= 2016;
	public static final int	ACK_SEND_PORT		= 2018;
	public static final int	DATA_RECEIVE_PORT	= 2017;
	public static final int	RELAY_PORT			= 2019;
	public static final int	PAYLOAD_LEN			= 30;
	private DatagramSocket	sendingSocket		= null;
	private DatagramSocket	ackSocket			= null;

	// endregion fields

	/**
	 * Initialize sending socket and ACK socket. ACK socket will send to relay
	 * host.
	 * 
	 * @throws SocketException
	 */
	public ReliableReceiver() throws SocketException {
		this.sendingSocket = new DatagramSocket(DATA_RECEIVE_PORT);
		this.ackSocket = new DatagramSocket(ACK_SEND_PORT);

		try {
			this.sendingSocket.connect(InetAddress.getByName("localhost"), //
					DATA_TRANSMIT_PORT); //
			this.ackSocket.connect(InetAddress.getByName("localhost"), //
					ACK_RECEIVE_PORT);
		} catch (UnknownHostException e) {
			System.out.println("ERROR!!!");
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ReliableReceiver receiver = new ReliableReceiver();
		while (true) {
			try {
				if (receiver.receive() == ReliableTransportMessage.END) {
					System.out.println("FOUND END!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * Receives packet, sends ACK or NAK, and prints contents to standard out.
	 * 
	 * @return packet opCode
	 * @throws IOException
	 */
	public char receive() throws IOException {
		byte buffer[] = new byte[1024];
		DatagramPacket datagram = new DatagramPacket(buffer, 1024);
		// System.out.println("WAITING TO RECEIVE!");

		this.sendingSocket.receive(datagram);
		// System.out.println("RECEIVED!");
		StringBuffer msgBuffer = new StringBuffer(datagram.getLength());
		
		for (int i = 0; i < datagram.getLength(); i++) {
		    char nextChar = new String(datagram.getData(),"US-ASCII").charAt(i);
		    msgBuffer.append(nextChar);
		}

		System.out.println("DATA received=\t" + new String(buffer,"US-ASCII"));

		ReliableTransportMessage message = ReliableTransportMessage
				.reconstitute(buffer);

		sendAck(true);
		System.out.println(message.getPayload());
		return message.getOpCode();
	}

	/**
	 * Sends an ACK or NAK
	 * 
	 * @param isOk
	 *            true when message is acceptable
	 * @throws IOException
	 */
	private void sendAck(boolean isOk) throws IOException {
		char opcode = ReliableTransportMessage.NAK;
		if (isOk) {
			opcode = ReliableTransportMessage.ACK;
		}
		ReliableTransportMessage message = new ReliableTransportMessage(
				this.sendingSocket.getLocalAddress(),
				this.sendingSocket.getInetAddress(),
				this.sendingSocket.getLocalPort(),
				this.sendingSocket.getPort(), opcode, 0, "");

		DatagramPacket datagram = new DatagramPacket(message.getBuffer(),
				message.getBuffer().length);

		this.sendingSocket.send(datagram);
	}
}
