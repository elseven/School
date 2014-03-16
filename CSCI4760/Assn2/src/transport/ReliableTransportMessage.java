/**
 * ReliableTransportMessage.java
 * @author Elliott Tanner
 */
package transport;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Represents a transport-level message that will be delivered via a possibly
 * unreliable transport service. Packet format is as follows (all fields should
 * be human-readable): chars Content <br>
 * <code>
 * 0-14 Source IP address, left-padded with blanks <br>
 * 15-19 Source UDP port, 0-99999, left-padded with blanks <br>
 * 20-34 Destination IP address, left -padded with blanks <br>
 * 35-39 Destination UDP port, 0-99999, left-padded with blanks <br>
 * 40-40 Operation code <br>
 * 41-42 Sequence number, 0-99, left padded with blanks <br>
 * 43-72 Payload, right-padded with blanks <br>
 * 73-77 Checksum, left-padded with blanks <br>
 * </code> <br>
 * Opcodes are D=DATA, A=ACK, N=NAK, E=END. <br>
 * The checksum is the sum of all the character codes of the first 73 chars.
 * 
 * @author Elliott Tanner
 * 
 */
public class ReliableTransportMessage {

    // region fields
    /**
     * Operation code for a positive acknowledgment
     */
    public static final char	ACK    = 65;

    /**
     * Total length of this message
     */
    public static final int	BUFFER_LEN	= 78;

    /**
     * Operation code for a data message
     */
    public static final char	DATA		= 68;

    /**
     * Operation code for end of transmission
     */
    public static final char	END			= 69;

    /**
     * Length of the header
     */
    public static final int		HEADER_LEN	= 43;

    /**
     * Operation code for a negative acknowledgment
     */
    public static final char	NAK			= 78;
    /**
     * Maximum length of the text payload carried by this message. Shorter
     * payloads will be right-padded with blanks.
     */
    public static final int		PAYLOAD_LEN	= 30;

    private InetAddress			srcIP		= null;

    private InetAddress			destIP		= null;

    private int		       		srcPort;

    private int		       	        destPort;

    private char	       		opCode;

    private int	       			sequenceNo;

    private String	       		payload		= " ";

    private byte[]     			buffer;

    private int	       			storedChecksum;
    
    private static PrintWriter          error           = null;
    // endregion fields

    /**
     * Load this message with all of its fields. <br>
     * If length of 'payload' is greater than {@link #PAYLOAD_LEN} truncate
     * 'payload'. If the length of 'payload' is less than {@link #PAYLOAD_LEN}
     * 
     * @param srcIP
     *            source IP address as an InetAddress
     * @param destIP
     *            destination IP address as an InetAddress
     * @param srcPort
     *            source UDP port number as an int (0-99999)
     * @param destPort
     *            destination UDP port number as an int (0-99999)
     * @param opCode
     *            operation code as a char ('D', 'A', 'N', or 'E')
     * @param seqNo
     *            the sequence number (0-99)
     * @param payload
     *            main body of the message
     */
    public ReliableTransportMessage(InetAddress srcIP, InetAddress destIP,
				    int srcPort, int destPort, char opCode, int seqNo, String payload) {

	if(error == null){
	    try{
		error = new PrintWriter(new File("msg_error_out.txt"));
	    }catch (IOException ioe){
		//do nothing
	    }
	}
	this.srcIP = srcIP;
	this.destIP = destIP;
	this.srcPort = srcPort;
	this.destPort = destPort;
	this.opCode = opCode;
	this.sequenceNo = seqNo;
	this.payload = String.copyValueOf(rightPaddedString(payload));
	this.encode();
	
    }

    // region static methods

    /**
     * Create and print out a message
     * 
     * @param args
     * @throws UnknownHostException
     */
    public static void main(String[] args) throws UnknownHostException {

    }

