package Cookie;

import java.io.*;
import java.net.*;
import java.lang.Math;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * <b>The CookieFile is the representation of a file in the client application.</b>
 */
public class CookieFile {
    
    /**
     * The name of the file.
     */
    private String filename;

    /**
     * The size of the pieces.
     */
    private int pieceSize;

    /**
     * The key of the file (MD5).
     */
    private String key;

    /**
     * The size of the file.
     */
    private int size;
    
    /**
     * Is the file complete or not ?
     */
    private boolean isComplete;

    /**
     * The bufferMap of the file.
     */
    private byte[] bufferMap;
    
    /**
     * The number of pieces in the file.
     */
    private int nbPieces;

    /**
     * The percentage of the file the client has.
     */
    private float sizePercent;

    /**
     * A lock to avoid concurrency.
     */
    private ReentrantReadWriteLock dataFileLock;

    /**
     * A time variable to compute the download speed.
     */
    private long downTime;

    /**
     * A time variable tu compute the upload speed.
     */
    private long upTime;
    
    /**
     * The download speed.
     */
    private float downloadSpeed;

    /**
     * The upload speed.
     */
    private float uploadSpeed;


    /**
     * <b>The CookieFile constructor</b>
     * @param filename
     *        the name of the file.
     * @param size
     *        the size of the file in bytes.
     * @param pieceSize
     *        the size of a piece. This size is fixed at 2048 bytes.
     * @param key
     *        the hash of the file content with MD5 function.
     * @param isComplete
     *        a boolean set to true if the file is complete, false if not.
     */
    public CookieFile (String filename, int size, int pieceSize, String key, boolean isComplete){
	this.filename = filename;
	this.size = size;
	this.pieceSize = pieceSize;     
	this.isComplete = isComplete;
	this.key = key;
	this.nbPieces = (int)Math.ceil((float)size / pieceSize);
	this.bufferMap = new byte[nbPieces];
	for (int i = 0; i < this.nbPieces; i++) {
	    bufferMap[i] = 0;
	}
	this.sizePercent = 0;
	this.dataFileLock = new ReentrantReadWriteLock();
	this.downTime = 0;
	this.upTime = 0;
	this.downloadSpeed = 0;
	this.uploadSpeed = 0;
    }
    
    /**
     * <b>The CookieFile constructor.</b>
     * @param filename
     *        the name of the file.
     * @param size
     *        the size of the file in bytes.
     * @param pieceSize
     *        the size of a piece. This size is fixed at 2048 bytes.
     * @param key
     *        the hash of the file content whith MD5 function.
     * @param isComplete
     *        a boolean set to true is the file is complete, false if not.
     * @param bufferString
     *        a string representing a bufferMap.
     * @see CookieFile#mapToString()
     * @see CookieFile#stringToMap(String)
     */
    public CookieFile (String filename, int size, int pieceSize, String key, boolean isComplete, String bufferString){
	this.filename = filename;
	this.size = size;
	this.pieceSize = pieceSize;     
	this.isComplete = isComplete;
	this.key = key;
	this.nbPieces = (int)Math.ceil((float)size / pieceSize);
	this.bufferMap = this.stringToMap(bufferString);
	this.sizePercent = calcSize();
	this.dataFileLock = new ReentrantReadWriteLock();
	this.downTime = 0;
	this.upTime = 0;
	this.downloadSpeed = 0;
	this.uploadSpeed = 0;
    }

    /**
     * <b>The CookieFile constructor</b>
     * @param filename
     *        the name of the file.
     * @param pieceSize
     *        the size of a piece. This size if fixed at 2048 bytes.
     */
    public CookieFile (String filename, int pieceSize) {

	File file = new File("shared/"+filename);		
	this.filename = file.getName();
	this.size = (int)file.length();
	this.pieceSize = pieceSize;
	this.isComplete = true;
	this.nbPieces = (int)Math.ceil((float)size / pieceSize);

	MD5 md5 = new MD5(filename);
	this.key = md5.getHash();

	this.bufferMap = new byte[this.nbPieces];
	System.out.println("["+new Date()+"] ADD : new cookiefile : name="+filename+" | key="+key+" | size="+size+" | nbPieces="+nbPieces);
	for (int i = 0; i < this.nbPieces; i++) {
	    bufferMap[i] = 1;
	}
	this.sizePercent = size;
	this.dataFileLock = new ReentrantReadWriteLock();
	this.downTime = 0;
	this.upTime = 0;
	this.downloadSpeed = 0;
	this.uploadSpeed = 0;
    }

    /**
     * Getter to the ReadWriteLock of the file.
     * @return the lock.
     */
    public ReentrantReadWriteLock getLock() {
	return dataFileLock;
    }

    /**
     * Getter to the filename.
     * @return the filename
     */
    public String getFilename(){
	return filename;
    }

