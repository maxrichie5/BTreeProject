import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Parses through the query file
 * 
 * @author maxrichmond
 *
 */
public class QueryParser {

	private String geneString;

	/**
	 * Sets up and completes the parsing for the file and adds the gene queries to a
	 * string
	 * Note: the geneString has queries separated by spaces
	 * @param file
	 *            The file to be parsed.
	 */
	public QueryParser(File file) {
		geneString = "";
		try {
			Scanner scan = new Scanner(file);

			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				geneString += line;
				geneString += " ";
			}
		} catch (FileNotFoundException e) {
			System.out.println("The file, " + file + ", cannot be found.");
		}

	}

	/**
	 * Gets the geneString generated from parsing the file and coverts it to an array
	 * 
	 * @return geneString as an array
	 */
	public String[] getGeneStringArray() {
		return geneString.split(" ");
	}

}
