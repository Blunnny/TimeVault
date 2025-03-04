package deque;

import java.util.Iterator;

/**
 * 使用数组实现双端队列,同时可以实现迭代
 *
 * @param <T> the type of elements held in this deque
 */
public class ArrayDeque<T> implements Iterable<T> {
    /**
     * 存储元素的数组
     */
    private T[] items;
    /**
     * 元素的数量
     */
    private int size;
    /**
     * 头部的索引
     */
    private int front;
    /**
     * 尾部索引（指向最后一个元素的后一个位置）
     */
    private int rear;

    /**
     * 创建一个空的数组实例，初始大小为 8
     */
    public ArrayDeque() {
        items = (T[]) new Object[8]; // 初始大小为 8
        size = 0;
        front = 0;
        rear = 0;
    }

    /**
     * 调整数组大小（数组满或利用不足的时候）
     *
     * @param capacity the new capacity of the array
     */
    private void resize(int capacity) {
        T[] newItems = (T[]) new Object[capacity];
        int current = front;
        for (int i = 0; i < size; i++) {
            newItems[i] = items[current];
            current = (current + 1) % items.length;
        }
        items = newItems;
        front = 0;
        rear = size;
    }

    /**
     * Adds an item to the front of the deque.
     *
     * @param item the item to add
     */
    public void addFirst(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        front = (front - 1 + items.length) % items.length;
        items[front] = item;
        size += 1;
    }

    /**
     * Adds an item to the back of the deque.
     *
     * @param item the item to add
     */
    public void addLast(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[rear] = item;
        rear = (rear + 1) % items.length;
        size += 1;
    }

    /**
     * Returns true if the deque is empty, false otherwise.
     *
     * @return whether the deque contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of items in the deque.
     *
     * @return the size of the deque
     */
    public int size() {
        return size;
    }

    /**
     * Prints all items in the deque from first to last, separated by spaces,
     * followed by a newline.
     */
    public void printDeque() {
        int current = front;
        for (int i = 0; i < size; i++) {
            System.out.print(items[current] + " ");
            current = (current + 1) % items.length;
        }
        System.out.println();
    }

    /**
     * Removes and returns the item at the front of the deque.
     * Returns null if the deque is empty.
     *
     * @return the removed item, or null if empty
     */
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T item = items[front];
        items[front] = null;
        front = (front + 1) % items.length;
        size -= 1;
        if (items.length >= 16 && size < items.length * 0.25) {
            resize(items.length / 2);
        }
        return item;
    }

    /**
     * Removes and returns the item at the back of the deque.
     * Returns null if the deque is empty.
     *
     * @return the removed item, or null if empty
     */
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        rear = (rear - 1 + items.length) % items.length;
        T item = items[(rear) % items.length];
        items[(rear) % items.length] = null;
        size -= 1;
        if (items.length >= 16 && size < items.length * 0.25) {
            resize(items.length / 2);
        }
        return item;
    }

    /**
     * Gets the item at the given index (0-based, from the front).
     * Returns null if the index is invalid.
     *
     * @param index the index of the item to get
     * @return the item at the index, or null if invalid
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return items[(index + front) % items.length];
    }

    /**
     * Returns an iterator over the items in the deque from first to last.
     *
     * @return an iterator for the deque
     */
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    /**
     * Private inner class that implements Iterator<T> to traverse the deque.
     */
    private class ArrayDequeIterator implements Iterator<T> {
        private int pos; // Current position relative to front
        private int count; // Number of elements iterated

        public ArrayDequeIterator() {
            pos = front;
            count = 0;
        }

        public boolean hasNext() {
            return count < size;
        }

        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more elements to iterate");
            }
            T item = items[pos];
            pos = (pos + 1) % items.length;
            count += 1;
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
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque<?> other = (ArrayDeque<?>) o;
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