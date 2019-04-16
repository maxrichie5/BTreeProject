import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Parses through the .gbk file
 * 
 * @author thomasreinking
 *
 */
public class Parser {

	private String GeneString;

	/**
	 * Sets up and completes the parsing for the file and adds the gene block to a
	 * string
	 * 
	 * @param file
	 *            The file to be parsed.
	 */
	public Parser(File file) {
		GeneString = "";
		try {
			Scanner scan = new Scanner(file);

			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (line.contains("ORIGIN")) {
					boolean geneBlock = true;
					while (geneBlock) {
						String newLine = scan.nextLine();
						if (!newLine.contains("//")) {
							GeneString += newLine.replaceAll("[^AaGgTtCc]", "");
							// replaceAll makes sure that only the bases are added, if an n is found that is
							// it not put into the string
						} else {
							geneBlock = false;
							GeneString += "\n"; // If the file contains more than one gene block then they will be
												// separated
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("The file, " + file + ", cannot be found.");
		}

	}

	/**
	 * Gets the geneString generated from parsing the file
	 * 
	 * @return geneString of the file
	 */
	public String getGeneString() {
		return GeneString;
	}
}
