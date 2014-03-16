package transport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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

    public static final int PAYLOAD_LEN = 30;
    public static final int DATA_TRANSMIT_PORT	= 2015;
    public static final int ACK_RECEIVE_PORT	= 2016;
    public static final int DATA_RECEIVE_PORT	= 2017;
    public static final int RELAY_PORT		= 2021;
    //public static final int     RELAY_PORT_2            = 56697; //reliable
    //public static final int     RELAY_PORT_2            = 59381; //semi-reliable 
    public static final int     RELAY_PORT_2            = 49856; //reliable 
    private static String relayIP = "172.17.152.60";
    private static String localIP = "172.17.152.46";
    private static int runningSequenceNo = 0;
    private static final int TIMEOUT            = 6000;
    private DatagramSocket sendingSocket	= null;
    private DatagramSocket ackSocket            = null;
    private boolean debug =false;
    
    
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

	if(debug){
	    System.out.println("NEW SENDER!");
	}
	this.sendingSocket = new DatagramSocket(DATA_TRANSMIT_PORT, InetAddress.getByName(localIP));
	this.ackSocket = new DatagramSocket(ACK_RECEIVE_PORT,InetAddress.getByName(localIP));
	//this.connect(destIP, DATA_RECEIVE_PORT);
	

	this.sendingSocket.setSoTimeout(TIMEOUT);
	this.ackSocket.setSoTimeout(TIMEOUT);
	this.connect(destIP, RELAY_PORT);
	this.ackSocket.connect(destIP,RELAY_PORT_2);
	
	if(debug){
	    System.out.println("END NEW SENDER");
	}
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


	
	ReliableSender sender =
	    new ReliableSender(InetAddress.getByName(relayIP));
	
	
	
	while (true) {
	    char buffer[] = new char[PAYLOAD_LEN];
	    int readIndex = in.read(buffer, 0, PAYLOAD_LEN);
	    String fileString = new String(buffer);
	    
	    if (readIndex == -1) {
		sender.close();
		System.out.println("SENDER: CLOSED!");
		break;
	    }

	    sender.singleSend(fileString, runningSequenceNo);
	    runningSequenceNo++;
	    if (runningSequenceNo > 99) {
		runningSequenceNo = 0;
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
	System.out.println("ABOUT TO CONNECT");
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
	
	if(debug){
	    System.out.println("SENDER: PAYLOAD=\t" + payload);
	    System.out.println("SENDER: LOCAL ADD=" +
			       this.sendingSocket.getLocalAddress());
	    System.out.println("SENDER: REMOTE ADD=" +
			       this.sendingSocket.getLocalAddress());
	    System.out.println("SENDER: LOCAL P=" +
			       this.sendingSocket.getLocalPort());
	    System.out.println("SENDER: REMOTE P=" +
			       DATA_RECEIVE_PORT);
	}
	
	/*
	
	ReliableTransportMessage message = 
	    new ReliableTransportMessage(this.sendingSocket.getLocalAddress(),
					 this.sendingSocket.getInetAddress(),
					 this.sendingSocket.getLocalPort(),
					 this.sendingSocket.getPort(), 
					 ReliableTransportMessage.DATA,
					 seqNo, payload);
	
	*/



	
	ReliableTransportMessage message = 
	    new ReliableTransportMessage(this.sendingSocket.getLocalAddress(),
					 this.sendingSocket.getLocalAddress(),
					 this.sendingSocket.getLocalPort(),
					 DATA_RECEIVE_PORT, 
					 ReliableTransportMessage.DATA,
					 seqNo, payload);
	
	

	DatagramPacket datagram = new DatagramPacket(message.getBuffer(),
						     message.getBuffer().length);
	//System.out.println("ABOUT TO SEND");
	this.sendingSocket.send(datagram);
	//System.out.println("SENT!");
	
	
	if (!waitForAck()) {
	    System.out.println("***RESEND!!!!***" + message.getPayload());
	    singleSend(payload, seqNo);
	}
	
    }

    /**
     * Listens for an ACK/NAK and returns true if response is an ACK.
     * 
     * @return true if response is an ACK and no timeout thrown
     * @throws IOException
     */
    private boolean waitForAck() throws IOException {
	byte buffer[] = new byte[1024];
	DatagramPacket responseDatagram = new DatagramPacket(buffer, 1024);

	//this.sendingSocket.receive(responseDatagram);
	
	try{
	    
	    this.ackSocket.receive(responseDatagram);
	}catch (SocketTimeoutException ste){
	    if(debug){
		System.out.println("TIMEOUT!");
	    }
	    return false;//timeout thrown
	}
	

	StringBuffer msgBuffer = new StringBuffer(responseDatagram.getLength());
	for (int i = 0; i < responseDatagram.getLength(); i++) {
	    msgBuffer.append((char) responseDatagram.getData()[i]);
	}

	ReliableTransportMessage response = ReliableTransportMessage
	    .reconstitute(buffer);
	//System.out.println(response.getOpCode());
	boolean isAck = (response.getOpCode() == ReliableTransportMessage.ACK);
	boolean sumOk = validateSum(response);
	boolean isOkAck = isAck && sumOk;

	System.out.println("\t\tACK RECEIVED: " + response.getSequenceNo());
	
	return isOkAck;
    }



    /**
     * 
     * @param message The ReliableTransportMessage to check
     * @return true when calculated checksum is equal to stored checksum
     */
    private boolean validateSum(ReliableTransportMessage message){
	

	return message.getStoredChecksum() == message.getComputedChecksum();

    }
    
    /**
     * Sends a single packet with opcode END.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
	

	System.out.println("CLOSING...");
	/*
	ReliableTransportMessage message = 
	    new ReliableTransportMessage(this.sendingSocket.getLocalAddress(),
					 this.sendingSocket.getInetAddress(),
					 this.sendingSocket.getLocalPort(),
					 this.sendingSocket.getPort(), 
					 ReliableTransportMessage.END, 2,"");
	*/

	ReliableTransportMessage message = 
	    new ReliableTransportMessage(this.sendingSocket.getLocalAddress(),
					 this.sendingSocket.getLocalAddress(),
					 this.sendingSocket.getLocalPort(),
					 DATA_RECEIVE_PORT, 
					 ReliableTransportMessage.END, runningSequenceNo,"");
	DatagramPacket datagram = new DatagramPacket(message.getBuffer(),
						     message.getBuffer().length);

	this.sendingSocket.send(datagram);

    }
}
