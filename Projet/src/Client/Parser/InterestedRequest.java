package Cookie;
import java.util.Date;

/**
 * <b>Parse of the Interested request.</b>
 */
public class InterestedRequest extends TrackRequest {

    /**
     * The file key
     */
    private String key;

    /**
     * <b>The InterestedRequest constructor</b>
     * @param s
     *        the request
     */
    public InterestedRequest (String s){
	super(s);
    }

    /**
     * getter to the file key
     * @return the file key
     */
    public String getKey() {
	return key;
    }
    
    /**
     * check the form of the request
     * @return true if the request is correct, false if is not
     */
     public boolean isRequestCorrect(){
	 if (stringSplit.length != 2)
	     return false;
	 return super.type().equals("interested");
    }
    
    /**
     * Read the input stream of the socket and parse the request.
     */
    public void Parse() {
	if (isRequestCorrect()){
	    key = stringSplit[1];
	}
    }   
}
