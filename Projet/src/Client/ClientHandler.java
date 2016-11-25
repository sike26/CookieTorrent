package CookieClient;

import Cookie.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.Date;

import java.lang.StringBuilder;


/**
 *<b>ClientHandler is the Runnable class that manage de server side of the client.</b> 
 * <p>This thread receive requests from other clients and treat them. This threads is put in the main threads pool in the main fonction.</p>
 */
public class ClientHandler implements Runnable {
    
    /**
     * The client socket.
     */
    private final Socket clientSocket;

    /**
     * Files in shared.
     */
    private ArrayList<CookieFile> files;
    
    /**
     * <b>Constructor of the clientHandler.</b>
     * @param clientSocket
     *        The server socket of the client.
     * @param files
     *        The arrayList of shared files.
     */
    public ClientHandler(final Socket clientSocket, ArrayList<CookieFile> files) {
		this.clientSocket = clientSocket;
		this.files = files;
	}

    /**
     * Send a message to the client.
     * This method write the message pass in parameter in the userOutput.
     * @param message
     *        the message to write.
     * @param userOutput
     *        the ouput stream of the socket.
     * @throws IOException
     *         if an error occured while writing in the userOutput.
     */
    public void sendClient(String message, DataOutputStream userOutput) throws IOException {
		if (message != null){
		    
		    userOutput.write(message.getBytes("ASCII"));
		    userOutput.flush();
		}
	}

    /**
     * The run method is invoked by the ExecutorService (thread pool).
     * This method manage the server part of the client
     */
    @Override
    public void run() {

    	BufferedReader userInput = null;
    	DataOutputStream userOutput = null;

    	try {
	    // Input stream of the client's server socket
	    userInput = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

	    // Ouput stream of the client's server socket
	    userOutput = new DataOutputStream(this.clientSocket.getOutputStream());
	    
	    while (true) {
		
		// Read until " " to recognize the command
		StringBuilder command = new StringBuilder();
		int c;
		while((c = userInput.read()) != -1) {
		    if ((char)c == ' ') {
			break;
		    }
		    command.append((char)c);
		}


		// The client receive an interested request
		if (command.toString().equals("interested")){

		    String origLine = command.toString().concat(" "+userInput.readLine());
		    System.out.println("["+new Date()+"] RECEIVE :" + origLine);

		    // Parse the request
		    InterestedRequest ir = new InterestedRequest(origLine);
		    ir.Parse();

		    // Then treat them
		    InterestedTreatment it = new InterestedTreatment(ir, files);
		    it.Treat();
		    try {
			// Send the response
			sendClient(it.getMessage(), userOutput);

			System.err.println("["+new Date()+"] SENDING : "+it.getMessage());
		    } catch(IOException ioe) {
			System.err.println("["+new Date()+"] ERROR : Error while sending have response to client" + ioe.getMessage());
		    }
		}
		// The client receive a getpieces request
		else if (command.toString().equals("getpieces")){

		    String origLine = command.toString().concat(" "+userInput.readLine());
		    System.out.println("["+new Date()+"] RECEIVE : " + origLine);

		    // Parse the request
		    GetpiecesRequest gr = new GetpiecesRequest(origLine);
		    try {
			gr.Parse();
		    }
		    catch (BadRequestException e) {
			System.err.println("["+new Date()+"] ERROR : Bad request from the client :" + e.getMessage());
		    }
		    catch (LengthRequestException e) {
			System.err.println("["+new Date()+"] ERROR : Bad request from the client :" + e.getMessage());
		    }

		    // Then treat them
		    GetpiecesTreatment gt = new GetpiecesTreatment(gr, files);
		    gt.Treat();

		    try {
			// Send the response
			sendClient(gt.getMessage(), userOutput);
		    } catch(IOException ioe) {
			System.err.println("["+new Date()+"] ERROR : Error while sending have response to client" + ioe.getMessage());
		    }
		}	
	    }
	} catch(IOException e){ 
	    System.err.println("["+new Date()+"] ERROR : Error while creating the socket reader" + e.getMessage());
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

	    // Close the client Socket
	    this.clientSocket.close();
	    System.err.println("["+new Date()+"] ERROR : Lost connection to " + this.clientSocket.getRemoteSocketAddress());

	} catch(IOException e){ 
	   System.err.println("["+new Date()+"] ERROR : Error while ending the clientHandler thread" + this.clientSocket.getRemoteSocketAddress()); 
	}
	
    }
}



