package CookieDownload;

import Cookie.*;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.RandomAccessFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * <b>This class send a getpiece request to a peer.</b>
 */
public class GetPieces implements Runnable {

    /**
     * The peer socket
     */
    private Socket s;

    /**
     * The file key
     */
    private String key;
    
    /**
     * The file to download
     */
    private CookieFile f;

    /**
     * The piece index
     */
    private int pieceIndex;

    /**
     * The files in shared
     */
    private ArrayList<CookieFile> files;

    /**
     * The file
     */
    private RandomAccessFile dataFile;

    /**
     * A lock to avoid concurrency
     */
    private ReentrantReadWriteLock dataFileLock;

    /**
     * <b>The GetPIeces constructor.</b>
     * @param s
     *        the peer socket. (the socket to send the getpiece request).
     * @param f
     *        the file to download.
     * @param pieceIndex
     *        the piece concern by the getpiece request.
     * @param files
     *        the files shared by the user.
     * @param dataFile
     *        the file reader.
     */
    public GetPieces (Socket s, CookieFile f, int pieceIndex, ArrayList<CookieFile> files, RandomAccessFile dataFile) {
    	this.s = s;
	this.f = f;
    	this.key = f.getKey();
    	this.pieceIndex = pieceIndex + 1;
    	this.files = files;
	this.dataFile = dataFile;
	this.dataFileLock = dataFileLock; 
    }

    /**
     * The run method is invoked by the ExecutorService (thread pool).
     * This method send the getpiece request and receive the data request for a piece.
     */
    @Override
    public void run () {
    	DataOutputStream Output = null;
    	BufferedReader Input = null;

    	try {
	    // Output stream of the client socket
	    Output = new DataOutputStream(this.s.getOutputStream());

	    // Input stream of the client socket
	    Input = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
	    
	    // Form the request
	    String message = "getpieces " + key + " [" + pieceIndex + "]\n";
	    
	    // Send the getpiece 
	    Output.write(message.getBytes("ASCII"));
	    Output.flush();
	    System.err.println("["+new Date()+"] SENDING : " + message);
	} catch (IOException e) {
	    System.err.println("["+new Date()+"] ERROR : Error while sending getpieces to the peer :"+e.getMessage());
	}

	// Parse the data request
	DataRequest dr = new DataRequest(Input, files);
	try {
	    f.getLock().writeLock().lock();
	    dr.Parse();
	}  catch (BadRequestException bre) {
	    System.err.println(bre.getMessage());
	} catch (LengthRequestException lre) {
	    System.err.println(lre.getMessage());
	}  finally {
	    f.getLock().writeLock().unlock();
	}
	
	// Treat them
	DataTreatment dt = new DataTreatment(dr, f, files, dataFile);
	dt.Treat();
    }
    
}
