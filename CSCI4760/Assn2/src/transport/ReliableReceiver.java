/**
 * ReliableReceiver.java
 * @author Elliott Tanner
 */
package transport;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Receives packets sent by a ReliableSender
 * 
 * @author Elliott Tanner
 * 
 */
public class ReliableReceiver {



    // region fields
    public static final int	DATA_TRANSMIT_PORT	= 2015; //raid-cs
    public static final int	ACK_RECEIVE_PORT	= 2016; //bootserver
    public static final int	ACK_SEND_PORT		= 2018; //rellpack
    public static final int	DATA_RECEIVE_PORT	= 2017; //bootclient
    public static final int	RELAY_PORT		= 2021; 
    //public static final int     RELAY_PORT_2            = 56697; //reliable
    //public static final int     RELAY_PORT_2            = 59381; //semi-reliable 
    //public static final int     RELAY_PORT_2            = 49856; //unreliable
    public static final int     RELAY_PORT_2            = 36675; //unreliable
    public static final int	PAYLOAD_LEN		= 30;
    
    public static PrintWriter   error                   = null;
    private static String       relayIP                 = "172.17.152.60"; 
    private static String       localIP                 = "172.17.152.46";
    
    private DatagramSocket	sendingSocket		= null;
    private DatagramSocket	ackSocket		= null;
    private int                 lastSeqNo               = -1;
    // endregion fields

    
    
    /**
     * Initialize sending socket and ACK socket. ACK socket will send to relay
     * host.
     * 
     * @throws SocketException
     */
    public ReliableReceiver() throws SocketException {


	
	try {
	    //try to initialize both sendingSocket and ackSocket

	    //create a sending socket with local port DATA_RECEIVE_PORT
	    this.sendingSocket = new DatagramSocket(DATA_RECEIVE_PORT,InetAddress.getByName(localIP));
	    this.ackSocket = new DatagramSocket(ACK_SEND_PORT,InetAddress.getByName(localIP));
	    
	    //this.sendingSocket.connect(InetAddress.getByName(relayIP),RELAY_PORT_2);
	    this.ackSocket.connect(InetAddress.getByName(relayIP),RELAY_PORT);
	
	} catch (UnknownHostException e) {
	    error.println("ERROR!!!");
	    e.printStackTrace();
	}

    }

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	
	error = new PrintWriter(new File("rcv_error_out.txt"));
	
	ReliableReceiver receiver = new ReliableReceiver();
	while (true) {
	    try {
		if (receiver.receive() == ReliableTransportMessage.END) {
		    System.out.println("\n**************************************************\nFOUND END!!!");
		    System.out.println("GOODBYE!");
		    break;
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
	char opcode = ReliableTransportMessage.NAK;
	DatagramPacket datagram = new DatagramPacket(buffer, 1024);
	
	this.sendingSocket.receive(datagram);
	/*
	try{
	    this.sendingSocket.receive(datagram);
	    

	    
	}catch (SocketTimeoutException ste){
	    if(debug){
		System.out.println("TIMEOUT!");
	    }
	    sendAck(false);//timeout thrown
	    }*/
	

	StringBuffer msgBuffer = new StringBuffer(datagram.getLength());
	
	for (int i = 0; i < datagram.getLength(); i++) {
	    char nextChar = new String(datagram.getData(),"US-ASCII").charAt(i);
	    msgBuffer.append(nextChar);
	}
	
	try{
	    ReliableTransportMessage message = ReliableTransportMessage
		.reconstitute(buffer);
	    
	
	
	    boolean messageOk = validateChecksum(message);
	    System.out.println("\t\t\tVALID CHECKSUM = " + messageOk);
	    
	    //System.out.println(message.getSequenceNo());
	
	    int expected = (lastSeqNo+1) % 100;
	    
	    boolean sequenceNoOk = (expected == message.getSequenceNo());
	    
	    System.out.println("\t\t\tSEQOK = " + sequenceNoOk);
	    
	    //if it's the next sequence, increment the sequence number
	    if(messageOk && sequenceNoOk){
		lastSeqNo ++;
		lastSeqNo %= 100;//make sure seqNo wraps around from 99 to 0
	    }

	
	    sendAck(messageOk);
	
	
	    if(messageOk){
		if(sequenceNoOk){
		    System.out.print(message.getPayload());
		}else{
		    error.println("\n******************************************");
		    error.println("\t\t\tLast ok sequence no = " + lastSeqNo + "\n\n");
		}
	    }else{
		error.println(message.getPayload());
	    }
	    
	    opcode = message.getOpCode();
	    
	}//try
	catch (NullPointerException npe){
	    error.println("NPE! opcode will default to NAK");
	    
	}
	return opcode;
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
	    
	    /*lastSeqNo ++;
	    if(lastSeqNo==100){
		lastSeqNo = 0;
		}*/
	}else{
	    //System.out.println("RESENDING!");
	}
	

	
	//this.ackSocket.connect(InetAddress.getByName(relayIP),RELAY_PORT);
	
	
	
	
	ReliableTransportMessage message = new ReliableTransportMessage(this.sendingSocket.getLocalAddress(),
									this.sendingSocket.getLocalAddress(),
									ACK_SEND_PORT,
									ACK_RECEIVE_PORT, 
									opcode, lastSeqNo, "");
	


	DatagramPacket datagram = new DatagramPacket(message.getBuffer(),
						     message.getBuffer().length);

	
	this.ackSocket.send(datagram);
	//this.sendingSocket.send(datagram);
    }



    /**
     * @param message The ReliableTransportMessage to validate.
     * @return true when the checksum is correct
     */
    private boolean validateChecksum(ReliableTransportMessage message){
	return (message.getComputedChecksum() == message.getStoredChecksum());
    }

    


}
