package Cookie;

import java.util.ArrayList;
import java.util.Date;

/**
 * <b>Parse of the list request.</b>
 */
public class ListRequest extends TrackRequest {

    /**
     * the list of cookiefile received
     */
    private ArrayList<CookieFile> fileList;
    
    /**
     * <b>The ListRequest constructor</b>
     * @param s
     *        the request
     */
    public ListRequest (String s) {
	super(s);
	fileList = new ArrayList<CookieFile>(1);
    }
    
    /**
     * Getter to the array of file
     * @return the array of file received
     */
    public ArrayList<CookieFile> getFileList(){
	return fileList;
    }

    /**
     * check the form of the request
     * @return true if the request is correct, false if is not
     * @throws BadRequestException
     *         if the request is not correct
     * @throws LengthRequestException
     *         if the request is not full
     */
    public boolean isRequestCorrect() 
	throws BadRequestException, LengthRequestException {

	if (this.stringSplit.length == 2 && stringSplit[1].equals("[]"))
	    return false;

	String last = this.stringSplit[this.stringSplit.length-1];
	
	if (!super.type().equals("list") || this.stringSplit[1].charAt(0) != '['){
	    throw new BadRequestException("Parser unable to understand this request"); 
	}        
	if ((this.stringSplit.length-1)%4 != 0 || ((this.stringSplit.length-1)%4 != 0 && last.charAt(last.length()-2) != ']')){
	    throw new LengthRequestException();
	}
		
	return true;

	
    }

     /**
     * Read the input stream of the socket and parse the request. Parse the filename, pieceSize, 
     * the key for each file in the list request.
     * @throws BadRequestException
     *         if the request is not correct
     * @throws LengthRequestException
     *         if the request is not full
     */
    public void Parse() throws BadRequestException, LengthRequestException {
	if (isRequestCorrect()){

	    String filename;
	    int pieceSize;
	    int size;
	    String key;
	    int i = 1;
	    while (i < stringSplit.length){
		
		filename = cleanString(stringSplit[i]);
		size =  Integer.parseInt(stringSplit[i+1]);
		pieceSize =  Integer.parseInt(stringSplit[i+2]);
		key = cleanString(stringSplit[i+3]);
		
		CookieFile f = new CookieFile(filename, size, pieceSize, key, false);
		f.setKey(key);
		
		fileList.add(f);
		i += 4;
	    }   
	}
    }

}

