package Cookie;

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

/**
 * <b>Treatment of the getpieces request.</b>
 */
public class GetpiecesTreatment {

    /**
     * The path to the sheared folder
     */
    private String pathShared = "shared/";

    /**
     * the getpieces request parsed
     */
    private GetpiecesRequest gr;

    /**
     * The files in shared
     */
    private ArrayList<CookieFile> fileList;

    /**
     * A list of pieces
     */
    private ArrayList<Piece> pieceList;

    /**
     * The response to send
     */
    private String response;

    /**
     * <b>The GetpiecesTreatment constructor</b>
     * @param gr
     *        the GetpiecesRequest instance
     * @param files
     *        the files shared by the user
     */
    public GetpiecesTreatment (GetpiecesRequest gr, ArrayList<CookieFile> files) {
	this.gr = gr;
	this.fileList = files;
	this.pieceList = new ArrayList<Piece>();
    }


    /**
     * This method treat the getpieces request. Read the file and send to the client the pieces he wants.
     */
    public void Treat () {
	String key = gr.getKey();

	// Find the corresponding file with the key
	CookieFile file;
	Iterator<CookieFile> it = fileList.iterator();
	while (it.hasNext()) { 
	    CookieFile f = it.next();
	    if (f.getKey().equals(key)) {
		file = f;
		gr_read(file);
		// fill response "data key [index1:piece1 index2:piece2...]
		response = "data " + key + " [";
		Iterator<Piece> pieceIterator = pieceList.iterator();
		while(pieceIterator.hasNext()) {
		    Piece piece = pieceIterator.next();
		    response = response + Integer.toString(piece.getIndex()) + ":" + piece.getData() + " ";
		    file.setUploadPiece(piece.getIndex());
		}
		if(response.charAt(response.length() - 1) == ' ')
		    response = response.substring(0, response.length() - 1);
		response = response + "]\n";
		return;
	    }
	}
    }


    /**
     * Getter to the response
     * @return the response to send 
     */
    public String getMessage () {
	return response;
    }
    
    /**
     * Fill the pieceList reading the file at the indexes given by indexList
     * @param file
     *        the file to read
     */
    private void gr_read (CookieFile file) {
	try {
	    // Create the reader
	    RandomAccessFile dataFile = new RandomAccessFile(pathShared + file.getFilename(), "r");

	    int pieceSize = file.getPieceSize();
	    int pieceNb = file.getNbPieces();
	    ArrayList<Integer> indexes = gr.getIndexes();
	    Iterator<Integer> it = indexes.iterator();

	    // for each pieces
	    while(it.hasNext()) {
		Integer index = it.next();
		int nbBytesToRead = 0;

		// find the size of the read
		if (index.intValue() < pieceNb) {
		    nbBytesToRead = pieceSize;
		} else if (index.intValue() == pieceNb) {
		    nbBytesToRead = file.getSize() - (index.intValue() - 1)*pieceSize;
		} else {
		    System.err.println("["+new Date()+"] ERROR : Index out of bound");
		    return;
		}

		byte b[] = new byte[nbBytesToRead];	

		// Read the file at the correct offset
		dataFile.seek((long)pieceSize * (index - 1));
		if (dataFile.read(b, 0, nbBytesToRead) == -1){
		    System.err.println("["+new Date()+"] ERROR : Bad reading !");
		} else {
		    // Add the piece to the list
		    Piece piece = new Piece(index.intValue(), DatatypeConverter.printBase64Binary(b));
		    pieceList.add(piece);
		}
	    }
	    
	    // Close the reader
	    dataFile.close();
	} catch (EOFException eofe) {
	    System.err.println("["+new Date()+"] ERROR : Reach EOF : "+eofe.getMessage()); 
	} catch (IOException ioe) {
	    System.err.println("["+new Date()+"] ERROR : Error while reading file : "+ioe.getMessage()); 
	}
	
    }
}
