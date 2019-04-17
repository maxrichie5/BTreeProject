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
						/* Found a gene block */
						String newLine = scan.nextLine();
						if (!newLine.contains("//")) {
							if (newLine.contains("N") || newLine.contains("n")) {
								/* N is in this new line, must move subseq to the last available spot */
								boolean foundN = false;
								int nSpot = 0;
								for (int i = 0; i < newLine.length(); i++) {
									if (!foundN) {
										/* Searching for n */
										if (newLine.charAt(i) == 'N' || newLine.charAt(i) == 'n') {
											/* Found the n, update nSpot */
											foundN = true;
											nSpot = i;
										} else {
											if (Character.isLetter(newLine.charAt(i)))
												GeneString += newLine.charAt(i);
										}
									}
								}
								if (foundN) {
									/* An N was found in this line, must start new subseq after last N */
									if (nSpot != 10)
										/* At n at index 10 means that the first char of subseq is n */
										GeneString += "\n";

									for (int i = nSpot; i < newLine.length(); i++) {
										if (newLine.charAt(i) != 'N' && newLine.charAt(i) != 'n'
												&& newLine.charAt(i) != ' ') {
											GeneString += newLine.charAt(i);
										}
									}
								}
							} else {
								/*
								 * No n in this line, just replace all non allowable chars and add to geneString
								 */
								GeneString += newLine.replaceAll("[^AaGgTtCc]", "");
							}
						} else {
							geneBlock = false;
							GeneString += "\n"; /*
												 * If the file contains more than one gene block then they will be
												 * separated
												 */
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

	/**
	 * Splits the geneStrings into the correct Sub sequences
	 * 
	 * @return An array of String[] that has all the sub sequences
	 */
	public String[] getSubGeneStrings() {
		return GeneString.split("\\r?\\n");
	}


}