    /**
     * @param encodingBytes
     * @return a {@link ReliableTransportMessage} containing the data in
     *         encodingBytes
     */
    public static ReliableTransportMessage reconstitute(byte[] encodingBytes) {
	ReliableTransportMessage message = null;
	
	try {
	    char encodingChars[] = new char[encodingBytes.length];

	    for (int byteIndex = 0; byteIndex < encodingBytes.length; byteIndex++) {
		/*
		 * convert the byte at 'byteIndex' in encodingBytes to a char
		 * and place this char in encodingChars at 'byteIndex'
		 */

		encodingChars[byteIndex] = (char) encodingBytes[byteIndex];
	    }//for (byteIndex...)



	    String encodingString = new String(encodingChars);

	    String sourceIPString = encodingString.substring(0, 15).trim();
	    String sourcePortString = encodingString.substring(15, 20).trim();
	    String destIPString = encodingString.substring(20, 35).trim();
	    String destPortString = encodingString.substring(35, 40).trim();
	    String opCodeString = encodingString.substring(40, 41).trim();
	    String sequenceString = encodingString.substring(41, 43).trim();
	    String payloadString = encodingString.substring(43, 73);
	    String checksumString = encodingString.substring(73, 78).trim();
	    InetAddress tempSrcIP = InetAddress.getByName(sourceIPString);
	    InetAddress tempDestIP = InetAddress.getByName(destIPString);
	    
	    /*
	    System.out.println("*************************************");
	    System.out.println("*****************DATA****************");
	    System.out.println("SOURCE IP  =\t" + sourceIPString);
	    System.out.println("SOURCE PORT=\t" + sourcePortString);
	    System.out.println("DEST IP    =\t" + destIPString);
	    System.out.println("DEST PORT  =\t" + destPortString);
	    System.out.println("OP CODE    =\t" + opCodeString);
	    System.out.println("SEQ        =\t" + sequenceString);
	    System.out.println("PAYLOAD    =\t" + payloadString);
	    System.out.println("CHECKSUM   =\t" + checksumString);
	    System.out.println("***************END DATA**************");
	    System.out.println("*************************************");
	    */

	    
	    int tempSrcPort = Integer.parseInt(sourcePortString);
	    int tempDestPort = Integer.parseInt(destPortString);
	    char tempOpCode = opCodeString.charAt(0);
	    int tempSequence = Integer.parseInt(sequenceString);

	    message = new ReliableTransportMessage(tempSrcIP, tempDestIP,
						   tempSrcPort, tempDestPort, tempOpCode, tempSequence,
						   payloadString);
	    
	    
	    message.storedChecksum = Integer.parseInt(checksumString);
	    
	    if (message.getComputedChecksum() != message.storedChecksum) {
		/*
		  System.err.println("stored: " + message.storedChecksum);
		System.err
		    .println("computed: " + message.getComputedChecksum());
		*/
		throw new Exception("ERROR: Bad checksum!");
	    }
	    
	    
	}//try
	catch (UnknownHostException e) {
	    error.println("ERROR: Unknown host: " + e.getMessage());
	    error.println(e.getMessage());
	    return null;
	}//catch unknown host 
	catch (Exception e1) {
	    error.println("ERROR: Issue reconstituting message");
	    error.println(e1.getMessage());
	    
	    return null;
	}//catch 
	//end try-catch

	return message;
    }

    /**
     * Returns the String representation of 'value', left-padded with blanks to 
     * a total size of 'width' characters.
     * 
     * @param value an IP address
     * @param width width of the resulting char array
     * @return string representatino of 'value', left-padded with blanks to a 
     * total size of 'width' characters
     */
    protected static char[] leftPaddedInt(int value, int width)
	throws IllegalArgumentException {
	String unpaddedString = Integer.toString(value);

	if (unpaddedString.length() > width) {
	    //System.out.println("VALUE: " + value + "\tWIDTH: " + width);
	    throw new IllegalArgumentException(
					       "'value' cannot be represented in 'width' chars.");
	}

	String paddedString = unpaddedString;
	for (int i = unpaddedString.length(); i < width; i++) {
	    paddedString = " " + paddedString;
	}
	
	return paddedString.toCharArray();
    }

