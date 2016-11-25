package Cookie;
import Cookie.Piece;

import java.io.RandomAccessFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.DatatypeConverter;

/**
 * <b>Treatment of the data request.</b>
 */
public class DataTreatment {

    /**
     * a parsed data request 
     */
    private DataRequest data;

    /**
     * The cookiefile
     */
    private CookieFile f;

    /**
     * The path to the sheared folder
     */
    private String pathShared = "shared/";

    /**
     * An arrayList of pieces
     */
    private ArrayList<Piece> pieces;

    /**
     * The filename
     */
    private String filename;

    /**
     * The files in shared
     */
    private ArrayList<CookieFile> files;

    /**
     * The object for write in the file
     */
    private RandomAccessFile dataFile;

    /**
     * A lock to avoid concurency
     */
    private ReentrantReadWriteLock dataFileLock;

    /**
     * <b>The DataTreament constructor</b>
     * @param data
     *        the DataRequest instance
     * @param f
     *        the file to download
     * @param files
     *        the files shared by the user
     * @param dataFile
     *        the file writer
     */
    public DataTreatment (DataRequest data, CookieFile f, ArrayList<CookieFile> files, RandomAccessFile dataFile) {
	this.data = data;
	this.f = f;
	this.pieces = data.getPieces();
	this.filename = data.getFilename();
	this.files = files;
	this.dataFile = dataFile;
	this.dataFileLock = dataFileLock;
    }

    /**
     * This method treat the resquest data. Write the pieces in the file with the correct offset.
     */
    public void Treat () {
	try {
	    int pieceLength = this.data.getPieceLength();
	    
	    // Take the writelock
	    f.getLock().writeLock().lock();
	    
	    //For each piece in the request
	    Iterator<Piece> it = pieces.iterator();
	    while (it.hasNext()) { 
		Piece p = it.next();;
	        
		// Write the piece in the file, with the correct offset
		dataFile.seek(pieceLength*(p.getIndex()-1));
		dataFile.write(DatatypeConverter.parseBase64Binary(p.getData()));

		// Update the file bufferMap
		f.setPieceToBuffer(p.getIndex());
		dataFile.seek(0);
	    }
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	} finally {
	    // release the lock
	    f.getLock().writeLock().unlock();
	}
    }
}
