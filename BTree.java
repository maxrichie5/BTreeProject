import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.LinkedList;

public class BTree implements Serializable {

	private BTreeNode root, currentNode, nextNode; //The root, current, next node in this BTree     
	private int debugLevel, degree, cacheSize, sequenceLength, nodeCount;
	private static RandomAccessFile raf; //The file we are writing to and reading from
	private static int rafOffset = 0; //The position being read/written to in the raf
	private static int maxBTreeNodeSize = 100000; //The largest expected size in bytes of a BTree Node
	private static int optimal = 4096;
	
    private static final int intMetadata = 4;
    private static final int booleanMetadata = 4;

	public BTree(int sequenceLength, int cacheSize, int degree, int debugLevel, String gbkFileName) {
		if (degree == 0) {
			degree = getOptimalDegree();
		} else {
			this.degree = degree;
		}
		this.debugLevel = debugLevel;
		this.sequenceLength = sequenceLength;
		this.cacheSize = cacheSize;
		nodeCount = 1;
		
		String btreeFileName = gbkFileName+".btree.data."+sequenceLength+"."+degree;
		File file = new File(btreeFileName);
		if(!file.exists()) { 
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			raf = new RandomAccessFile(btreeFileName, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		root = new BTreeNode(degree, true, true, 0);
	}
	
	public BTree(String btreeFileName, int debugLevel, int cacheSize) throws ClassNotFoundException, IOException {
		String[] sa = btreeFileName.split("."); //split the file name by .'s
		degree = Integer.parseInt(sa[5]); //get degree
		sequenceLength = Integer.parseInt(sa[4]); //get sequenceLength
		
		this.debugLevel = debugLevel;
		this.cacheSize = cacheSize;
		
		File file = new File(btreeFileName);
		try {
			raf = new RandomAccessFile(btreeFileName, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		root = diskRead(0);
	}
	
	public void finish() throws IOException {
		diskWrite(root, root.getOffset());
	}

	public int search(long key) throws ClassNotFoundException, IOException {
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
				currentNode = diskRead(currentNode.children.get(i)*maxBTreeNodeSize);
			}
		}
	}

	public void insert(TreeObject to) throws ClassNotFoundException, IOException {
		if(root.isFull()) { //root node is full
			nextNode = root; //create node to be parent of root after split
			root = new BTreeNode(degree, true, false, ++nodeCount); //make newParent the root
			root.children.add(0, nextNode.getIndex());
			nextNode.setParentIndex(root.getIndex());
			nextNode.setRoot(false);

			split(root, 0, nextNode); /*pls explain what OLDROOT is i am confused so i changed it*/

		} else { //root node is not full
			insertNonFull(to);
		}

	}

	private void split(BTreeNode parentNode, int childIndex, BTreeNode child) throws IOException {
		BTreeNode newNode = new BTreeNode(degree, false, child.isLeaf(), ++nodeCount);

		for(int j = 0; j < (degree-1); j++) { //move half the full node's keys to new node
			newNode.addKey(child.getKey(degree));
			child.getKeys().remove(degree);
			child.setNumKeys(child.getNumKeys()-1);
		}

		if(!child.isLeaf()) { //if child is not a leaf, move half the child's kids to new node
			int numChildsChildren = child.getChildren().size(); //number of children in child node
			for(int j = 0; j < (numChildsChildren/2); j++) {
				newNode.addChild(child.getChild(degree), j);
				child.getChildren().remove(degree);
			}
		}

		int i = 0;
		int lastChildIndex = child.getNumKeys()-1;
		int parentNumKeys = parentNode.getNumKeys();
		while(i <= parentNumKeys) { //find where to place key that gets shifted up
			if(parentNumKeys == 0) {
				parentNode.addKey(child.getKey(lastChildIndex));
				break;
			}
			else if(i == parentNumKeys-1) { //will add child node's key to end
				parentNode.addKey(child.getKey(lastChildIndex));
				child.removeKey(lastChildIndex);
				break;
			} else { //not at end of parent's list of keys yet
				int cmpResult = Long.compare( child.getKey(lastChildIndex).getKey(), parentNode.getKey(i).getKey() );
				if(cmpResult < 0) { //if key < parent key
					parentNode.addKey(child.getKey(lastChildIndex), i); //add key to current spot
					child.removeKey(lastChildIndex);
					break;
				} //else go to next index
			}
			i++;
		}

		parentNode.addChild(newNode.getIndex(), i+1); //add newNode after child in parent's list of children
		newNode.setParentIndex(parentNode.getIndex());
		child.setParentIndex(parentNode.getIndex());
		
		//set number of keys
		//parentNode.setNumKeys(parentNode.getNumKeys()+1);
		//child.setNumKeys(child.getKeys().size());
		//newNode.setNumKeys(newNode.getKeys().size());

		//write the nodes
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
				currentNode.setNumKeys(currentNode.getNumKeys()+1);
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
				if(index == currentNode.getChildren().size()) {
					currentNode.keys.add(index, to);
					currentNode.setNumKeys(currentNode.getNumKeys()+1);
					return;
				}
				nextNode = diskRead(currentNode.children.get(index)*maxBTreeNodeSize);
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
						nextNode = diskRead(currentNode.children.get(index + 1)*maxBTreeNodeSize);
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
	
	public static int getOptimalDegree() {
		
        int metadata = (intMetadata * 4) + (booleanMetadata * 2);
        int totalSize = 12;
        int pointerSize = 4;

		optimal -= metadata;
		optimal += totalSize;
		optimal -= pointerSize;
		int dividedBy = ((2*totalSize)+(2*pointerSize));
		optimal /= dividedBy;
		return optimal;

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

			if (isRoot()) {
				parentIndex = -1; // To indicate that it has no parent for now
			}

		}

		/**
		 * check if node is full
		 * 
		 * @return
		 */
		public boolean isFull() {
			return keys.size() == ((2*degree)-1);
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
			numKeys--;
			return keys.remove(index);
		}

		/**
		 * Adds a key to the key list
		 * 
		 * @param key
		 *            Element to be added
		 */
		public void addKey(TreeObject key) {
			numKeys++;
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
			numKeys++;
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
			return (maxBTreeNodeSize * index);
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