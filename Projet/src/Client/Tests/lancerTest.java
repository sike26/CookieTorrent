class lancerTest {


	public static void main(String[] args) {
	        GetpiecesTest.run();
		System.out.println("Test Getpieces OK !");

		sharedTest tst = new sharedTest();

		tst.initTest();

		boolean tmp = tst.addSharedFileTest("test.txt", 2048); 
		assert  tmp == true;
		System.out.println("Test addSharedFileTest OK !");

		boolean tmp2 = tst.delSharedFileTest("test.txt");
		assert  tmp == true;
		System.out.println("Test delSharedFileTest OK !");


	}

}
