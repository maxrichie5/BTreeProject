
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
	}
	
	public static void correctOutput() {
		System.out.println( "CL arguments: <0/1(no/with Cache> <btree file> <query file> [<cache size>] [<debug level>]");
		System.exit(0);
	}

}