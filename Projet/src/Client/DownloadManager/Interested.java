package CookieDownload;

import Cookie.*;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

/**
 * <b>The Interested class manage the sending of the interested request and the receiving of the response have.</b>
 * <p>The bufferMap in the response is put in the maps arraylist commun to all the threads in the DownloadManager 
 * threads pool.</p>
 */
public class Interested  implements Runnable {
    
    /**
     * The peer socket
     */
    private Socket s;

    /**
     * The file key
     */
    private String key;

    /**
     * The file size
     */
    private int size;
    
    /**
     * The peer id
     */
    private int peerId;

    /**
     * The nomber of peers
     */
    private int nbPeers;

    /**
     * The output stream of the client socket
     */
    private DataOutputStream Output;

    /**
     * The input stream of the client socket
     */
    private BufferedReader Input;

    /**
     * Array of peer's buffermaps 
     */
    private ArrayList<byte []> maps;


    /**
     * <b>The Interested constructor.</b>
     * @param s
     *        the socket of the peer (the peer to send interested request).
     * @param key
     *        the file key.
     * @param size
     *        the file size.
     * @param nbPeers
     *        the number of peer which have the file.
     * @param peerId
     *        the peer id.
     * @param maps
     *        an Arraylist of bufferMaps.
     */
    public Interested (Socket s, String key, int size, int nbPeers, int peerId, ArrayList<byte []> maps) {
	this.s = s;
	this.key = key;
	this.size = size;
	this. peerId = peerId;
	this.nbPeers = nbPeers;
	this.maps = maps;
	try {
	    this.Output = new DataOutputStream(this.s.getOutputStream());
	    this.Input = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
	} catch (IOException e) {
	    System.err.println("["+new Date()+"] ERROR : Error while creating out/in Stream +"+e.getMessage());
	}
    }

    /**
     * The run method is invoked by the ExecutorService (thread pool).
     * This method send the interested request and receive the have response.
     */
    @Override
    public void run () {

	String message = "interested "+key+"\n";
	byte [] bufferMap = new byte[nbPeers];
	String line = "";

	// Send interested request
	try {
	    System.err.println("["+new Date()+"] SENDING : "+message);
	    Output.write(message.getBytes("ASCII"));
	    Output.flush();
	} catch (IOException e) {
	    System.err.println("["+new Date()+"] ERROR : Error while sending interest to the peer :"+e.getMessage());
	}

	// wait for response (request have)
	try {
	    line = Input.readLine();
	} catch (IOException e) { 
	    System.exit(1);
	}
	String [] splitLine = line.split(" ");

	// Parse the response
	if (splitLine[0].equals("have")) {
	    System.err.println("["+new Date()+"] RECEIVE: "+line);
	    HaveRequest hr = new HaveRequest(line);
	    
	    hr.Parse();
	    if (hr.getKey().equals(key)){
		bufferMap = hr.getBufferMap();
	    }
	    // Add the bufferMap to the maps array
	    maps.add(peerId, bufferMap);
	}

    }

}