    /**
     * Getter to the piece size.
     * @return the piece size.
     */
    public int getPieceSize(){
	return pieceSize;
    }

    /**
     * Getter to the number of pieces in the file.
     * @return the the number of pieces in the file.
     */
    public int getNbPieces(){
	return nbPieces;
    }

    /**
     * Getter to the size of the file.
     * @return the size of the file.
     */
    public int getSize(){
	return size;
    }

    /**
     * Getter to the key of the file.
     * @return the key of the file.
     */
    public String getKey(){
	return key;
    }

    /**
     * Set the key .
     * @param key
     *        the key of the file.
     */
    public void setKey(String key) {
	this.key = key;
    }
    
    /**
     * Getter to the buffer map of the file.
     * @return the buffer map of the file.
     */
    public byte[] getMap() {
	return bufferMap;
    }

    /**
     * Getter to the buffer map of the file.
     * @return the buffer map of the file.
     */
    public byte[] getBufferMap() {
	return bufferMap;
    }

    /**
     * Check if the file is complete or not.
     * If isComlete is true the methode return true, else it looks over the bufferMap.
     * @return true if the file is complete, false if not.
     */
    public boolean isComplete() {
	if (isComplete) { 
	    return isComplete;
	} else {
	    for (int i = 0; i < nbPieces; i++){
		if (bufferMap[i] == 0)
		    return false;
	    }
	    isComplete = true;
	    downloadSpeed = 0;
	    return true;
	}
    }

    /**
     * Set the file complete.
     */
    public void setComplete() {
	isComplete = true;
    }

    /**
     * Convert a string representing a buffermap to an array of byte
     * @param bufferString
     *        A string representing a buffermap.
     * @return The array of byte corresponding.
     */
    public byte[] stringToMap (String bufferString) {
	byte[] bufferMap = new byte[nbPieces];

	for (int i = 0; i < nbPieces; i++) {
	    if (bufferString.charAt(i) == '1')
		bufferMap[i] = 1;
	    else
		bufferMap[i] = 0;
	}

	return bufferMap;
    }

    /**
     * Getter to the bufferMap, but converted in string.
     * @return a string representing the buffermap.
     */
    public String mapToString () {
	String bufferString = "";

	for(int i = 0; i < nbPieces; i++) {
	    if (bufferMap[i] == 1)
		bufferString = bufferString.concat("1");
	    else 
		bufferString = bufferString.concat("0");
	}

	return bufferString;
    }
	
    /**
     * Create a string representing a cookiefile, with the filename, the size, the piece size, and isComplete.
     * @return a string representing a cookiefile.
     */
    @Override
    public String toString() {	
	return "Filename: " + filename + ", Size: " + size + ", PieceSize: " + pieceSize + ", Key: " + key +", isComplet: " + isComplete;
    }

    /**
     * create a string to print a cookiefile with the filename, the size dans the key.
     * @return a string representing a cookiefile.
     */
    public String show() {
	return filename +" - Taille : " +size + "octets - Clé : "+ key;
    }

    /**
     * Compute the actual size of the file in percent.
     * @return the percent of the file already downloaded.
     */
    public float calcSize() {
	int actualSize = 0;
	for (int i = 0; i < nbPieces; i++){
	    if (bufferMap[i] == 1){
		actualSize += pieceSize;
	    }
	}
	return ((float)actualSize)/size;
    }

    /**
     * Getter to the percent of the file already download.
     * @return the percent of the file already download.
     */
    public int getSizePercent() {
	return (int)(this.sizePercent*100);
    }

    /**
     * Update the buffermap with the piece corresponding to the index. Update also the download speed of the file.
     * @param index
     *        the index of the piece downloaded.
     */
    public void setPieceToBuffer(int index) {
	if (bufferMap[index-1] == 0) {
	    bufferMap[index-1] = 1;
	    sizePercent += ((float)pieceSize)/size;
	    long now = System.currentTimeMillis();
	    long delta = now - downTime;
	    downloadSpeed = ((float)pieceSize)/delta;
	    downTime = now;
	}
    }

    /**
     * update the upload speed of the file
     * @param index 
     *        the index of the piece uploaded
     */
    public void setUploadPiece(int index) {
	if (index != nbPieces) {
	    long now = System.currentTimeMillis();
	    long delta = now - upTime;
	    uploadSpeed = ((float)pieceSize)/delta;
	    upTime = now;
	} else {
	    uploadSpeed = 0;
	}
    }
    
    /**
     * getter to the upload speed
     * @return the upload speed
     */
    public float getUploadSpeed() {
	return uploadSpeed;
    }
    
    /**
     * getter to the download speed
     * @return the download speed
     */
    public float getDownloadSpeed() {
	return downloadSpeed;
    }
}
