package Cookie;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.Date;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <b>The Peer is the class that represents a peer in the application.</b>
 */
public class Peer {

    /**
     * The soket address of the peer.
     */
    private InetSocketAddress peerAddress;

    /**
     * Constructor. Create the inet socket address of the peer.
     * @param ip
     *        the ip of the peer.
     * @param port
     *        the port of the peer socket.
     */
    public Peer(String ip, int port){
	this.peerAddress = new InetSocketAddress(ip, port);
    }
    
    /**
     * Getter to the ip Adress.
     *@return the inet address of the peer.
     */
    public InetAddress getInetAddress() {
	return peerAddress.getAddress();
    }

    /**
     * Getter to the port of the peer.
     * @return the port of the peer.
     */
    public int getPort() {
	return peerAddress.getPort();
    }

    /**
     * Getter to the inet Adress.
     *@return the inet socket address of the peer.
     */
    public InetSocketAddress getPeerAddress() {
	return peerAddress;
    }

}
