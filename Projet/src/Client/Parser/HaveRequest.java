package Cookie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * <b>Parse of the have request.</b>
 */
public class HaveRequest extends  TrackRequest{
    
    /**
     * the file key
     */
    private String key;
    
    /**
     * The buffermap of the file
     */
    private byte[] bufferMap; 
    
    /**
     * <b>TheHaveReasuest constructor</b>
     * @param s
     *        the request
     */
    public HaveRequest(String s) {
	super(s);
    }
    
    /**
     * check the form of the request
     * @return true if the request is correct, false if is not
     */
    public boolean isRequestCorrect(){
	 if (stringSplit.length != 3)
	     return false;
	 return super.type().equals("have");
    }

    /**
     * getter to the file key
     * @return the file key
     */
    public String getKey() {
	return key;
    }

    /**
     * getter to the file buffermap
     * @return the buffermap
     */
    public byte[] getBufferMap() {
	return bufferMap;
    }

    /**
     * Read the input stream of the socket and parse the request. Parse the file key and the bufferMap
     */
    public void Parse(){
	if (isRequestCorrect()){
	    key = stringSplit[1];
	    bufferMap = stringSplit[2].getBytes();
	}
    }
}
