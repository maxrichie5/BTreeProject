import java.io.Serializable;

/**
 * Creates the tree object that contains the key and freq
 * @author thomasreinking
 *
 */
public class TreeObject implements Serializable{
	 
	private int freq;
	private long key;
	
	/**
	 * Creates a tree object with a given frequency and key
	 * Could be used for when a duplicate is found.
	 */
	public TreeObject(long key, int freq) {
		this.freq = freq;
		this.key = key;
	}
	
	/**
	 * Creates a tree object with only the key
	 * Used for when no duplicates is found
	 * @param key
	 */
	public TreeObject(long key) {
		this.key = key;
		freq = 1;
	}
	
	/**
	 * Increases the freq count 
	 */
	public void increaseFreq() {
		freq++;
	}
	
	/**
	 * Returns the freq of the object
	 */
	public int getFreq() {
		return freq;
	}
	/**
	 * Returns the key of 
	 * @return
	 */
	public long getKey() {
		return key;
	}
}
