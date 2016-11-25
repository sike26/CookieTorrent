package CookieClient;

import Cookie.*;

import java.net.Socket;
import java.net.SocketException;

import java.io.IOException;
import java.io.DataOutputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * <b>The updateTask is the class that manage the sending of periodic update requests to the tracker.</b>
 * <p>This method is execute by a schedulethreadpool. The period is fixed to 30 seconds.</p>
 */
class updateTask implements Runnable {
    
    /**
     * The shared files.
     */
    private SharedFiles files;

    /**
     * The socket connected with the tracker.
     */
    private Socket trackerSocket;

    /**
     * <b>the upadteTask constructor.</b>
     * @param trackerSocket
     *        The socket connected to the tracker.
     * @param files
     *        The files shared by the user.
     */
    public updateTask (Socket trackerSocket, SharedFiles files) {	
	this.files = files;
	this.trackerSocket = trackerSocket;
    }

    /**
     * The run method is invoked by the ScheduleExecutorService (thread pool) each 30 secondes
     * Send an update request to the tracker.
     */
    @Override
    public void run() {
	ArrayList<CookieFile> sharedFiles = new ArrayList<CookieFile>();   
	sharedFiles = files.getFiles();
	try {
	    
	    // Output stream of the socket
	    DataOutputStream userOutput = new DataOutputStream(this.trackerSocket.getOutputStream());
	    
	    String spaceSeed = "";
	    String spaceLeech = "";
	    String leech = "";
	    String seed = "";
	    CookieFile f;
	    String message = "";

	    // For each file in shared
	    Iterator<CookieFile> it = sharedFiles.iterator();
	    while (it.hasNext()){
		f = it.next();
		if (seed.length() != 0)
		    spaceSeed = " ";
		if (leech.length() != 0)
		    spaceLeech = " ";

		if (f.isComplete())
		    seed += spaceSeed+f.getKey();
		else
		    leech += spaceLeech+f.getKey();
	    }
	    
	    // Form the message
	    if (seed.length() > 0 && leech.length() > 0)
		message = "update seed ["+seed+"] leech ["+leech+"]\n";
	    else if (seed.length() > 0 && leech.length() == 0)
		message = "update seed ["+seed+"]\n";
	    else if (leech.length() > 0 && seed.length() == 0)
		message = "update leech ["+leech+"]\n";
	    else
		message = "update\n";
	    
	    System.out.println("["+new Date()+"] SENDING : "+message);

	    // Send the request
	    userOutput.write(message.getBytes("ASCII"));
	    userOutput.flush();

	} catch (IOException e) {
	    System.err.println("["+new Date()+"] ERROR : Error while sendin update status to the tacker: "+ e.getMessage());
	}
    }
}
