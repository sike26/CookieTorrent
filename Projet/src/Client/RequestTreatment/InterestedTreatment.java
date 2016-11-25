package Cookie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

/**
 * <b>Treatment of the Interested request.</b>
 */
public class InterestedTreatment {

    /**
     * The insterested request parsed
     */
    private InterestedRequest ir;

    /**
     * The path to the shared folder
     */
    private String pathSharedState = "./Shared.state";

    /**
     * The files in shared
     */
    private ArrayList<CookieFile> files;

    /**
     * The response to send
     */
    private String response;
    

    /**
     * <b>The InterestedTreatment constructor</b>
     * @param ir
     *        the InterestedRequest instance
     * @param files
     *        the files shared by the user
     */
    public InterestedTreatment (InterestedRequest ir, ArrayList<CookieFile> files) {
	this.ir = ir;
	this.files = files;
	this.response = "";
    }


    /**
     * This method treat th interested request. Send in response the buffermap of the corresponding file.
     */
    public void Treat () {
	String key = ir.getKey();
	
	// Search the file in the files in sheared
	Iterator<CookieFile> it = files.iterator();
	while (it.hasNext()) { 
	    CookieFile f = it.next();
	    if (f.getKey().equals(key)) {
		String encodeBufferMap = new String(f.getMap());
		// Form the response
		this.response = "have "+f.getKey()+" "+encodeBufferMap+"\n";
		return;
	    }
	}		
    }

    /**
     * Getter to the response
     * @return the response to send
     */
    public String getMessage() {
	return response;
    }

}
