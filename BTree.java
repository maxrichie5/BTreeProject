import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.LinkedList;

public class BTree {

	private BTreeNode root, currentNode, nextNode; //The root, current, next node in this BTree
	private int sequenceLenth; //The number of genes per key in this BTree

	private static RandomAccessFile raf; //The file we are writing to and reading from
	private static int rafOffset = 0; //The position being read/written to in the raf
	private static int maxBTreeNodeSize = 4096; //The largest expected size in bytes of a BTree Node
	private static int debugLevel;
	private static int degree;
	private static int nodeCount;
	private static int cacheSize;


	public BTree(int totalSize, int pointerSize, int metadata, int sequenceLenth, int cacheSize, int degree, int debugLevel) {
		if (degree == 0) {
			degree = GeneBankCreateBTree.getOptimalDegree();
		} else {
			this.degree = degree;
		}
		this.debugLevel = debugLevel;
		root = new BTreeNode(degree, true, true, 0);
		currentNode = new BTreeNode();
		nextNode = new BTreeNode();
		this.sequenceLenth = sequenceLenth;
		nodeCount = 1;
	}

	public long search(long key) throws ClassNotFoundException, IOException {
		while (true) {
			int i = 0;
			while (i < currentNode.keys.size() && Long.compare(key, currentNode.keys.get(i).getKey()) > 0){
				i++;
			}

			if (i < currentNode.keys.size() && Long.compare(key, currentNode.keys.get(i).getKey()) == 0)
			{
				return currentNode.keys.get(i).getFreq();
			}

			if (currentNode.isLeaf()) {
				return 0;
			} else {
				currentNode = diskRead(currentNode.children.get(i));
			}
		}
	}

	public void insert(TreeObject to) throws ClassNotFoundException, IOException {
		Long key = to.getKey();

		int maxAllowedKeys = degree;
		BTreeNode oldRoot = root;

		if(root.isFull()) { //root node is full
			nextNode = root; //create node to be parent of root after split
			root = new BTreeNode(degree, false, true, nodeCount+1); //make newParent the root
			nodeCount++;
			root.children.add(0, nextNode.getIndex());
			nextNode.setParentIndex(root.getIndex());
			nextNode.setRoot(false);
			
			
			split(root, 0, nextNode); /*pls explain what OLDROOT is i am confused so i changed it*/

		} else { //root node is not full
			insertNonFull(to);
		}

	}

	private void split(BTreeNode parentNode, int childIndex, BTreeNode child) {
		BTreeNode newNode = new BTreeNode(degree, false, child.isLeaf(), nodeCount);
		nodeCount++;		

		for(int j = 1; j <= (degree-1); j++) { //half the full node's keys
			newNode.addKey(child.getKey(j+degree));
		}

		if(!child.isLeaf()) { //if child is not a leaf
			for(int j = 1; j <= degree; j++) {
				newNode.addChild(child.getChild(j+degree), j);
			}
		}

		for(int j = parentNode.getNumKeys()+1; j >= 1; j--) {//reindex children
			parentNode.addChild(parentNode.getChild(j), j+1);
		}
		parentNode.addChild(newNode.getOffset());

		for(int j = parentNode.getNumKeys(); j >= 1/*newNode*/; j--) { //reindex keys
			parentNode.addKey(parentNode.getKey(j), j+1);
		}
		parentNode.addKey(child.getKey(degree), i/*idk*/);
		parentNode.setNumKeys(parentNode.getNumKeys()+1);

		diskWrite(parentNode, parentNode.getOffset());
		diskWrite(child, child.getOffset());
		diskWrite(newNode, newNode.getOffset());

	}