    /**
     * @param payload
     * @return
     */
    protected static char[] rightPaddedString(String payload) {
	String unpaddedString = payload;
	String paddedString = unpaddedString;
	if (unpaddedString.length() > PAYLOAD_LEN) {
	    paddedString = unpaddedString.substring(0, PAYLOAD_LEN - 1);
	} else {
	    for (int i = unpaddedString.length(); i < PAYLOAD_LEN; i++) {
		paddedString += " ";
	    }
	}

	return paddedString.toCharArray();
    }

    /**
     * 
     * @param value
     * @param width
     * @return
     */
    public static String leftPaddedIP(InetAddress value, int width) {
	String padded = value.getHostAddress();

	while (padded.length() < width) {
	    // add a space character to the left of the string
	    padded = " " + padded;
	}

	return padded;

    }

    // endregion static methods

    /**
     * Encodes this ReliableTransportMessage into its buffer.
     */
    public void encode() {
	String encodedString = "";
	encodedString += leftPaddedIP(this.srcIP, 15); // 0-14
	encodedString += new String(leftPaddedInt(this.srcPort, 5)); // 15-19
	encodedString += leftPaddedIP(this.destIP, 15);// 20-34
	encodedString += new String(leftPaddedInt(this.destPort, 5));// 35-39
	encodedString += this.opCode;// 40
	encodedString += new String(leftPaddedInt(this.sequenceNo, 2));// 41-42
	encodedString += new String(rightPaddedString(this.payload));// 43-72
	char encodedChars[] = encodedString.toCharArray();
	int sum = 0;
	for (char encodedChar : encodedChars) {
	    sum += (int) encodedChar;
	}
	encodedString += new String(leftPaddedInt(sum, 5));// 73-77
	this.buffer = encodedString.getBytes();

    }

    // region getters

    /**
     * Returns the entire contents of this message as a byte array.
     * 
     * @return entire message as byte array
     */
    public byte[] getBuffer() {
	return this.buffer;

    }

    // region checksum getters
    /**
     * Returns the checksum stored in the trailer of the message.
     */
    protected int getStoredChecksum() {

	return this.storedChecksum;

    }

    /**
     * Returns the int value of the index-th byte of this message.
     * 
     * @param index
     * @return index-th byte of message as an int
     */
    protected int getChecksumTerm(int index) {

	return (int) this.buffer[index];
    }

    /**
     * Returns the sum of the first {@link #HEADER_LEN}+{@link #PAYLOAD_LEN}
     * chars of buffer.
     */
    protected int getComputedChecksum() {

	int sum = 0;
	for (int i = 0; i < HEADER_LEN + PAYLOAD_LEN; i++) {
	    sum += getChecksumTerm(i);
	}

	return sum;

    }

    // endregion checksum getters

    /**
     * Returns the destination IP address.
     */
    public InetAddress getDestIP() {
	return this.destIP;
    }

    /**
     * Returns the destination port number.
     */
    public int getDestPort() {
	return this.destPort;
    }

    /**
     * Returns the op code.
     */
    public char getOpCode() {
	return this.opCode;
    }

    /**
     * Returns the payload of this message.
     */
    public String getPayload() {
	return this.payload;
    }

    /**
     * Returns the sequence number.
     */
    public int getSequenceNo() {
	return this.sequenceNo;
    }

    /**
     * Returns the source IP address.
     */
    public InetAddress getSourceIP() {
	return this.srcIP;
    }

    /**
     * Returns the source port number.
     */
    public int getSrcPort() {
	return this.srcPort;
    }

    // endregion getters

    // region setters

    /**
     * Replaces the initial part of the payload with a new payload, without
     * changing the checksum.
     * 
     * @param newPayload
     *            the new value to be stored in payload.
     */
    public void setPayload(String newPayload) {
	this.payload = newPayload;
    }

    // endregion setters

}
