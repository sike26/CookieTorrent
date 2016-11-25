package Cookie;

import Cookie.CookieFile;

import java.util.*;
import java.util.Date;

import java.io.*;
import java.io.File;

import java.net.*;

/**
 * <b>The SharedFiles class manage the CookieFiles in the application.</b>
 */
public class SharedFiles {

    /**
     * the path to the Shared.state file where the files information is stored.
     */
    private String pathSharedState = "./Shared.state";

    /**
     * The path to the shared folder
     */
    private String pathShared = "shared/";

    /**
     * the array of shared files
     */
    private ArrayList<CookieFile> files;

    /**
     * <b>The SharedFiles constructor</b>
     */
    public SharedFiles(){
	files = new ArrayList<CookieFile>();
	init();
    }

    /**
     * Read the Shared.state file and initialize the array of files in shared (in seed and in leech).
     */
    private void init() {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(this.pathSharedState));
	    String[] lineSplit;
	    String line;
	    boolean b; 
	    CookieFile f;

	    br.readLine();
	    
	    // For each line of the file
	    while ((line = br.readLine()) != null){
		// Parse the line
		lineSplit = line.split(",");

		if (lineSplit[4].equals("true"))
		    b = true;
		else
		    b = false;
		// Create the cookie file corresponding
		f = new CookieFile(lineSplit[0], Integer.parseInt(lineSplit[1]), Integer.parseInt(lineSplit[2]),  lineSplit[3], b, lineSplit[5]); 

		// Check if the file exist in shared
		File file = new File(pathShared + f.getFilename());
		if (!file.exists())
		    System.err.println("["+new Date()+"] ERROR : "+f.getFilename()+" is missing");
		else
		    //Add to the files in shared
		    files.add(f);
	    } 
	    br.close();
	}catch(IOException e){
	    System.err.println("["+new Date()+"] ERROR : Error while reading Shared.state");
	}
    }

    /**
     * Delete a line in Shared.state
     * @param filename
     *        the filename you want to delete
     * @param lineNumber
     *        the line number
     * @return true if the line is deleted, false if is not 
     */
    public static boolean deleteLine(String filename, int lineNumber) {
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

	    StringBuffer sb = new StringBuffer(); 
	    String line;    
	    int nbLinesRead = 0;       

	    // for each line in the file
	    while ((line = reader.readLine()) != null) {
		// If is not the good number
		if (nbLinesRead != lineNumber) {
		    // put the line in sb
		    sb.append(line + "\n");
		}
		nbLinesRead++;
	    }
	    reader.close();
	    BufferedWriter out = new BufferedWriter(new FileWriter(filename));

	    // Write sb in the file (overwrite the file)
	    out.write(sb.toString());
	    out.close();
	} catch (Exception e) {
	    System.out.println("["+new Date()+"] ERROR : Exception");
	    return false;
	}
	return true;
    }

    /**
     * Add the file f in the shared file array. the file has to be in the shared folder.
     * @param f
     *        the file to add. the file has to be in the shared folder.
     * @return -1 if an error occured, 0 if the file is add without error.
     */
    public int addSharedFile(CookieFile f) {

	// Check if the file is already in shared
	Iterator<CookieFile> it = files.iterator();
	while(it.hasNext())	{
	    CookieFile tmp = it.next();
	    if(tmp.getFilename().equals(f.getFilename())) {
		System.err.println("["+new Date()+"] ERROR : This file is already shared");
		return -1;
	    }
	}

	// Check if the file exist in shared
	try {
	    BufferedReader brTest = new BufferedReader(new FileReader(pathShared + f.getFilename()));
	    brTest.close();
	}
	catch(FileNotFoundException e){
	    System.err.println("["+new Date()+"] ERROR : "+f.getFilename()+" is missing");
	    return -2;
	}
	catch(IOException e) {
	    System.err.println("IOException");
	}

	// Write the new file in shared.state
	String tmp;
	try {
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathSharedState,true)), true);

	    if (f.isComplete())
		tmp = "true";
	    else
		tmp = "false";

	    out.println(f.getFilename()+","+f.getSize()+","+f.getPieceSize()+","+f.getKey()+","+tmp+","+f.mapToString());
	    //Add them to the files in shared array
	    files.add(f);
	    out.close();

	}catch(IOException e){
	    System.err.println("["+new Date()+"] ERROR : Error while reading Shared.state");
	}

	return 0;
    }

    /**
     * Delete the file to the shared files array and in the shared.state file. the file is still in the 
     * shared folder
     * @param filename
     *        the filename to delete
     */
    public void delSharedFile(String filename) {

	try{
	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pathSharedState)));
	    String line;
	    String[] lineSplit;
	    int nbLines = 0;
	    reader.readLine();
	    // Search the line number of the file in the shared.state file
	    while((line = reader.readLine()) != null){
		nbLines++;
		lineSplit = line.split(",");
		if (lineSplit[0].equals(filename)){
		    // delete this line
		    deleteLine(pathSharedState, nbLines);
		    break;
		}
	    }
	    reader.close();

	    // Find the file in the array
	    Iterator<CookieFile> it = files.iterator();
	    int index = 0;
	    while(it.hasNext())	{
		CookieFile tmp = it.next();
		if(tmp.getFilename().equals(filename)) {
		    // remove them
		    files.remove(index);
		    break;
		}
		index++;
	    }

	}catch (IOException e) {
	    System.out.println("["+new Date()+"] ERROR : Error while reading Shared.state");
	}

    }

    /*
     * Réecris le Shared.state avec les nouveaux bufferMap
     */

    /**
     * Rewrite the shared.state file when the client is shutdown
     */
    public void updateSharedState() {

	try {
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathSharedState,false)), true);

	    // First let me take a selfie
	    out.println("filename,size,pieceSize,key,isComplete");

	    // Then update
	    Iterator<CookieFile> it = files.iterator();
	    while(it.hasNext())	{
		CookieFile f = it.next();
		String tmp;

		if (f.isComplete())
		    tmp = "true";
		else
		    tmp = "false";

		out.println(f.getFilename()+","+f.getSize()+","+f.getPieceSize()+","+f.getKey()+","+tmp+","+f.mapToString());
	    }
	    out.close();

	}catch(IOException e){
	    System.err.println("["+new Date()+"] ERROR : Error while reading Shared.state");
	}
    }
    
    /**
     * Getter to the array of files shared
     * @return the array of files shared
     */
    public ArrayList<CookieFile> getFiles() {
	return files;
    }	

}
