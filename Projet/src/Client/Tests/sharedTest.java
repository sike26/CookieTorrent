import Cookie.SharedFiles;
import Cookie.CookieFile;
import java.util.*;

public class sharedTest {
		

		public void initTest(){

			ArrayList<CookieFile> files;
			SharedFiles shdTest = new SharedFiles();
			
			files = shdTest.getFiles();
			System.out.println(files);
		}


		public boolean addSharedFileTest(String filename, int size){

			CookieFile newFile = new CookieFile(filename, size);
			SharedFiles shdTest = new SharedFiles();

			shdTest.addSharedFile(newFile);

			ArrayList<CookieFile> files = shdTest.getFiles();
			Iterator<CookieFile> it = files.iterator();
			System.out.println(files);
 			
			while (it.hasNext()) {
				CookieFile file = it.next();
 	      		if(file.getFilename().equals(filename) && file.getPieceSize() == size) {
					return true;
 	      		}
			}

			return false;
		}

		public boolean delSharedFileTest(String filename){

			SharedFiles shdTest = new SharedFiles();

			shdTest.delSharedFile(filename);

			ArrayList<CookieFile> files = shdTest.getFiles();
			Iterator<CookieFile> it = files.iterator();
			System.out.println(files);
 			
			while (it.hasNext()) {
				CookieFile file = it.next();
 	      		if(file.getFilename() == filename) {
					return false;
 	      		}
			}

			return true;
		}
}