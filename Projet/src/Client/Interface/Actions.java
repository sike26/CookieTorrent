package CookieClient;

import Cookie.*;;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.regex.PatternSyntaxException;


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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

/**
 * <b>The Actions class manage the actions of the user on the interface end there treatment.</b>
 */
public class Actions {
    
    /**
     * the window
     */
    private JFrame frame;
    /**
     *  The socket connected to the tracker
     */
    private final Socket trackerSocket;
    
    /**
     * Files Shared
     */
    private SharedFiles files;
    
    /**
     * Blocking Queue User/xTrancker
     */
    public BlockingQueue<CookieFile> q;

    /*
    * Config.ini path
    */
    private String configPath = "./config.ini";

    /**
     * <b>The Action constructor</b>
     * @param frame
     *        the CookieTorrent main window
     * @param trackerSocket
     *        the socket connected to the tracker
     * @param files
     *        The files shared by the user
     * @param q
     *        The queue between the UserHandler thread and the TrackerHandler
     */
    public Actions (JFrame frame, final Socket trackerSocket, SharedFiles files, BlockingQueue<CookieFile> q) {
	this.trackerSocket = trackerSocket;
	this.files = files;
	this.q = q;
	this.frame = frame;
    }

    
    /**
     * This method is invoked when the user want to search a file.
     * Send a look request to the tracker.
     * @param response
     *        The filename or the criterion of the search.
     * @param type
     *        1 if response is a filename, 2 if response is a criterion.
     * @return true if the look is there is no error. False if an error occured.
     */
    public boolean look(String response, int type) {	
	String message = "";
	switch (type)
	    {
		// Search by filename
	    case 1: 
		// Form the request
		String filename = response;
		message = "look [filename=\""+filename+"\"]\n";
		break;                     
	    case 2:
		// Search with a criterion
		String criterion = response;		
		String[] splitCriterion = criterion.split(" ");
		
		// Is the criterion valid ?
		if (isCriterionOk(splitCriterion)){
		    String filesize = splitCriterion[1];

		    //Form the request
		    message = "look [filesize"+splitCriterion[0]+"\""+filesize+"\"]\n";
		}
		else {
		    ImageIcon img = new ImageIcon("Interface/images/erreur.png");
		    JOptionPane.showMessageDialog(frame, "Mauvais Critère. Seul les critères suivant sont supportés :\n > < = !" , "Erreur", JOptionPane.ERROR_MESSAGE, img);
		    return false;
		}
		break;                                                        
	    default:
		message = null;
		return false;
	    }
	
	try {
	    // Send the message to the tracker
	    sendTracker(message);
	    return true;
	}
	catch (IOException ioe) {
	    System.err.println("["+new Date()+"] ERROR : Error while sending a look to the tracker: "+ioe.getMessage());
	    return false;
	}
    }

    /**
     * This method is invoked when the user want to download a file
     * It use the blockingQueue with the trackerHandler and treat the response of the list request 
     * (the response of look). The user have to choose a file and them Send a getfile request to the tracker.
     */
    public void getFile() {
	boolean stop = false;
	boolean isCorrect = false;
	CookieFile f = null;
	ArrayList<CookieFile> Files = new ArrayList<CookieFile>();

	// get the files in the list request (using the blockingQueue)
	while (!stop) {
	    try {
		f = q.take();
	    } catch (InterruptedException ie) {
		// DO something !
	    }
	    if (f.getFilename().equals("END")) {
		stop = true;
	    } else {
		Files.add(f);
	    }
	}
	
	// Show the files to the user
	if (!Files.isEmpty()) {	    
	    String [] availableFiles = new String[100];
	    Iterator<CookieFile> it = Files.iterator();
	    int i = 1;
	    while (it.hasNext()) {
		f = it.next();
		availableFiles[i] = f.show();
		i++;
	    }
	    // display the window
	    ImageIcon imgQuestion = new ImageIcon("Interface/images/question.png");
	    String filename = (String)JOptionPane.showInputDialog(frame,"Choississez un fichier",
			"Fichiers", JOptionPane.QUESTION_MESSAGE, imgQuestion, availableFiles, i-1);
	    
	    if (filename == null) {
		return;
	    }
	    
	    // Parse the response of the user
	    String [] fsplit = filename.split(" - ");
	    filename = fsplit[0];
	    String key = fsplit[2].substring(6, fsplit[2].length());
	    int size = Integer.parseInt(fsplit[1].substring(9, fsplit[1].length()-6));

	    // Create a cookeFile
	    CookieFile fileToDownload = new CookieFile(filename, size, 2048, key, false);
	    ArrayList<CookieFile> sharedFiles = files.getFiles();
	    System.err.println("Filename: " +filename);

	    // Create the new file in the shared folder
	    File newFile = new File("shared/"+filename);
	    try {
		newFile.createNewFile(); 
	    } catch (IOException e) {
		System.err.println("["+new Date()+"] ERROR : Error while creating the file "+filename+" : "+e.getMessage());
	    }

	    // Add the file to the shared files
	    this.files.addSharedFile(fileToDownload);
	    
	    // Form the request
	    String message = "getfile "+key+"\n";

		try {
		    //Send the request to the tracker
		    sendTracker(message);	
		}
		catch (IOException ioe) {
		    System.err.println("["+new Date()+"] ERROR : Error while sending getfile to the tracker: "+ioe.getMessage());
		}
	} else {
	    ImageIcon img = new ImageIcon("Interface/images/erreur.png");
	    JOptionPane.showMessageDialog(frame, "Aucun fichier correspondant sur le réseau." , "Erreur", JOptionPane.ERROR_MESSAGE, img);
	}
    }
    


