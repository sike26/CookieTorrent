package Cookie;
import java.util.Date;

import java.io.BufferedReader;

/**
 * <b>Abstract class of the parser.</b>
 */
abstract public class TrackRequest {

    /**
     * The request
     */
    private String string;

    /**
     * The request after a split(" ")
     */
    public String [] stringSplit;
    
    /**
     *<b>The TrackRequest constructor</b>
     * @param s
     *        the request
     */
    public TrackRequest(String s) {
	string = s;
	stringSplit = string.split(" ");
    }

    /**
     * <b>The TrackRequest constructor</b>
     * This constructor is use for the data request because it's impossible to read then like the others.
     * @param userInput
     *        the socket stream reader
     */
    public TrackRequest(BufferedReader userInput) {
	string = new String();
    }


    /**
     * Find the request type (have, list, peer, getfile, getpiece, data, ...)
     * @return the request type
     */
    public String type(){
	return stringSplit[0];
    }

    /**
     * Print the content of the request
     */
    public void Print (){
	for(int i = 0; i < stringSplit.length; i++){
	    System.out.println(stringSplit[i]);
	}
    }

    /**
     * Check if the string s il the first element of the request, ie just after the "[".
     * @param s
              element of the stringSplit attribut.
     * @return true if the string is the fisrt element of the request, false, if is not.
     */
    private boolean isFirstList(String s){
	return s.charAt(0) == '[';
    }

    /**
     * Check if the string s il the last element of the request, ie just before the "]".
     * @param s 
     *        element of the stringSplit attribut.
     * @return true if the string is the last element of the request, false, if is not.
     */
    private boolean isLastList(String s){
	return s.charAt(s.length()-1) == ']';
    }

    /**
     * Delete the first charecter of the string
     * @param s
     *        the string to modify
     * @return s without the first charecter
     */
    private String delFirst(String s){
	return s.substring(1);
    }

    /**
     * Delete the last charecter of the string
     * @param s
     *        the string to modify
     * @return s without the last charecter
     */
    private String delLast(String s){
	return s.substring(0, s.length()-1);
    }

    /**
     * Clean the string. If the string isFirstlist, this method remove the fist character. If the string 
     * isLastList, this method remove the Last character. 
     * @param s
     *        the string to modify
     * @return a clean string or s.
     */
    public String cleanString(String s){
	if (isFirstList(s))
	    s = delFirst(s);

	if (isLastList(s))
	    s = delLast(s);

	return s;
    }

    /**
     * Check the form of the request
     * @return true if the request is correct, false if is not
     * @throws BadRequestException
     *         if the request is not correct
     * @throws LengthRequestException
     *         if the request is not full
     */
    abstract public boolean isRequestCorrect() throws BadRequestException, LengthRequestException;
    
    /**
     * Read the input stream of the socket and parse the request.
     * @throws BadRequestException
     *         if the request is not correct
     * @throws LengthRequestException
     *         if the request is not full
     */
    abstract public void Parse() throws BadRequestException, LengthRequestException;

}

