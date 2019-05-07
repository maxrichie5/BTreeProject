import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GeneBankSearch {

static private Boolean cacheSearch = false;
	static private String bTreeFile = "";
	static private String queryFile = "";
	static private int cacheSizeSearch = 0;
	static private int debugLevelSearch = 0;
	
	public static void main(String[] args) {

		
		if (args.length > 5 || args.length < 3) {
			correctOutput();
		}
		if (args[0].equals("1")) {
			cacheSearch = true;
		} else if (!(args[0].equals("0"))) {
			correctOutput();
		}
		bTreeFile = args[1];
		queryFile = args[2];
		if (args.length > 3) {
			try {
				cacheSizeSearch = Integer.valueOf(args[3]);	
			}catch(Exception e) {
				correctOutput();				
			}
		}
		if (args.length > 4) {
			try {
				debugLevelSearch = Integer.valueOf(args[4]);	
			}catch(Exception e) {
				correctOutput();				
			}
		}
		
		try {
			search();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void correctOutput() {
		System.out.println( "CL arguments: <0/1(no/with Cache> <btree file> <query file> [<cache size>] [<debug level>]");
		System.exit(0);
	}
	
	private static void search() throws ClassNotFoundException, IOException {
		//Create file from query file
		File file = new File(queryFile);
		
		//Parse the file
		QueryParser parser = new QueryParser(file);
		
		//Get the string from the parser
		String[] geneStringArray = parser.getGeneStringArray();
		
		//Create GeneConverter
		GeneConverter gc = new GeneConverter();
		
		//Create BTree
		BTree bt = new BTree(bTreeFile, debugLevelSearch, cacheSizeSearch);
		
		//Create a dump for query results
		BufferedWriter bw = null;
		if(debugLevelSearch == 1) {
			String dumpFileName = queryFile+"_result";
			File dumpFile = new File(dumpFileName);
			if(!dumpFile.exists()) { 
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			bw = new BufferedWriter(new FileWriter(dumpFileName));
		}
		
		//search btree
		for (String queryString: geneStringArray) {
			queryString = queryString.toLowerCase();
			long key = gc.convertStringToLong(queryString);
			int frequency = bt.search(key);
			if(debugLevelSearch == 1) { //make query dump
				bw.write(queryString+": "+frequency+"\n");
			} else { //print query result to stdout
				System.out.println(queryString+": "+frequency);
			}
		}		
		
	}

}