package Cookie;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

/**
 * <b>Treatment of the list request.</b>
 */
public class ListTreatment {
    
    /**
     * The list request parsed
     */
    private ListRequest lr;

    /**
     * A queue between the Userhandler thread and the trackerHandler thread
     */
    public BlockingQueue<CookieFile> q;

    /**
     * <b>The ListTreatment constructor</b>
     * @param lr
     *        the ListRequest instance.
     * @param q
     *        the blocking queue between the TrackerHandler thread and the UserHandler thread.
     */
    public ListTreatment(ListRequest lr, BlockingQueue<CookieFile> q) {
	this.lr = lr;
	this.q = q;
    }

    /**
     * This method treat the list request. Send the files in the list to the userHandler thread
     */
    public void Treat () {
	ArrayList<CookieFile> Files = lr.getFileList();
	CookieFile f = null;
	Iterator<CookieFile> it = Files.iterator();

	while (it.hasNext()) {
	    f = it.next();
	    // Send CookieFiles to the User Thread
	    try {
		q.put(f);
	    } catch (InterruptedException ie) {
		// DO something
	    }
	}
	// End of queue
	f = new CookieFile("END", 1, 1, "", true);
	try {
	    q.put(f);
	} catch (InterruptedException ie) {
	    // DO something
	}
    }
    

}
