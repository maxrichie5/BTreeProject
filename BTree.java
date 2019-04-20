import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.LinkedList;

public class BTree {
	
	private BTreeNode root; //The root node in this BTree
	private int t; //The degree of keys for this BTree
	private int sequenceLenth; //The number of genes per key in this BTree
	
	private static RandomAccessFile raf; //The file we are writing to and reading from
	private static int rafOffset = 0; //The position being read/written to in the raf
	private static int maxBTreeNodeSize = 4096; //The largest expected size in bytes of a BTree Node
	
	public BTree(int t, int sequenceLenth) {
		root = new BTreeNode();
		this.t = t;
		this.sequenceLenth = sequenceLenth;
	}
	
	public long search(long key) {
		//TODO
	}
	
	public long subSearch() {
		//TODO
	}
	
	public void insert(long key) {
		//TODO
	}
	
	private void split(BTreeNode parentNode, int childIndex, BTreeNode child) {
		//TODO
	}
	
	private void insertNonFull(BTreeNode parentNode, long key) {
		//TODO
	}
	
	/**
	 * Serializes the object by converting it into an array of bytes
	 * @param node The BTreeNode to be serialized
	 * @return The given BTreeNode as an array of bytes
	 * @throws IOException 
	 */
	private static byte[] serialize(BTreeNode node) throws IOException {
		//Array of bytes that store the given BTree Node as a sequence of bytes
		byte[] stream = null;
		
		//Creates a ByteArrayOutputStream to be passed as a parameter to the ObjectOutputStream
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		//Creates an ObjectOutputStream
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		
		//Writes the object to by converted into a byte array by the baos
		oos.writeObject(node);
		
		//Converts the written object into a byte array stored in stream
		stream = baos.toByteArray();
				
		//Returns the given node as an array of bytes
		return stream;
	}
	
	/**
	 * Deserializes an object by converting an array of bytes into a BTreeObject
	 * @param byteArray The given BTreeNode as a byte array
	 * @return The byte array converted back into a BTreeNode
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static BTreeNode deserialize(byte[] byteArray) throws ClassNotFoundException, IOException {
		//Creates a ByteArrayInputStream to make the given byte array readable
		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
		
		//Creates an ObjectInputStream to read the ByteArrayInputStream
		ObjectInputStream ois = new ObjectInputStream(bais);
		
		//Reads the given byteArray'd BTreeNode and converts it back into a BTreeNode
		return (BTreeNode) ois.readObject();
	}
	
	/**
	 * Writes a BTreeNode into a Random Access File in binary
	 * @param node The BTreeNode to be written
	 * @param position The offset in the RAF to write the BTreeNode to
	 * @throws IOException
	 */
	public static void diskWrite(BTreeNode node, int position) throws IOException {
		//Serializes the given BTreeNode
		byte[] byteArray = serialize(node);
		
		//Finds the position in the RandomAccessFile to write to
		raf.seek(position);
		
		//Writes the byte array to the RandomAccessFile
		raf.write(byteArray);
		
		//Changes the RandomAccessFile offset position to the next length of the BTreeNode
		rafOffset += maxBTreeNodeSize;
	}
	
	/**
	 * Reads the binary Random Access File and converts it into a BTreeNode
	 * @param position
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static BTreeNode diskRead(int position) throws ClassNotFoundException, IOException {
		//Creates byte array to be read to
		byte[] byteArray = new byte[maxBTreeNodeSize];
		
		//A BTreeNode place holder
		BTreeNode copyNode = null;
		
		//Finds the starting postion in the RAF to start reading from
		raf.seek(position);
		
		//Reads from the RAF into the byte array
		raf.read(byteArray);
		
		//Deserializes the byte array into the node place holder
		copyNode = deserialize(byteArray);
		
		//Returns the node place holder
		return copyNode;
	}
	
	
	private class BTreeNode implements Serializable {
		
		private LinkedList keys;			//array of keys for this node
		private LinkedList children;		//array of children for this node
		private boolean isLeaf;				//boolean to keep track of this node being a leaf
		
		/**
		 * Constructor to create BTreeNode and initialize variables
		 */
		public BTreeNode() {
			keys = new LinkedList<TreeObject>();
			children = new LinkedList<TreeObject>();
			isLeaf = true;
		}
		
		/**
		 * @return The list of keys
		 */
		public LinkedList<TreeObject> getKeys() {
			return keys;
		}
		
		
		/**
		 * @return The list of children
		 */
		public LinkedList<TreeObject> getChildren() {
			return children;
		}
		
		/**
		 * @return True if this node is a leaf node, false otherwise
		 */
		public boolean isLeaf() {
			return isLeaf;
		}
		
		/**
		 * Sets the isLeaf boolean to true or false.
		 * @param leafValue Boolean to set the leaf value to
		 */
		public void setLeaf(boolean leafValue) {
			isLeaf = leafValue;
		}
	}
} //End BTree
