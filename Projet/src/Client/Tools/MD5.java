 package Cookie;
import java.security.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.io.File;
import java.util.Date;

/**
 * <b>Generate the MD5 hash of a file.</b>
 */
public class MD5
{
    /**
     * the hash of the file (the key)
     */
    private String hash;

    /**
     * the path the the shared folder
     */
    private String sharedPath = "./shared/";

    /**
     * <b>The MD5 constructor.</b> 
     * Compute the MD5 hash of the file.
     * @param filename
     *        the filename
     */
    public MD5(String filename) {

	try {
	    MessageDigest md5 = MessageDigest.getInstance("MD5");
	    File file = new File(sharedPath + filename);

	    //Flux
	    FileInputStream fileStream = new FileInputStream(file);
	    DigestInputStream dStream = new DigestInputStream(fileStream, md5);
		

	    //Create byte array to read data in chunks
	    byte[] byteArray = new byte[4096];
	    int bytesCount = 0; 

	    //Read file data and update in message digest
	    if(file.exists() && file.isFile() && file.canRead()) {
		while ((bytesCount = dStream.read(byteArray,0,4096)) != -1) {
		    md5.update(byteArray, 0, bytesCount);
		}
	    }

	    hash = (new HexBinaryAdapter()).marshal(md5.digest(byteArray));
	}
	catch (IOException e) {
	    System.err.println("["+new Date()+"] ERROR : IOException");		
	}
	catch (NoSuchAlgorithmException e) {
	    System.err.println("["+new Date()+"] ERROR : NoSuchAlgorithmException: MD5");		
	}
    }
    
    /**
     * Getter to the MD5 hash of the file
     * @return the MD5 hash of the file
     */
    public String getHash() {
	return hash;
    }
}

