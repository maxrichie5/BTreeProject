import java.io.Serializable;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Contains all the methods to create a new cache with methods to add, remove,
 * and check
 * 
 * @author thomasreinking
 *
 * @param <BTreeNode>
 *            For BTreeNode objects
 */
public class Cache<BTreeNode> implements Serializable {
	private LinkedList<BTreeNode> list;
	private final int MAX_SIZE;
	private BTreeNode node = null;

	/**
	 * Creates a new cache with the size given from the parameter
	 * 
	 * @param size
	 */
	public Cache(int size) throws NumberFormatException {
		list = new LinkedList<BTreeNode>();
		MAX_SIZE = size;
	}

	/**
	 * Adds the element to list, if the element is in the cache then it moves it to
	 * the front
	 * 
	 * @param element
	 *            node to be added
	 */
	public BTreeNode add(BTreeNode element) {
		BTreeNode returnNode = null;
		if (isFull()) {
			returnNode = list.removeLast();
		}
		BTreeNode node = get(element);
		if (node == null) {
			list.addFirst(element);
		} else { // node in cache, add to front
			list.addFirst(node);
		}
		return returnNode;
	}

	/**
	 * Looks for BTreeNode in cache and returns it if found.
	 * 
	 * @param element
	 *            node to find
	 * @return BTreeNode if found, null if not found
	 */
	public BTreeNode get(BTreeNode element) {
		if (check(element)) {
			return list.remove(indexOf(element));
		}
		return null;
	}
	
	/**
	 * Looks for BTreeNode in cache and returns it if found.
	 * 
	 * @param index
	 *            node to find
	 * @return BTreeNode 
	 */
	public BTreeNode get(int index) {
		
		return list.remove(index);
	}

	/**
	 * Removes the last element in the list
	 * 
	 * @return the element removed
	 */
	public BTreeNode removeLast() {
		return list.removeLast();
	}

	/**
	 * Removes all element in the list
	 */
	public void clearCache() {
		while (!isEmpty()) {
			removeLast();
		}
	}

	/**
	 * Checks to see if the cache is full
	 * 
	 * @return boolean result of size compared to max_size
	 */
	public boolean isFull() {
		return size() == MAX_SIZE;
	}

	/**
	 * Removes the chosen element out of the list
	 * 
	 * @param element
	 * @return the element removed
	 */
	public BTreeNode remove(BTreeNode element) {
		int idx = list.indexOf(element);
		if (idx < 0) {
			throw new NoSuchElementException();
		}
		BTreeNode t = list.get(idx);
		list.remove(t);
		return t;
	}

	/**
	 * Checks to see if the target element is in the list
	 * 
	 * @param target
	 * @return a boolean result of target in the list
	 */
	public boolean check(BTreeNode target) {
		return list.contains(target);
	}

	/**
	 * Checks to see if the list is empty
	 * 
	 * @return boolean result of it being empty
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * Returns the size of the list
	 * 
	 * @return size
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Returns the max size of the list
	 * 
	 * @return size
	 */
	public int maxSize() {
		return MAX_SIZE;
	}

	/**
	 * Finds the index of target element
	 * 
	 * @param element
	 * @return The index of that element
	 */
	public int indexOf(BTreeNode element) {
		return list.indexOf(element);
	}

	@Override
	public String toString() {
		return list.toString();
	}
}