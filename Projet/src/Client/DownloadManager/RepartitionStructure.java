package CookieDownload;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;


/**
 * <b>The Repartition class manage the Repartition structure.</b> 
 * <p>This structure represente the repartition of the pieces of the file betweens the peers on the network.</p>
 */
public class RepartitionStructure {

    /**
     * Array of peer's buffermaps.
     */
    private ArrayList<byte []> maps;

    /**
     * Repartition of the file pieces between peers.
     */
    private ArrayList<ArrayList<Integer>> fileRepartition;
    
    /**
     * the current buffer of the file.
     */
    private byte[] CurrentBufferMap;

    /**
     * <b>RepartitionStructure constructor.</b>
     * @param maps
     *        an arraylist of buffermaps.
     * @param CurrentBufferMap
     *         the currect buffermap of the file.
     */
    public RepartitionStructure (ArrayList<byte []> maps, byte[] CurrentBufferMap) {
	this.maps = maps;
	this.fileRepartition = new ArrayList<ArrayList<Integer>>();
	this.CurrentBufferMap = CurrentBufferMap;
    }

    /**
     * Initialize the repartition structure with the maps array.
     */
    public void init () {

	int nbPeers = maps.get(0).length;

	// Dimension the array
	for (int i = 0; i < nbPeers; i++){
	    if (CurrentBufferMap[i] == 0) {
		ArrayList<Integer> lp = new ArrayList<Integer>();
		lp.add(i);
		this.fileRepartition.add(lp);
	    }
	}

	int peerNb = 0;
	int pieceNb = 0;

	// for each element of the repartition array
	Iterator<ArrayList<Integer>> fileRepIt = this.fileRepartition.iterator();
	while (fileRepIt.hasNext()){
	    peerNb = 0;
	    Iterator<byte []> itMaps = maps.iterator();
	    ArrayList<Integer> lp = fileRepIt.next();
	    pieceNb = lp.get(0);
	    // for each bufferMap in maps (for each client)
	    while (itMaps.hasNext()) {
		byte [] bufferMap = itMaps.next();
		
		// The client have this piece
		if (pieceNb < bufferMap.length && bufferMap[pieceNb] == 1) {
		    //add them to the array
		    lp.add(peerNb);
		}
		peerNb++;
	    }
	    
	}
 
    }
    
    /**
     * Get the repartition structure.
     * @return the repartition structure. Is an Arraylist of Arraylist of bytes.
     */
    public ArrayList<ArrayList<Integer>> getRepartition() {
	return fileRepartition;
    }
    
    /**
     * Update the repartition structure with a new map and the current bufferMap of the file.
     * @param maps
     *       the buffermaps of the clients wich have the file.
     * @param bufferMap
              the buffermap of the file.
     * @return the updated repartition structure.
     */
    public ArrayList<ArrayList<Integer>> update (ArrayList<byte []> maps, byte [] bufferMap) {
	this.maps = maps;
	CurrentBufferMap = bufferMap;
	fileRepartition.removeAll(fileRepartition);
	init();
	return getRepartition();
    }
    
    /**
     * Convert an array on byte in a String containing "0" and "1".
     * The string represent the array.
     * @param  bufferMap
     *         the array of byte to convert.
     * @return the string representing the array .
     */
    public String mapToString (byte [] bufferMap) {
	String bufferString = "";
	
	for(int i = 0; i < bufferMap.length; i++) {
	    if (bufferMap[i] == 1)
		bufferString = bufferString.concat("1");
	    else 
		bufferString = bufferString.concat("0");
	}	
	return bufferString;
    }

}
    
