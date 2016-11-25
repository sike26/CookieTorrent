package CookieClient;

import Cookie.*;

import Cookie.SharedFiles;
import Cookie.CookieFile;
import Cookie.ListTreatment;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Date;
import java.util.ArrayList;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <b>The main class of the application.</b>
 * <p> This class initialize the network configuration, manage the main thread pool, create the server socket of 
 * the client and wait for accept others connections from clients, create the socket with the tracker, and launch the the thread of the Interface. This class also manage the shutdown of the client.</p>
 */
public class Client extends Thread {


    public static void main(String[] args) {

	String trackerAddress = "";
	int trackerPort = 0;
	int clientPort = 0;
	

	//Read the configuration file
	Config conf = new Config();
	trackerAddress = conf.getTrackerAddress();
	trackerPort = conf.getTrackerPort();
	
	//Search for a free Port on the host
	FindPort fd = new FindPort();
	clientPort = fd.findFreePort();
	
	System.out.println("\n========= Welcome on COOKIE TORRENT ! =========\n\n");
	System.out.println("["+new Date()+"] INFO : Listening on port: "+ clientPort);
	final Client client = new Client(clientPort, trackerPort, trackerAddress);

	// Starts the client's independent thread
	client.start();
	
	try {
	    // Wait for the server to shutdown
	    client.join();
	    System.out.println("["+new Date()+"] INFO : Completed shutdown. Bye !\n\nSEE YOU SOON ON COOKIE_TORRENT !\n\n ");
	} catch (InterruptedException e) {
	    // Exit with an error condition
	    System.err.println("["+new Date()+"] ERROR : Interrupted before accept thread completed.");
	    System.exit(1);
	}
	
    }

    /** 
     * Client socket on which to accept incoming client connections.
     */
    private ServerSocket listenSocket;

    /**
     * Client socket on which to speak with the tracker.
     */
    private Socket trackerSocket;

    /**
     * Pool of worker threads.
     */
    private final ExecutorService workers = Executors.newCachedThreadPool();

    /**
     * Boolean use to keep the client working.
     */
    private volatile boolean keepRunning = true;  

    /**
     * Listening port of the client.
     */
    private int clientPort;

    /**
     * Files shared by the user.
     */
    public static SharedFiles files = new SharedFiles();

    /**
     * <b>Client constructor.</b>
     * <p>Initialize and create client server socket, tracker sokcet and connect the tracker socket</p>
     * @param clientPort 
     *        The client listening port. This port is send to the tracker with the announce request.
     * @param trackerPort
     *        The tracker listening port. This port is configurable in the config.ini file.
     * @param trackerAddress 
     *        The tracker ip address. Thos address is configurable in the config.ini file.
     */
    public Client(final int clientPort, final int trackerPort, final String trackerAddress) {
	
	// Capture Ctrl+C
	Runtime.getRuntime().addShutdownHook(new Thread() {
		@Override
		public void run() {
		    Client.this.shutdown();
		}
	    });

	this.clientPort = clientPort;

	try {
	    //Create the Server Socket of the client
	    this.listenSocket = new ServerSocket(this.clientPort);
	} catch (IOException e) {
	    System.err.println("["+new Date()+"] ERROR : An exception occurred while creating listenSocket sockets: "+ e.getMessage());
	    System.exit(1);
	}

	boolean connected = false;
	while (!connected) {
	    try {
		// Creating the tracker socket
		System.out.println("["+new Date()+"] INFO : Connection to the tracker");
		this.trackerSocket = new Socket(trackerAddress, trackerPort);
		connected = true;
		
		// Set timeout to 1 minute
		this.trackerSocket.setSoTimeout(60000);
	    } catch (IOException e) {
		System.out.println("["+new Date()+"] ERROR : An exception occurred while creating trackerSocket sockets: "+ e.getMessage());
		System.err.println("["+new Date()+"] ERROR : Unable to connect to the tracker");
	    }
	    try {
		// wait 5 seconds and try to reconnect
		Thread.sleep(2000);
		System.err.println("["+new Date()+"] INFO : Trying to connect...");
	    } catch (InterruptedException ie) { }
	    
	}
    }
    
    @Override
    public void run() {
	
	// Queue between the trackerHandler thread and the interface
 	BlockingQueue<CookieFile> q = new LinkedBlockingQueue<CookieFile>();
	
	// Worker use for manage download
	ExecutorService downloadWorkers = Executors.newCachedThreadPool();

	// Execute the tracker dialogue on another thread
	TrackerHandler trackerHandler = new TrackerHandler(this.trackerSocket, this.clientPort, files, q, downloadWorkers);
	this.workers.execute(trackerHandler);

	// Execute the user dialogue on another thread
	UserHandler userHandler = new UserHandler(this.trackerSocket, files, q);
	this.workers.execute(userHandler);

	// Set a timeout on the accept so we can catch shutdown requests
	try {
	    this.listenSocket.setSoTimeout(10000);
	} catch (SocketException e1) {
	    System.err.println("["+new Date()+"] ERROR : Unable to set timeout value");
	}
	// Accept an incoming connection, handle it, then close and repeat.
	while (this.keepRunning) {
	    try {
		// Accept the next incoming connection
		final Socket clientSocket = this.listenSocket.accept();
		System.out.println("["+new Date()+"] INFO : Accepted connection from " + clientSocket.getRemoteSocketAddress());
		
		// Set a timeout on the accept so we can catch shutdown requests
		try {
		    clientSocket.setSoTimeout(10000);
		} catch (SocketException e1) {
		    System.err.println("["+new Date()+"] ERROR : Unable to set timeout value");
		}

		// Execute the peer/peer dialogue on another thread
		ClientHandler clientHandler = new ClientHandler(clientSocket, files.getFiles());
		this.workers.execute(clientHandler);
		
	    } catch (SocketTimeoutException toe) {
	    } catch (IOException ioe) {
		System.err.println("["+new Date()+"] ERROR : Exception occurred while handling client request: "+ ioe.getMessage());
		Thread.yield();
	    }
	}
	try {
	    this.listenSocket.close();
	}
	catch (IOException e) { }
	
	//System.out.println("\nStopped accepting incoming connections");
    }
    
    /**
     * Method to execute after a Ctrl-C.
     */
    public void shutdown() {
	System.out.println("\n["+new Date()+"] INFO : Shutting down CookieTorrent.\n");

	//Stop the client
	this.keepRunning = false;

	//Stop the thread pool
	this.workers.shutdownNow();

	//Save the current state of the files
	files.updateSharedState();
	try {
	    this.join();
	} catch (InterruptedException e) {   }
	
    }
    
    
       
}
