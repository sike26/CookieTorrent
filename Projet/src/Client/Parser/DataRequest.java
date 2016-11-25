package Cookie;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.util.Iterator;
import java.io.IOException;
import java.util.Date;

/**
 * <b>Parse of the data request.</b>
 */
public class DataRequest extends TrackRequest {

    /**
     * The file key
     */
    private String key;
    
    /**
     * the pieces received
     */
    private ArrayList<Piece> pieces;

    /**
     * The input stream of the client soket
     */
    private BufferedReader userInput;

    /**
     * The length of a piece
     */
    private int pieceLength;

    /**
     * the files in shared
     */
    private ArrayList<CookieFile> files;

    /**
     * The file in download
     */
    private CookieFile file;
    
    /**
     * <b>DataRequest constructor</b>
     * @param userInput 
     *        The client socket strem reader.
     * @param files
     *        The files shared by the user.
     */
    public DataRequest (BufferedReader userInput, ArrayList<CookieFile> files){
	super(userInput);
	this.userInput = userInput;
	this.pieceLength = 2048;
	pieces = new ArrayList<Piece>();
	this.files = files;
    }

    /**
     * getter to the file key
     * @return the file key
     */
    public String getKey() {
	return key;
    }
    
    /**
     * getter to the pieces array
     * @return the array of pieces
     */
    public ArrayList<Piece> getPieces() {
	return pieces;
    }

    /**
     * getter to the filename
     * @return the filename
     */
    public String getFilename() {
	return file.getFilename();
    }

    /**
     * getter to the piece length
     * @return the piece length
     */
    public int getPieceLength() {
	return pieceLength;
    }

    /**
     * check the form of the request
     * @return true if the request is correct, false if is not
     * @deprecated Unused here
     */
    @Deprecated
    public boolean isRequestCorrect(){
	return true;
    }

    /**
     * Read the input stream of the socket and parse the request. Parse the file key, and the pieces 
     * send and the index of each pieces.
     * @throws BadRequestException
     *         if the request is not correct
     * @throws LengthRequestException
     *         if the request is not full
     */
    public void Parse() 
	throws BadRequestException, LengthRequestException {

	// Reading the first word of the request
	StringBuilder command = new StringBuilder();
	int c = 0;
	try {
	    while((c = userInput.read()) != -1) {
		if ((char)c == ' ')
		    break;
		command.append((char)c);
	    }
	} catch (IOException e) {
	    System.err.println("["+new Date()+"] ERROR : Error while reading the input stream of the client socket :" + e.getMessage());
	}	

	// Read the file key
	StringBuilder key = new StringBuilder();
	try {
	    while((c = userInput.read()) != -1) {
		if ((char)c == ' ')
		    break;
		key.append((char)c);
	    }
	} catch (IOException e) {
	    System.err.println("["+new Date()+"] ERROR : Error while reading the input stream of the client socket :" + e.getMessage());
	}

	// Bad key
	if(key.toString().length() != 32){
	    throw new BadRequestException("Wrong key length");
	}
	this.key = key.toString();

	// Search the file in the files in shared
	Iterator<CookieFile> it = files.iterator();
	while (it.hasNext()) { 
	    CookieFile f = it.next();

	    if (f.getKey().equals(this.key)) {
		f.toString();
		this.file = f;
		break;
	    }
	}

	// check the first "["
	try {
	    c = userInput.read();
	    if( (char)c != '[')
		throw new BadRequestException("Parser unable to understand this request");
	} catch (IOException e) {
	    System.err.println("["+new Date()+"] ERROR : Error while reading the input stream of the client socket :" + e.getMessage());
	}

	// get the pieces
	while(true) {
	    int index;
	    String data;
	    try {
		// get the index
		StringBuilder sbIndex = new StringBuilder();
		int ck;
		while((ck = userInput.read()) != -1) {
		    if ((char)ck == ':')
			break;
		    sbIndex.append((char)ck);
		}
		index = Integer.parseInt(sbIndex.toString(), 10);

		// get the pieces
		if (index != file.getNbPieces()) {
		    char dataBuffer[] = new char[2732/*pieceLength*/];
		    userInput.read(dataBuffer, 0, 2732/*pieceLength*/);

		    data = new String(dataBuffer);
		    Piece p = new Piece(index, data);
		    pieces.add(p);
		    if ((ck = userInput.read()) != -1){
			if((char)ck == ']'){
			    return;
			}
			else if ((char)ck != ' '){
			    throw new BadRequestException("Parser unable to understand this request");
			}
		    }
		}
		// The last piece
		else { 

		    StringBuilder lastPiece = new StringBuilder();
		    while((ck = userInput.read()) != -1) {
			if ((char)ck == ']')
			    break;
			lastPiece.append((char)ck);
		    }

		    data = lastPiece.toString();
		    Piece p = new Piece(index, data);
		    pieces.add(p);
		    return;
		}
	    } catch (IOException e) {
		System.err.println("read:"+ e.getMessage());
	    }
	}
    }
}
