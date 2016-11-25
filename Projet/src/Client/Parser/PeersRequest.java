package Cookie;

import java.util.ArrayList;
import java.net.InetAddress;
import java.util.Date;

/**
 * <b>Parse of the peers request.</b>
 */
public class PeersRequest  extends TrackRequest{

    /**
     * The file key
     */
    private String key;

    /**
     * The array of peer received
     */
    private ArrayList<Peer> peersList;
    
    /**
     * <b>The PeersRequest constructor</b>
     * @param s
     *        the resquest
     */
    public PeersRequest(String s){
	super(s);
	this.key = "";
	this.peersList = new ArrayList<Peer>();
    }

    /**
     * check the form of the request
     * @return true if the request is correct, false if is not
     * @throws BadRequestException
     *         if the request is not correct
     * @throws LengthRequestException
     *         if the request is not full
     */
     public boolean isRequestCorrect() 
	 throws BadRequestException, LengthRequestException {

	 String last = this.stringSplit[this.stringSplit.length-1];
	 
	 if (!super.type().equals("peers") || this.stringSplit[2].charAt(0) != '['){
	     throw new BadRequestException("Parser unable to understand this request");
	 }
	 if (last.charAt(last.length()-1) != ']'){
	     throw new LengthRequestException();
	 }
	 
	 return true;
    }

    /**
     * Getter to the file key
     * @return the file key
     */
    public String getKey() {
	return key;
    }
    
    /**
     * getter to the peers array
     * @return the peers array
     */
    public ArrayList<Peer> getPeersList() {
	return peersList;
    }
    
    /**
     * Read the input stream of the socket and parse the request. Parse the peer ip and port for each peer
     * @throws BadRequestException
     *         if the request is not correct
     * @throws LengthRequestException
     *         if the request is not full
     */
    public void Parse() throws BadRequestException, LengthRequestException{
	if (isRequestCorrect()){
	    String [] tokens;
	    String ip;
	    int port;
	    Peer p;
	    int i = 2;
	    
	    this.key = stringSplit[1];	    
	    System.err.println("parse : "+key);
	    while(i < stringSplit.length){
		tokens = cleanString(stringSplit[i]).split(":");
		ip = tokens[0];
		port = Integer.parseInt(tokens[1]);
		
		p = new Peer(ip, port);
		peersList.add(p);
		i += 1;
	    }
	}
    }
    
    
}