	private void insertNonFull(TreeObject to) throws IOException, ClassNotFoundException {
		Long key = to.getKey();
		currentNode = root;

		while (true)
		{
			int index = currentNode.keys.size() - 1;
			if (currentNode.isLeaf())
			{
				while (index >= 0 && Long.compare(key, currentNode.keys.get(index).getKey()) <= 0)
				{
					if (Long.compare(key, currentNode.keys.get(index).getKey()) == 0)
					{
						currentNode.keys.get(index).increaseFreq();
						diskWrite(currentNode, currentNode.getOffset());
						if(debugLevel == 0)
							System.err.println();
						return;
					}
					index--;
				}
				currentNode.keys.add(index + 1, to);
				if(debugLevel == 0)
					System.err.println();

				diskWrite(currentNode, currentNode.getOffset());
				break;
			}
			else
			{
				while (index >= 0 && Long.compare(key, currentNode.keys.get(index).getKey()) <= 0)
				{
					if (Long.compare(key, currentNode.keys.get(index).getKey()) == 0)
					{
						currentNode.keys.get(index).increaseFreq();
						diskWrite(currentNode, currentNode.getOffset());

						if(debugLevel == 0)
							System.err.println();

						return;
					}
					index--;
				}
				index++;
				nextNode = diskRead(currentNode.children.get(index));
				if (nextNode.isFull())
				{
					split(currentNode, index, nextNode);
					if (Long.compare(key, currentNode.keys.get(index).getKey()) == 0)
					{
						currentNode.keys.get(index).increaseFreq();
						diskWrite(currentNode, currentNode.getOffset());
						if(debugLevel == 0)
							System.err.println();

						return;
					} else if (Long.compare(key, currentNode.keys.get(index).getKey()) > 0)
						nextNode = diskRead(currentNode.children.get(index + 1));
				}
				currentNode = nextNode;
			}
		}
	}
	/**
	 * returns length of a long
	 * @param l is the long you want the length of 
	 * @return
	 */
	private static int getLongLength(long l) { 
		String s = ""+l;
		return s.length();
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

	/**
	 * Creates a BTreeNode with keys, children, and other methods.
	 * 
	 * @author thomasreinking, maxrichmond
	 *
	 */
	private class BTreeNode implements Serializable {

		private LinkedList<TreeObject> keys; // array of keys for this node
		private LinkedList<Integer> children; // array of children for this node
		private boolean isLeaf,isRoot; // boolean to keep track of this node being a leaf/root
		private int numKeys; // number of keys in this node
		private int parentIndex, index; // index for parent and this node
		private int offset; //
		private int degree; // b tree degree
		/*please explain offset above ^^*/

		/**
		 * Constructor to create BTreeNode and initialize variables
		 */
		public BTreeNode(int degree, boolean isRoot, boolean isLeaf, int index) {
			keys = new LinkedList<TreeObject>();
			children = new LinkedList<Integer>();
			
			this.degree = degree;
			this.isRoot = isRoot;
			this.isLeaf = isLeaf;
			this.index = index;
			
			numKeys = 0;
			
			if (isRoot) {
				parentIndex = -1; // To indicate that it has no parent for now
			}
			
			offset = 0;
		}

		/**
		 * check if node is full
		 * 
		 * @return
		 */
		public boolean isFull() {
			return keys.size() == numKeys;
		}


		/**
		 * 
		 * @return Number of Keys in this BTreeNode
		 */
		public int getNumKeys() {
			return numKeys;
		}

		/**
		 * Sets the number of keys in this BTreeNode
		 * 
		 * @param numKeys
		 */
		public void setNumKeys(int numKeys) {
			this.numKeys = numKeys;
		}

		/**
		 * Gets the index of parent
		 * 
		 * @return Parent Index
		 */
		public int getParentIndex() {
			return parentIndex;
		}

		/**
		 * Sets the index of parent
		 * 
		 * @param parent
		 */
		public void setParentIndex(int parentIndex) {
			this.parentIndex = parentIndex;
		}
		

		/**
		 * Gets the index of node
		 * 
		 * @return Parent Index
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * Sets the index of node
		 * 
		 * @param parent
		 */
		public void setIndex(int index) {
			this.index = index;
		}

		/**
		 * Gets the key of TreeObject for that index
		 * 
		 * @param key
		 *            Index of key
		 * @return TreeObject of that index
		 */
		public TreeObject getKey(int key) {
			return keys.get(key);
		}

		/**
		 * @return The list of keys
		 */
		public LinkedList<TreeObject> getKeys() {
			return keys;
		}

		/**
		 * Removes the key at index
		 * 
		 * @param index
		 *            Index of key to be removed
		 * @return The removed TreeObject
		 */
		public TreeObject removeKey(int index) {
			return keys.remove(index);
		}

		/**
		 * Adds a key to the key list
		 * 
		 * @param key
		 *            Element to be added
		 */
		public void addKey(TreeObject key) {
			keys.add(key);
		}

		/**
		 * Adds a key to the key list to given index
		 * 
		 * @param key
		 *            Key to be added
		 * @param index
		 *            Index to be added to it
		 */
		public void addKey(TreeObject key, int index) {
			keys.add(index, key);
		}

		/**
		 * Adds a child to the list
		 * 
		 * @param child
		 *            Child to be added
		 */
		public void addChild(int child) {
			children.add(child);
		}

		/**
		 * Adds a child to the given index
		 * 
		 * @param child
		 *            Child to be add
		 * @param index
		 *            Index to add child at
		 */
		public void addChild(Integer child, int index) {
			children.add(index, child);
		}

		/**
		 * Gets the child of the given index
		 * 
		 * @param index
		 *            Index of child
		 * @return The value of the int at that index
		 */
		public int getChild(int index) {
			return children.get(index).intValue();
		}

		/**
		 * Removes the child at the index
		 * 
		 * @param index
		 *            The index of child
		 * @return The removed child value at that index
		 */
		public int removeChild(int index) {
			return children.remove(index);
		}

		/**
		 * @return The list of children
		 */
		public LinkedList<Integer> getChildren() {
			return children;
		}

		/**
		 * @return True if this node is a leaf node, false otherwise
		 */
		public boolean isLeaf() {
			return isLeaf;
		}
		
		/**
		 * @return True if this node is a root node, false otherwise
		 */
		public boolean isRoot() {
			return isRoot;
		}
		
		/**
		 * Sets the isRoot boolean to true or false.
		 * 
		 * @param leafValue
		 *            Boolean to set the leaf value to
		 */
		public void setRoot(boolean isRoot) {
			this.isRoot = isRoot;
		}

		/**
		 * Sets the isLeaf boolean to true or false.
		 * 
		 * @param leafValue
		 *            Boolean to set the leaf value to
		 */
		public void setLeaf(boolean leafValue) {
			isLeaf = leafValue;
		}

		/**
		 * Gets the offset.
		 */
		public int getOffset() {
			return offset;
		}

		/**
		 * Sets the offset.
		 * 
		 * @param os
		 *            the offset to set the node to.
		 */
		public void setOffset(int os) {
			offset = os;
		}


		@Override
		public String toString() {
			String btnstr = "";
			btnstr += "Keys: ";
			for (int i = 0; i < keys.size(); i++) {
				btnstr += keys.get(i) + " ";
			}
			btnstr += "\nChildren: ";
			for (int i = 0; i < children.size(); i++) {
				btnstr += children.get(i) + " ";
			}
			return btnstr;
		}

	}
} //End BTree
