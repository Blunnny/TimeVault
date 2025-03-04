package deque;

import java.util.Iterator;

/**
 * A double-ended queue (Deque) implemented using a circular sentinel doubly linked list.
 * Supports constant-time operations at both ends and maintains memory usage proportional to size.
 * Implements Iterable to allow iteration over elements.
 *
 * @param <T> the type of elements held in this deque
 */
public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    /**
     * Private inner class representing a node in the doubly linked list.
     */
    private class Node {
        /**
         * The element stored in this node
         */
        T item;
        /**
         * Reference to the previous node
         */
        Node prev;
        /**
         * Reference to the next node
         */
        Node next;

        /**
         * Constructs a node with the given item and links.
         *
         * @param item the element to store
         * @param prev the previous node
         * @param next the next node
         */
        Node(T item, Node prev, Node next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    /**
     * The sentinel node forming a circular structure
     */
    private Node sentinel;
    /**
     * Number of elements in the deque, ensures O(1) size()
     */
    private int size;

    /**
     * Constructs an empty LinkedListDeque.
     * Initializes the sentinel node in a circular topology.
     */
    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    /**
     * Adds an item to the front of the deque in constant time.
     *
     * @param item the item to add
     */
    @Override
    public void addFirst(T item) {
        Node newnode = new Node(item, sentinel, sentinel.next);
        sentinel.next.prev = newnode;
        sentinel.next = newnode;
        size += 1;
    }

    /**
     * Adds an item to the back of the deque in constant time.
     *
     * @param item the item to add
     */
    @Override
    public void addLast(T item) {
        Node newnode = new Node(item, sentinel.prev, sentinel);
        sentinel.prev.next = newnode;
        sentinel.prev = newnode;
        size += 1;
    }

    /**
     * Returns true if the deque is empty, false otherwise.
     *
     * @return whether the deque contains no elements
     */
//    public boolean isEmpty() {
//        return sentinel.next == sentinel;
//    }

    /**
     * Returns the number of items in the deque.
     *
     * @return the size of the deque
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Prints all items in the deque from first to last, separated by spaces,
     * followed by a newline.
     */
    @Override
    public void printDeque() {
        Node current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.item + " ");
            current = current.next;
        }
        System.out.println();
    }

    /**
     * Removes and returns the item at the front of the deque.
     * Returns null if the deque is empty.
     *
     * @return the removed item, or null if empty
     */
    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node first = sentinel.next;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        size -= 1;
        first.prev = null;
        first.next = null;
        return first.item;
    }

    /**
     * Removes and returns the item at the back of the deque.
     * Returns null if the deque is empty.
     *
     * @return the removed item, or null if empty
     */
    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node last = sentinel.prev;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        size -= 1;
        last.prev = null;
        last.next = null;
        return last.item;
    }

    /**
     * Gets the item at the given index (0-based, from the front).
     * Returns null if the index is invalid. Uses iteration.
     *
     * @param index the index of the item to get
     * @return the item at the index, or null if invalid
     */
    @Override
    public T get(int index) {
        if (index < 0 || index > size() - 1) {
            return null;
        }
        Node current = sentinel.next;
        int count = 0;
        while (count < index) {
            current = current.next;
            count += 1;
        }
        return current.item;
    }

    /**
     * Gets the item at the given index (0-based, from the front) using recursion.
     * Returns null if the index is invalid.
     *
     * @param index the index of the item to get
     * @return the item at the index, or null if invalid
     */
    public T getRecursive(int index) {
        if (isEmpty() || index < 0 || index > size() - 1) {
            return null;
        }
        return getRecursiveHelper(sentinel.next, index);
    }

    /**
     * Helper method for getRecursive to recursively traverse the deque.
     *
     * @param current the current node being processed
     * @param index   the remaining index to reach
     * @return the item at the target index
     */
    private T getRecursiveHelper(Node current, int index) {
        if (index == 0) {
            return current.item;
        }
        return getRecursiveHelper(current.next, index - 1);
    }

    /**
     * Returns an iterator over the items in the deque from first to last.
     *
     * @return an iterator for the deque
     */
    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    /**
     * Private inner class that implements Iterator<T> to traverse the deque.
     */
    private class DequeIterator implements Iterator<T> {
        /**
         * The current position in the iteration
         */
        private Node current;

        /**
         * Constructs an iterator starting at the first element
         */
        public DequeIterator() {
            current = sentinel.next;
        }

        /**
         * Returns true if there are more elements to iterate.
         *
         * @return whether there are more elements
         */
        public boolean hasNext() {
            return current != sentinel;
        }

        /**
         * Returns the next element and advances the iterator.
         *
         * @return the next element in the deque
         * @throws java.util.NoSuchElementException if no more elements exist
         */
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more elements to iterate");
            }
            T item = current.item;
            current = current.next;
            return item;
        }
    }

    /**
     * Checks if the given object is equal to this deque.
     * Two deques are equal if they contain the same items in the same order.
     *
     * @param o the object to compare with
     * @return true if equal, false otherwise
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof LinkedListDeque)) {
            return false;
        }
        LinkedListDeque<?> other = (LinkedListDeque<?>) o;
        if (this.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            T thisItem = this.get(i);
            Object otherItem = other.get(i);
            if (!thisItem.equals(otherItem)) {
                return false;
            }
        }
        return true;
    }
}