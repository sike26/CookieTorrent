package CookieDownload;

import Cookie.*;
import CookieDownload.*;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Date;
import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;

import java.lang.Math;

/**
 * <b>The Download manager manage the download process of a file.</b>
 * <p>The class send an interested request to all the peers which have the file and organizes 
 * responses in a repartition structure and them chose peers to send getpiece request. The file 
 * is put in the shared folder. This class at the moment does'nt manage peers deconections. </p>
 */
public class DownloadManager implements Runnable {

    /**
     * Array of peer's sockets
     */
    private ArrayList<Socket> peerSockets;

    /**
     * Array of peer's buffermaps 
     */
    private ArrayList<byte []> maps;

    /**
     * Nomber of peer who have the file or a part
     */
    private int nbPeers;

    /**
     * The file key
     */
    private String key;

    /**
     * File size
     */
    private int size;

    /**
     * Nomber of pices wich counpond the file
     */
    private int nbPieces;

    /**
     * Repartition of the file pieces between peers
     */
    private RepartitionStructure rs;

    /**
     * Pool of worker threads
     */
    private ExecutorService workers = Executors.newFixedThreadPool(5);

    /**
     * The CookieFile wich is downloading
     */
    private CookieFile f;

    /**
     * Array of the files in shared
     */
    private ArrayList<CookieFile> files;

    /**
     * Path the put the downloaded file
     */
    private String pathShared = "shared/";

    /**
     * The trackerHandler output
     */
    private DataOutputStream userOutput;

    /**
     * <b>The DownloadManager constructor.</b>
     * @param peerSockets
     *        the arraylist of the peer which have the file.
     * @param f
     *        the file to download.
     * @param files
     *        the files shared by the user.
     * @param userOutput
     *        the socket output stream of the TrackerHandler
     */
    public DownloadManager (ArrayList<Socket> peerSockets, CookieFile f, ArrayList<CookieFile> files, DataOutputStream userOutput) {
    	this.f = f;
    	this.key = f.getKey();
    	this.files = files;
    	this.peerSockets = peerSockets; 
    	this.nbPeers = this.peerSockets.size();
    	this.size = f.getSize();
    	this.nbPieces = (int)Math.ceil((float)size / 2048);
    	this.maps = new ArrayList<byte[]>(nbPeers);
    	this.getMaps();
	this.userOutput = userOutput;
    }
    

    /**
     * Send an intereted request to each peer and store bufferMaps in maps.
     */
    public void getMaps() {
	// Allocate the array of bufferMaps
    	this.maps = new ArrayList<byte[]>(nbPeers);
    	
	// Create a Fixed thread pool
    	this.workers = Executors.newFixedThreadPool(5);

	// For each peers
    	int peerId = 0;
	Iterator<Socket> it = peerSockets.iterator();
    	while (it.hasNext()) {
    		Socket s = it.next();
    		System.err.println("["+new Date()+"] INFO : Sending interested requests to peers");

		// Send and interested request
    		Interested interested = new Interested(s, key, size, nbPeers, peerId, maps);
    		this.workers.execute(interested);
    		peerId++;
    	}

	// Wait for threads termination
    	this.workers.shutdown();
    	try {
    		this.workers.awaitTermination(30, TimeUnit.SECONDS);
    	} catch (InterruptedException ie) {
    		System.err.println("["+new Date()+"] ERROR : Wait termination interrupted "+ie.getMessage());
    	}
    }
    
    /**
     * The run method is invoked by the ExecutorService (thread pool).
     * This method manage the download of the file.
     */
    @Override
    public void run () {
    	System.err.println("["+new Date()+"] INFO : Start Downloading");
    	ArrayList<ArrayList<Integer>> Repartition = null;
    	Iterator<ArrayList<Integer>> it = null;

	// Get the repartition structure
    	this.rs = new RepartitionStructure(maps, f.getBufferMap());
    	rs.init();

    	boolean end = f.isComplete();
	boolean stopDown = false;
	int rsSize = 0;
	// While the file is not complete
    	while (!end) {	    

	    // Create a thread pool
	    this.workers = Executors.newFixedThreadPool(5);
	    Repartition = rs.getRepartition();
	    it = Repartition.iterator();
	    int pieceIndex = 0;
	    try {
		RandomAccessFile dataFile = new RandomAccessFile(pathShared + f.getFilename(), "rws");

		// For each file's pieces
		while(it.hasNext()){

		    // Get the peer which have this piece
		    ArrayList<Integer> peers = it.next();
		    if (!peers.isEmpty())
			{
			    // Choise one randomly
			    int peerId = 0;
			    if (peers.size() > 2) {
				Random rand = new Random();
				int randIndex = rand.nextInt(peers.size()-2);
				peerId = peers.get(randIndex+1);
			    }
			    else if (peers.size() == 2) {
				peerId = peers.get(1);
			    } else {
				peerId = -1;
			    }
				    
			    // Send a getpiece request to this peer
			    if (peerId != -1) {
			    Socket s = peerSockets.get(peerId);
			    pieceIndex = peers.get(0);
			    GetPieces getPieces = new GetPieces(s, f, pieceIndex, files, dataFile);
			    this.workers.execute(getPieces);
			    }
			    // Wait to avoid concurrency 
			    try {
				Thread.sleep(100);
			    } catch (InterruptedException ie) { }
			}
		}
		    

		// Wait for threds termination
		this.workers.shutdown();
		try {
		    this.workers.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
		    System.err.println("["+new Date()+"] ERROR : Wait termination interrupted "+ie.getMessage());
		}
		
		// Close the file
		dataFile.close();
		    
		    
	    } catch (IOException ioe) {
		System.err.println("["+new Date()+"] ERROR : Error while closing the datafile : "+ioe.getMessage());
	    }
	    end = f.isComplete();
	    rsSize = Repartition.size();
	    

	    // If the file is not complete continue
	    if(!end) {
		this.getMaps();
		rs.update(maps, f.getBufferMap());
	    }

	    if (rs.getRepartition().size() == rsSize && !end) {
		stopDown = true;
		System.err.println("["+new Date()+"] INFO : Nobody have the pieces you need");
		break;
	    }
	}
	
	if (!stopDown) {
	    System.out.println("["+new Date()+"] INFO : Download finished !");

	    // Verifying the key on the downloaded file
	    MD5 h = new MD5(f.getFilename());
	    System.out.println("["+new Date()+"] INFO : Final key : "+h.getHash());
	    if (!h.getHash().equals(key)) {
		System.err.println("["+new Date()+"] ERROR : The file is corrupted");
	    }	

	    // Close peer's sockets
	    Iterator<Socket> socketIt = peerSockets.iterator();
	    if (socketIt.hasNext()){
		try {
		    socketIt.next().close();
		} catch (Exception e) {}
	    }
	} else {
	    try {
		Thread.sleep(1000);
		String message = "getfile "+this.key+"\n";
		System.out.println("["+new Date()+"] SENDING : "+message);
		userOutput.write(message.getBytes("ASCII"));
		userOutput.flush();
	    } catch (InterruptedException ie) { 
		System.err.println("["+new Date()+"] ERROR : error while waiting for send getfile : "+ie.getMessage());
	    } catch (Exception e) {
		System.err.println("["+new Date()+"] ERROR : error while sending getfile : "+e.getMessage());
	    }
	}
    }    
}
