package Cookie;

@SuppressWarnings("serial") 
/**
 * <b>The resquest doesn't have the good form.</b>
 */
public class BadRequestException extends Exception {
    public BadRequestException(String message){
	super(message);
    }  
}