    /**
     * Is the string pass in parameter represent  an integer. For example isInteger("220") will return true but 
     * isInteger("22d2df") will return false
     * @param s
     *        the string you want to test
     * @return true, if the string represent an integer, false if is not.
     */
    public static boolean isInteger (String s) {	
	int radix = 10;
	Scanner sc = new Scanner(s.trim());
	if(!sc.hasNextInt(radix)) 
	    return false;
	sc.nextInt(radix);
	return !sc.hasNext();
    }

     /**
     * Check if the criterion is valid.
     * @param criterion
     *        the tring criterion after a split(" ").
     * @return true is the criterion is valid, false if is not.
     */
    private boolean isCriterionOk (String[] criterion) {
	if (criterion.length != 2)
	    return false;
	String s = criterion[0];
	String filesize = criterion[1];		
	if (!s.equals("<") && !s.equals("=") && !s.equals(">"))
	    return false;	
	if (!isInteger(filesize))
	    return false;
	return true;
    }

     /**
     * Delete the last charater of the string
     * @param s
     *        the string to midify
     * @return the string whitout the last character
     */
    private String delLast (String s){
	return s.substring(0, s.length()-1);
    }


    /**
     * This method is invoked when the user want to share a new file
     * Add the file to the array of shared files and check if the file exist in the shared folder
     * @param response
     *        the filename 
     */
    public void addSharedFile (String response) {

	// is the file exists
	File file = new File("shared/"+response);
	if (file.exists()){
	    // Create a new cookieFile
	    CookieFile cookieFile = new CookieFile(response, 2048);

	    // Add to the array of shered files
	    int res = this.files.addSharedFile(cookieFile);

	    // The file is already shared or the user type a bad filename
	    if (res != 0) {
		ImageIcon img = new ImageIcon("Interface/images/erreur.png");
		if (res == -1) {
		    JOptionPane.showMessageDialog(frame, "Le fichier est déjà présent." , "Erreur", JOptionPane.ERROR_MESSAGE, img);
		}
		else {
		    JOptionPane.showMessageDialog(frame, "Nom de fichier incorrect." , "Erreur", JOptionPane.ERROR_MESSAGE, img);
		}
	    }
	}
	else {
	    ImageIcon img = new ImageIcon("Interface/images/erreur.png");
	    JOptionPane.showMessageDialog(frame, "Ce fichier n'est pas présent dans le dossier shared" , "Erreur", JOptionPane.ERROR_MESSAGE, img);
	}	
    }


    /**
     * Delete the file from the shared files. The file is still in the shared folder
     * @param response 
     *        the filename
     */
    public void delSharedFile (String response){	
	this.files.delSharedFile(response);
    }
    

    /**
     * Send the message to the tracker. 
     * @param message 
     *        the massage to send
     * @throws IOException
     *         if an error occured while writting
     */
     public void sendTracker (String message) throws IOException {
	 DataOutputStream userOutput = new DataOutputStream(this.trackerSocket.getOutputStream());
	 System.out.println("["+new Date()+"] SENDING : "+message);
	 userOutput.write(message.getBytes("ASCII"));
	 userOutput.flush();
    }

