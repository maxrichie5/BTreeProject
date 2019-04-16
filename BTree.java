public class BTree {
	
	private BTreeNode root;
	private int t;
	private int sequenceLenth;
	
	public BTree(int t, int sequenceLenth) {
		root = new BTreeNode();
		this.t = t;
		this.sequenceLenth = sequenceLenth;
	}
	
	/**
	 * Searches for the frequency of a given key value starting from the root. If the key is in a 
	 * child of the root, subSearch is called to recursively 
	 * look through all children until the key is found or is
	 * not there.
	 * @param The long key to be found
	 * @return The 
	 */
	public long search(long key) {
		int i = 1;
		
		while( i <= ) {
			
		}
		//TODO
	}
	
	public long subSearch() {
		//TODO
	}
	
	public void insert(long key) {
		if( root.getKeys().length == (2t-1) ) {		//root is full
			BTreeNode newNode = new BTreeNode();	//create new child node
			
			
		}
	}
	
	private class BTreeNode {
		
		private list keys;			//array of keys for this node
		private list children;		//array of children for this node
		private boolean isLeaf;		//boolean to keep track of this node being a leaf
		
		/**
		 * Constructor to create BTreeNode and initialize variables
		 */
		public BTreeNode(int t, boolean leafStatus) {
			keys = new list();
			children = new list();
			isLeaf = leafStatus;
		}
		
		/**
		 * Returns the long array of keys
		 */
		public long[] getKeys() {
			return keys;
		}
		
		
		/**
		 * Returns the long array of children
		 */
		public long[] getChildren() {
			return children;
		}
		
		/**
		 * Returns true if this node is a leaf node, false otherwise
		 */
		public boolean isLeaf() {
			return isLeaf;
		}
	}
} //End BTree
