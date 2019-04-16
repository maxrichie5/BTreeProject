import java.util.LinkedList;

public class BTree {
	
	private BTreeNode root;
	private int t;
	private int sequenceLenth;
	
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
	
	private class BTreeNode {
		
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
