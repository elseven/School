package transport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Transmits a file via a Relayer to a remote {@link ReliableReceiver}, using a
 * stop-and-wait protocol with error checking.
 * 
 * @author Elliott Tanner
 * 
 */
public class ReliableSender {

    // region fields

    public static final int	PAYLOAD_LEN			= 30;
    public static final int	DATA_TRANSMIT_PORT	= 2015;
    public static final int	ACK_RECEIVE_PORT	= 2016;
    public static final int	DATA_RECEIVE_PORT	= 2017;
    public static final int	RELAY_PORT			= 2019;

    private DatagramSocket	sendingSocket		= null;
    
    private String relayIP = "172.15.152.60";
    // endregion fields
    /**
     * Initializes sending socket
     * 
     * @param destIP
     * @throws SocketException
     * @throws UnknownHostException
     */
    public ReliableSender(InetAddress destIP) throws SocketException,
						     UnknownHostException {
	// System.out.println("SENDER: NEW SENDER");
	this.sendingSocket = new DatagramSocket(DATA_TRANSMIT_PORT,
						InetAddress.getByName("localhost"));
	this.connect(destIP, DATA_RECEIVE_PORT);
    }

    /**
     * Open file and send to remote receiver
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	
	FileReader fileReader = new FileReader("divine_comedy2.txt");
	BufferedReader in = new BufferedReader(fileReader);
	
	ReliableSender sender = new ReliableSender(
						   InetAddress.getByName("localhost"));

	int seqNo = 0;
	while (true) {
	    char buffer[] = new char[PAYLOAD_LEN];
	    int readIndex = in.read(buffer, 0, PAYLOAD_LEN);
	    String fileString = new String(buffer);
	    
	    if (readIndex == -1) {
		sender.close();
		System.out.println("SENDER: CLOSED!");
		break;
	    }

	    // System.out.print(fileString);
	    sender.singleSend(fileString, seqNo);
	    seqNo++;
	    if (seqNo > 99) {
		seqNo = 0;
	    }
	}
	in.close();

    }

    /**
     * Connects to remote host at 'remoteIP', 'remotePort'
     * 
     * @param remoteIP
     *            IP address of remote host
     * @param remotePort
     *            UDP port number at remote host
     */
    public void connect(InetAddress remoteIP, int remotePort) {
	this.sendingSocket.connect(remoteIP, remotePort);
    }

    /**
     * Sends a single packet with sequence number 'seqNo' and receives ACK
     * 
     * @param payload
     * @param seqNo
     * @throws IOException
     */
    public void singleSend(String payload, int seqNo) throws IOException {

	/*
	 * System.out.println("SENDER: PAYLOAD=\t" + payload);
	 * System.out.println("SENDER: LOCAL ADD=" +
	 * this.sendingSocket.getLocalAddress());
	 * System.out.println("SENDER: REMOTE ADD=" +
	 * this.sendingSocket.getInetAddress());
	 * System.out.println("SENDER: LOCAL P=" +
	 * this.sendingSocket.getLocalPort());
	 * System.out.println("SENDER: LOCAL P=" +
	 * this.sendingSocket.getPort());
	 */
	ReliableTransportMessage message = new ReliableTransportMessage(
									this.sendingSocket.getLocalAddress(),
									this.sendingSocket.getInetAddress(),
									this.sendingSocket.getLocalPort(),
									this.sendingSocket.getPort(), ReliableTransportMessage.DATA,
									seqNo, payload);

	DatagramPacket datagram = new DatagramPacket(message.getBuffer(),
						     message.getBuffer().length);

	this.sendingSocket.send(datagram);

	System.out.println("SENT: " + new String(message.getBuffer()));
	if (!waitForAck()) {
	    System.out.println("***RESEND!!!!***");
	    singleSend(payload, seqNo);
	}

    }

    /**
     * Listens for an ACK/NAK and returns true if response is an ACK.
     * 
     * @return true if response is an ACK
     * @throws IOException
     */
    private boolean waitForAck() throws IOException {
	byte buffer[] = new byte[1024];
	DatagramPacket responseDatagram = new DatagramPacket(buffer, 1024);
	// System.out.println("WAITING TO RECEIVE!");

	this.sendingSocket.receive(responseDatagram);
	StringBuffer msgBuffer = new StringBuffer(responseDatagram.getLength());
	for (int i = 0; i < responseDatagram.getLength(); i++) {
	    msgBuffer.append((char) responseDatagram.getData()[i]);
	}

	// System.out.println("DATA received=\t" + msgBuffer.toString());

	ReliableTransportMessage response = ReliableTransportMessage
	    .reconstitute(buffer);
	System.out.println(response.getOpCode());
	boolean ok = (response.getOpCode() == ReliableTransportMessage.ACK);
	return ok;
    }

    /**
     * Sends a single packet with opcode END.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
	ReliableTransportMessage message = new ReliableTransportMessage(
									this.sendingSocket.getLocalAddress(),
									this.sendingSocket.getInetAddress(),
									this.sendingSocket.getLocalPort(),
									this.sendingSocket.getPort(), ReliableTransportMessage.END, 2,
									"");

	DatagramPacket datagram = new DatagramPacket(message.getBuffer(),
						     message.getBuffer().length);

	this.sendingSocket.send(datagram);

    }
}
