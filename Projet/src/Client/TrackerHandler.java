package CookieClient;

import Cookie.*;
import Cookie.ListTreatment;
import CookieDownload.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


/**
 * <b>The TrackerHandler Runnable class is the class that manage the reading of the socket connected 
 * with the tracker.</b> 
 * <p>This class receive and treat request form the tracker. This class have also a fixedThreadPool to 
 * manage multithreading downloads.</p> 
 */
public class TrackerHandler implements Runnable {
    
    /**
     *  The socket connected to the tracker.
     */
    private final Socket trackerSocket;

    /**
     * listen port of the host machine.
     */
    private int clientPort;
    
    /**
     * List of shared files.
     */
    public SharedFiles files;

    /**
     * Bloking Queue UserHandler / TrackerHandler.
     */
    public BlockingQueue<CookieFile> q;

    /**
     * The worker which manage the download.
     */
    private ExecutorService downloadWorkers;

    /**
     * <b>The TrackerHandler constructor.</b>
     * @param trackerSocket
     *        The socket connected to the tracker.
     * @param clientPort
     *        The client listening port.
     * @param files
     *        A SharedFiles object that represent the shared files by the user.
     * @param q
     *        A queue between the UserHandler thread and the TrackerHandler thread. This queue is use to 
     *        communicate the files in the list request to the UserHandler
     * @param downloadWorkers
     *        A threadspool to manage multithreading download
     */
    public TrackerHandler(final Socket trackerSocket, int clientPort, SharedFiles files, BlockingQueue<CookieFile> q, ExecutorService downloadWorkers) {
	this.trackerSocket = trackerSocket;
	this.clientPort = clientPort;
	this.files = files;
	this.q = q;
	this.downloadWorkers = downloadWorkers;
    }

    /**
     * Send a message to the client.
     * This method write the message pass in parameter in the userOutput.
     * @param message
     *        The message to write.
     * @param userOutput
     *        The ouput stream of the socket.
     * @throws IOException
     *        if an error occured while writing in the userOutput.
     */
    public void sendTracker(String message, DataOutputStream userOutput) throws IOException {
	if (message != null){ 
	    System.out.println("["+new Date()+"] SENDING : "+message);
	    userOutput.write(message.getBytes("ASCII"));
	    userOutput.flush();
	}
    }

    /**
     * The method is invoked by the ExecutorService
     * This method manage the dialogue between the peer (client) and the tracker. It receive request and treat them.
     */
    @Override
    public void run (){
	
	BufferedReader userInput = null;
	DataOutputStream userOutput = null;

	try {
	    // Input stream of the client socket connected with the tracker
	    userInput = new BufferedReader(new InputStreamReader(this.trackerSocket.getInputStream()));

	    // Ouput stream of the client socket connected with the tracker
	    userOutput = new DataOutputStream(this.trackerSocket.getOutputStream());

	    // Get shared files 
	    ArrayList<CookieFile> sharedFiles = new ArrayList<CookieFile>();
	    sharedFiles = this.files.getFiles();

	    // Start the connection to the tracker
	    this.sendTracker(announce(sharedFiles, this.clientPort), userOutput);

	    // Wait for ack
	    String line = userInput.readLine();

	    // is response OK ?
	    if (line.equals("ok")) { 

		// schedule a periodic task
		ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(1);
		updateTask update = new updateTask(this.trackerSocket, this.files);

		// send un update request each 30s to the tracker
		ScheduledFuture<?> result = scheduleExecutor.scheduleAtFixedRate(update, 5, 30, TimeUnit.SECONDS);

		while (true) {
		    TrackRequest trackerRequest = null;

		    // Read a line for the socket
		    line = userInput.readLine();
		    if (line == null){
		    	break;
		    }
		    System.out.println("["+new Date()+"] RECEIVE : "+line);
		    String[] splitLine = line.split(" ");
		    

		    try {
			// The client receive a list request
			if (splitLine[0].equals("list")) {

			    // Parse the request
			    ListRequest listRequest = new ListRequest(line);
			    listRequest.Parse();

			    // Treat the request
			    ListTreatment lt = new ListTreatment(listRequest, q);
			    lt.Treat();
			}
			// The client receive an peers request
			else if (splitLine[0].equals("peers")) {

			    // Parse the request
			    PeersRequest peersRequest = new PeersRequest(line);
			    peersRequest.Parse();

			    // Treat the request
			    PeersTreatment pr = new PeersTreatment(peersRequest);
			    ArrayList<Socket> clientSockets = pr.getClientSockets();

			    // Search the file in the shared files
			    String key = peersRequest.getKey();
			    Iterator<CookieFile> it = sharedFiles.iterator();
			    int i = 1;
			    CookieFile f = null;
			    while (it.hasNext()) {
				f = it.next();
				if (f.getKey().equals(key))
				    break;
				i++;
			    }

			    // Launch the downlod thread
			    DownloadManager Download = new DownloadManager(clientSockets, f, sharedFiles, userOutput); 
			    this.downloadWorkers.execute(Download);
			}
		    }
		    catch (BadRequestException e) {
			System.err.println("["+new Date()+"] ERROR : Bad request from the tracker :" + e.getMessage());
		    }
		    catch (LengthRequestException e) {
			System.err.println("["+new Date()+"] ERROR : Bad request from the tracker :" + e.getMessage());
		    }
		}
		
		// Stop the schedule thread pool
		scheduleExecutor.shutdown();
		} 
	    else {
		System.err.println("["+new Date()+"] ERROR : Bad response from the server...");
	    }
	} catch (IOException ioe) {
	    
	}
	try {
	    // Close the reader
	    if (userInput != null) { 
		userInput.close();
	    }
	    // Close the Writer
	    if (userOutput != null) {
		userOutput.close();
	    }
	    // Close the Tracker socket
	    this.trackerSocket.close();
	    System.err.println("["+new Date()+"] ERROR : Lost connection to " + this.trackerSocket.getRemoteSocketAddress());
	} catch(IOException e) { }
    }

    
    

    /**
     * From the announce request
     * Begin the dialogue with the tracker. From the announce with the array of files and the client port
     * pass in parameter  
     * @param sharedFiles
     *        The files in shared
     * @param clientPort
     *        The client listening port
     * @return The announce request
     */
    public static String announce(ArrayList<CookieFile> sharedFiles, int clientPort){
	String stringFiles = "";
	String space = "";
	for (int i = 0; i < sharedFiles.size(); i++){
	    if (i != 0)
		space = " ";	    
	    stringFiles += space+sharedFiles.get(i).getFilename()+" "+Integer.toString(sharedFiles.get(i).getSize())+" "+Integer.toString(sharedFiles.get(i).getPieceSize())+" "+sharedFiles.get(i).getKey();
	}
	return "announce listen "+Integer.toString(clientPort)+" seed ["+stringFiles+"]\n";
    }    
}
