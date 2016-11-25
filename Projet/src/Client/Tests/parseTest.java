import Cookie.ListRequest;
import Cookie.PeersRequest;
import Cookie.*;
import Cookie.Peer;
import Cookie.CookieFile;

import java.util.ArrayList;
import java.net.InetAddress;

class parseTest {

    public static void main (String[] args){

	String s = "list [file_a.dat 2097152 1024 8905e92afeb80fc7722ec89eb0bf0966 file_b.dat 3145728 1536 330a57722ec8b0bf09669a2b35f88e9e]";
	
	ListRequest l = new ListRequest(s);
	
	try {
	    l.Parse();
	}
	catch (BadRequestException bre){
	    assert false;
	}
	catch (LengthRequestException lre){
	    assert false;
	}
	
	CookieFile file1, file2;
	ArrayList<CookieFile> fileList = l.getFileList();
	assert fileList.size() == 2;
	file1 = fileList.get(0);
	file2 = fileList.get(1);


	assert file1.getFilename().equals("file_a.dat");
	assert file1.getSize() == 2097152;
	assert file1.getPieceSize() == 1024;
	assert file1.getKey().equals("8905e92afeb80fc7722ec89eb0bf0966");

	assert file2.getFilename().equals("file_b.dat");
	assert file2.getSize() == 3145728;
	assert file2.getPieceSize() == 1536;
	assert file2.getKey().equals("330a57722ec8b0bf09669a2b35f88e9e");

	System.out.println("Test Parse listRquest OK !");


	String s2 = "peers 8905e92afeb80fc7722ec89eb0bf0966 [1.1.1.2:2222 1.1.1.3:3333]";

	PeersRequest p = new PeersRequest(s2);

	try {
	    p.Parse();
	}
	catch (BadRequestException bre){
	    assert false;
	}
	catch (LengthRequestException bre){
	    assert false;
	}

	
	
	ArrayList<Peer> peerList = p.getPeersList();
	assert peerList.size() == 2;

	Peer peer1, peer2;
	peer1 = peerList.get(0);
	peer2 = peerList.get(1);

        InetAddress addr1 = peer1.getInetAddress();
	assert addr1.getHostAddress().equals("1.1.1.2");
	assert peer1.getPort() == 2222;

	InetAddress addr2 = peer2.getInetAddress();
	assert addr2.getHostAddress().equals("1.1.1.3");
	assert peer2.getPort() == 3333;

	System.out.println("Test Parse peersRequest OK !");

    }

}
