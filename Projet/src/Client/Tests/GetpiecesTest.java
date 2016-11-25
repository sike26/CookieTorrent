import Cookie.GetpiecesRequest;
import Cookie.GetpiecesTreatment;
import Cookie.*;
import java.util.ArrayList;

class GetpiecesTest {

    public static boolean run () {
	// test du parser
	String s = "getpieces 8905e92afeb80fc7722ec89eb0bf0966 [1 2 3]";
	GetpiecesRequest gr = new GetpiecesRequest(s);
	try {
	    gr.Parse();
	} catch (BadRequestException e) {
	    assert false: "BadRequestException";
	} catch (LengthRequestException e) {
	    assert false: "LengthRequestException";
	}
	
	String key = gr.getKey();
	ArrayList<Integer> indexList = gr.getIndexes();
	
	assert key.equals("8905e92afeb80fc7722ec89eb0bf0966");
	assert indexList.get(0) == 1;
	assert indexList.get(1) == 2;
	assert indexList.get(2) == 3;
	assert indexList.size() == 3;

	System.out.println("GetpiecesRequest ok !");

	//test du traitement

	
	
	return true;
	    

    }
}
