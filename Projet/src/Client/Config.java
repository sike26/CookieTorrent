package Cookie;

import java.io.*;

import java.util.Date;

/**
 * <b>The Config class manage the configuration of the tracker address.</b>
 * <p> The class read the config.ini file, the configuration file on the application. 
 * The file have to be place in the main client folder.</p>
 */
public class Config {
    
    /**
     * The tracker ip address.
     */
    private String trackerAddress;

    /**
     * the tracker listening port.
     */
    private int trackerPort;
    
    /**
     * The config constructor.
     */
    public Config(){
	trackerAddress = "";
	trackerPort = 0;
	initConfig();
    }
    
    /**
     * Read the configuration file conf.ini.
     * The file have to be in the Client folder and have this form.
     * <code>
     * # Adresse IP du tracker
     * tracker-address = 127.0.0.1
     *
     * # Numéro de port TCP d'écoute du tracker
     * tracker-port = 8080
     * </code>
     */
    private void initConfig() {
	try {
	    BufferedReader br = new BufferedReader(new FileReader("config.ini"));
	    String[] line;
	
	    /// Address ///
	    br.readLine();
	    trackerAddress  = br.readLine().split(" = ")[1];
	    
	    /// Port ///
	    br.readLine();
	    br.readLine();
	    line = br.readLine().split(" = ");
	    trackerPort = Integer.parseInt(line[1]);
	} catch(IOException e){
	    System.out.println("["+new Date()+"] ERROR : Error while reading the configuration file");
	}
    }	
    
    /**
     * 
     * Get the tracker address from the configuration file.
     * @return the tracker ip address.
     * @see Config#trackerAddress
     */
    public String getTrackerAddress() {
	return trackerAddress;
    }
    
    /**
     * 
     * Get the tracker listening port from the configuration file.
     * @return the tracker listening port.
     * @see Config#trackerPort
     */
    public int getTrackerPort() {
	return trackerPort;
    }
    
}