    /**
     * Change the configuration. Rewrite the tracker port in the config.ini file.
     * @param response
     *        the new tracker port
     */
    public void changePort (String response){
	// Verifying if the port is valid 
	int port = -1;
	try {
	    port = Integer.parseInt(response);
	} catch (NumberFormatException e) 
	    {
		//Port incorrect
		ImageIcon img = new ImageIcon("Interface/images/erreur.png");
		JOptionPane.showMessageDialog(frame, "Port incorrect." , "Erreur", JOptionPane.ERROR_MESSAGE, img);
		return;
	    }

	if (port < 1024 || port > 65535) {
	    //Port incorrect
	    ImageIcon img = new ImageIcon("Interface/images/erreur.png");
	    JOptionPane.showMessageDialog(frame, "Port incorrect." , "Erreur", JOptionPane.ERROR_MESSAGE, img);
	    return;
	}
	else {
	    /*
	     * TODO : Write the new port in the config.ini file
	     */
	    try {
	    	BufferedReader br = new BufferedReader(new FileReader(configPath));
	    	ArrayList<String> lines = new ArrayList<String>();

	    /// Address ///
	    	lines.add(br.readLine());
	    	lines.add(br.readLine());
	    	
	    /// Port ///
	    	lines.add(br.readLine());
	    	lines.add(br.readLine());
	    	lines.add("tracker-port = "+port);
	    	br.close();

	    	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(configPath,false)), true);
	    	Iterator<String> it = lines.iterator();
	    	while(it.hasNext())	{
	    		String tmp = it.next();
	    		out.println(tmp);
	    	}
	    	out.close();

	    } catch(IOException e){
	    	System.out.println("["+new Date()+"] ERROR : Error while modifying the configuration file");
	    }
	}
}

    /**
     * Change the configuration. Rewrite the tracker ip address in the config.ini file.
     * @param response
     *        the new tracker ip address
     */
    public void changeAdress(String response) {
	String [] fsplit;
	try {
	    fsplit = response.split("\\.");
	}
	catch (PatternSyntaxException e) {
	    //Adresse incorrect
	    ImageIcon img = new ImageIcon("Interface/images/erreur.png");
	    JOptionPane.showMessageDialog(frame, "Adresse incorrecte." , "Erreur", JOptionPane.ERROR_MESSAGE, img);
	    return;
	}

	if (fsplit.length != 4) {
	    //Address incorrect
	    ImageIcon img = new ImageIcon("Interface/images/erreur.png");
	    JOptionPane.showMessageDialog(frame, "Adresse incorrecte." , "Erreur", JOptionPane.ERROR_MESSAGE, img);
	    return;
	}
	
	int adress[] = new int[fsplit.length];

	for (int i = 0; i < fsplit.length; i++) {
	    try {
		adress[i] = Integer.parseInt(fsplit[i]);
	    } catch (NumberFormatException e)  {
		//Adsress incorrect
		ImageIcon img = new ImageIcon("Interface/images/erreur.png");
		JOptionPane.showMessageDialog(frame, "Adresse incorrecte." , "Erreur", JOptionPane.ERROR_MESSAGE, img);
		return;
	    }
	}

	for (int i = 0; i < fsplit.length; i++) {
	    if (adress[i] < 0 || adress[i] > 255) {
		//Address incorrect
		ImageIcon img = new ImageIcon("Interface/images/erreur.png");
		JOptionPane.showMessageDialog(frame, "Adresse incorrecte." , "Erreur", JOptionPane.ERROR_MESSAGE, img);
		return;
	    }
	}
	/*
	 * TODO : Write the new adress in the config.ini file
	 */
	try {
	    	BufferedReader br = new BufferedReader(new FileReader(configPath));
	    	ArrayList<String> lines = new ArrayList<String>();

	    /// Address ///
	    	lines.add(br.readLine());
	    	lines.add("tracker-address = "+response);
	    	br.readLine(); 	
	    /// Port ///
	    	lines.add(br.readLine());
	    	lines.add(br.readLine());
	    	lines.add(br.readLine());
	    	br.close();

	    	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(configPath,false)), true);
	    	Iterator<String> it = lines.iterator();
	    	while(it.hasNext())	{
	    		String tmp = it.next();
	    		out.println(tmp);
	    	}
	    	out.close();

	    } catch(IOException e){
	    	System.out.println("["+new Date()+"] ERROR : Error while modifying the configuration file");
	    }
    }
    
}
