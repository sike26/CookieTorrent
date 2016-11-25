package Cookie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * <b>Parse of the getpieces request.</b>
 */
public class GetpiecesRequest extends  TrackRequest{
    
    /**
     * The file key
     */
    private String key;

    /**
     * Array of piece index
     */
    private ArrayList<Integer> indexList;
    
    /**
     * <b>The GetpiecesRequest constructor</b>
     * @param s
     *        the resquest in a string format
     */
    public GetpiecesRequest(String s) {
	super(s);
	indexList = new ArrayList<Integer>();
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
	    if (stringSplit.length < 3)
		return false;
	    if (this.stringSplit.length == 3 && stringSplit[2].equals("[]"))
		return false;
	    String last = this.stringSplit[this.stringSplit.length-1];
	    
	    if (!super.type().equals("getpieces") || this.stringSplit[2].charAt(0) != '['){
		throw new BadRequestException("Parser unable to understand this request"); 
	    }        
	    if (last.charAt(last.length()-1) != ']')
		throw new LengthRequestException();
		
	return true;
    }
    
    /**
     * getter to the file key
     * @return the file key
     */
    public String getKey() {
	return key;
    }

    /**
     * getter to the array of index
     * @return the array of index
     */
    public ArrayList<Integer> getIndexes() {
	return indexList;
    }

    /**
     * Read the input stream of the socket and parse the request. Parse the file key and the index of each pieces.
     * @throws BadRequestException  
     *         if the request is not correct
     * @throws LengthRequestException
     *         if the request is not full
     */
    public void Parse() throws BadRequestException, LengthRequestException{
	if (isRequestCorrect()){
	    key = stringSplit[1];
	    int i = 2;
	    while (i < stringSplit.length){
		Integer index = Integer.parseInt(cleanString(stringSplit[i]));
		System.out.println(index.toString());
		indexList.add(index);
		i++;
	    }
	}
    }
}
