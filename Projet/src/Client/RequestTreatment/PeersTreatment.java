package Cookie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
 
import java.io.IOException;

/**
 * <b>Treatment of the peers request.</b>
 */
public class PeersTreatment {
    
    /**
     * The peers request parsed
     */
    private PeersRequest pr;

    /**
     * An array of socket, corresponding to the client which have the file
     */
    private ArrayList<Socket> clientSockets;

    /**
     * <b>The PeersTreatment constructor</b>
     * @param pr
     *        the PeersRequest instance
     */
    public PeersTreatment (PeersRequest pr) {
	this.pr = pr;
	this.clientSockets = new ArrayList<Socket>();
	init();
    }

    /**
     * This method treat the peers request. Create a socket with each peer in the peers request
     */
    public void init () {
	ArrayList<Peer> peersList = pr.getPeersList();
  
	Iterator<Peer> it = peersList.iterator();
	// For each peers
	while (it.hasNext()) {
	    Peer p = it.next();
	    try {	
		// Connect a new socket
		Socket s = new Socket(p.getInetAddress(), p.getPort());
		clientSockets.add(s);
	    } catch (IOException e) {
		System.err.println("["+new Date()+"] ERROR : An exception occurred while creating peerSocket sockets: "+ e.getMessage());
		System.exit(1);
	    }	    
	}
    
    }
    
    /**
     * Getter to the sockets array
     * @return the socket Array
     */
    public ArrayList<Socket> getClientSockets () {
	return clientSockets;	
    }
    

    



}
