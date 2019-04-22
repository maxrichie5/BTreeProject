
public class GeneBankCreateBTree {

	static private Boolean cacheCreate = false;
	static private String gbkFile = "";
	static private int degree = 0;
	static private int sequenceLength = 0;
	static private int cacheSizeCreate = 0;
	static private int debugLevelCreate = 0;
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
				debugLevelCreate = Integer.valueOf(args[5]);	
				
			}
		}catch(Exception e) {
			correctOutput();				
		}
		
		if (degree == 0) {
			// there should be a call to optimum degree -- this should maybe happen in a separate class
		}
	
	
	}
	
	public static void correctOutput() {
		System.out.println( "CL arguments: <0/1(no/with Cache> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]");
		System.exit(0);

	}
	
	public static void optimumDegree(int totalSize, int pointerSize, int metadata) {
		optimal -= metadata;
		optimal += totalSize;
		optimal -= pointerSize;
		int dividedBy = ((2*totalSize)+(2*pointerSize));
		optimal /= dividedBy;
		
	}
	
}
