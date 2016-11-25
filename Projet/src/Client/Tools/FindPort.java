package Cookie;
import java.io.*;
import java.net.*;
import java.util.Date;

/**
 * <b>This class is used to find a free port on the host.</b>
 */
public class FindPort {

    /**
     * the port of the host
     */
    public static int port;
 
    /**
     * <b>The FindPort constructor</b>
     */
    public FindPort(){
	port = -1;
    }

    /**
     * Find a free port on the host
     * @return a free port
     */
    public int findFreePort() {	
    int i = 1024;
	while (port == -1) {
	    Port p = new Port(i);
	    p.setDaemon(true);
	    p.start();
	    i++;		
	}
        return port;
    } 
}

class Port extends Thread {
    /**
     * a port
     */
    public static int _port = 0;
    
    /**
     * <b>the Port constructor</b>
     * @param port
     *        a port number
     */
    public Port(int port) {
	_port = port;
    }
    
    /**
     * Method run by the deamon
     */
    public void run() {
	try {
	    // If I can create a socket the port is free
	    Socket socket = new Socket("localhost", _port);
	    socket.close();
	} catch (IOException e) {
	    //else I try an other one
	}
	FindPort.port = _port;	    
    }
}
