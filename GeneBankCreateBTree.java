import java.io.File;
import java.io.IOException;

public class GeneBankCreateBTree {

	static private Boolean cacheCreate = false;
	static private String gbkFile = "";
	static private int degree = 0;
	static private int sequenceLength = 0;
	static private int cacheSizeCreate = 0;
	static private int debug = 2;
	
	public static void main(String[] args) {

		long startTime = System.nanoTime();
		
		if (args.length > 6 || args.length < 4) {
			correctOutput();
		}
		if (args[0].equals("1")) {
			cacheCreate = true;
		} else if (!(args[0].equals("0"))) {
			correctOutput();
		}
		gbkFile = args[2];
		try {
			degree = Integer.valueOf(args[1]);
			sequenceLength = Integer.valueOf(args[3]);
			
			if (args.length > 4 && cacheCreate) {
				cacheSizeCreate = Integer.valueOf(args[4]);	
			}
			if (args.length > 4 && !cacheCreate) {
				debug = Integer.valueOf(args[4]);	
			}
			if (args.length > 5 && !cacheCreate) {
				correctOutput();
			}
			if (args.length > 4 && args.length < 6 && cacheCreate) {
				cacheSizeCreate = Integer.valueOf(args[4]);	
			}
			if(args.length > 5 && cacheCreate) {
				cacheSizeCreate = Integer.valueOf(args[4]);	
				debug = Integer.valueOf(args[5]);
			}
			if(args.length > 5 && !cacheCreate) {
				correctOutput();
			}
			if ((debug != 0 && debug != 1) || debug == 2) {
				debug = 1;
			}
			
			
		}catch(Exception e) {
			correctOutput();				
		}
		
		try {
			createBTree();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("RUN UNSUCCESSFUL");
			System.exit(0);
		}
	
		long endTime   = System.nanoTime();
		long totalTime = endTime - startTime;
		double seconds = totalTime/1000000000.0;
		double minutes = seconds/60;
		System.out.println("Runtime:");
		System.out.println("Seconds: "+seconds);
		System.out.println("Minutes: "+minutes);
		
		System.out.println("\nRun Successful.");
	}
	
	public static void correctOutput() {
		System.out.println( "CL arguments: <0/1(no/with Cache> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]");
		System.exit(0);

	}
	
	public static int getDebug() {
		return debug;
	}
	
	public static int getDegree() {
		return degree;
	}
	
	public static void createBTree() throws ClassNotFoundException, IOException {
		//Create file from gbk
		File file = new File(gbkFile);
		
		//Parse the file
		Parser parser = new Parser(file);
		
		//Get the string from the parser
		String[] geneString = parser.getSubGeneStrings();
		
		//Create GeneConverter
		GeneConverter gc = new GeneConverter();
		
		//Create BTree
		BTree btree = new BTree(sequenceLength, cacheSizeCreate, degree, debug, gbkFile); // change
		
		//Insert into btree
		for (String subString: geneString) {
			for(int i = 0; i < (subString.length()-sequenceLength+1); i++) { //TODO Now it loops through all strings
				String seqString = subString.substring(i, sequenceLength+i); //Get string sequence
				long key = gc.convertStringToLong(seqString); //Convert string sequence to long
				TreeObject to = new TreeObject(key);
				if(i == 0) to.setSeqLen(sequenceLength);
				btree.insert(to); //Insert the long key into the BTree
			}	
		}
		btree.finish(); //tells btree you are done using it and write root node

		//make dump if debug 1
		if(debug == 1) {
			try {
				btree.makeDump();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
