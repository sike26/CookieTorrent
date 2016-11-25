package CookieClient;

import Cookie.*;
import GraphicInterface.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;

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

import java.util.InputMismatchException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Iterator;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;


/**
 * <b>The UserHandler class manage the interface with the user.</b>
 * <p>This class create the window interface using swing package and is able to send request to the tracket when the user want to. (look, getfile requests for examples).</p>
 */
public class UserHandler implements Runnable {

    /**
     *  The socket connected to the tracker.
     */
    private final Socket trackerSocket;

    /**
     * Files Shared.
     */
    private SharedFiles files;

    /**
     * Blocking Queue UserHandler / TrackerHandler.
     */
    public BlockingQueue<CookieFile> q;

    /**
     * <b>The UserHandler constructor.</b>
     * @param trackerSocket
     *        The socket connected to the tracker.
     * @param files
     *        The files shared by the user.
     * @param q
     *        The queue between the UserHandler thread and the TrackerHandler.
     */
    public UserHandler (final Socket trackerSocket, SharedFiles files, BlockingQueue<CookieFile> q) {
	this.trackerSocket = trackerSocket;
	this.files = files;
	this.q = q;
    }

    /**
     * The method is invoked by the ExecutorService. 
     * Launch the graphic interface.
     */
    @Override
    public void run (){
	
	JFrame frame = new JFrame("Cookie Torrent");
	
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	Actions A = new Actions(frame, trackerSocket, files, q);

	FenetreAvecBouton F = new FenetreAvecBouton(A, frame);
	frame.setContentPane(F.buildContentPane());
	frame.setJMenuBar(new Menu(A, frame));

	frame.setMinimumSize(new Dimension(500, 600));
	frame.pack();
	frame.setVisible(true);

	while (true) {
	    try {
		//sending the actual Thread of execution to sleep 200 milliseconds
		Thread.sleep(200);
	    } catch(InterruptedException ie) { }

	    // Refresh the windows content
	    F.refreshContentPane(files);
	}
    }

}
