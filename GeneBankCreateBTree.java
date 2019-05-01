import java.io.File;
import java.io.IOException;

public class GeneBankCreateBTree {

	static private Boolean cacheCreate = false;
	static private String gbkFile = "";
	static private int degree = 0;
	static private int sequenceLength = 0;
	static private int cacheSizeCreate = 0;
	static private int debug = 0;
	static private int optimal = 4096;
	
	public static void main(String[] args) {

		
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
			
			if (args.length > 4) {
				cacheSizeCreate = Integer.valueOf(args[4]);	
			}
			if (args.length > 5) {
				debug = Integer.valueOf(args[5]);	
				
			}
		}catch(Exception e) {
			correctOutput();				
		}
		
		if (degree == 0) {
			// there should be a call to optimum degree -- this should maybe happen in a separate class
		}
		
		try {
			createBTree();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	
	}
	
	public static void correctOutput() {
		System.out.println( "CL arguments: <0/1(no/with Cache> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]");
		System.exit(0);

	}
	
	public static void optimalDegree(int totalSize, int pointerSize, int metadata) {
		optimal -= metadata;
		optimal += totalSize;
		optimal -= pointerSize;
		int dividedBy = ((2*totalSize)+(2*pointerSize));
		optimal /= dividedBy;
		degree = optimal;
		
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
		BTree btree = new BTree(sequenceLength);
		
		//Insert into btree
		for (String subString: geneString) {
			for(int i = 0; i < (subString.length()-sequenceLength+1); i++) { //TODO Now it loops through all strings
				String seqString = subString.substring(i, sequenceLength+i); //Get string sequence
				long key = gc.convertStringToLong(seqString); //Convert string sequence to long
				TreeObject to = new TreeObject(key);
				btree.insert(to); //Insert the long key into the BTree
			}	
		}
	}
	
}
