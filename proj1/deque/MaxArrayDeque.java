package deque;

import java.util.Comparator;

/**
 * 创建继承自 ArrayDeque 的数组 MaxArrayDeque.
 *
 * @param <T> the type of elements held in this deque
 */
public class MaxArrayDeque<T> extends ArrayDeque<T> {
    /**
     * 存储构造函数中传入的比较器
     */
    private Comparator<T> comparator;

    /**
     * 创建带比较器的构造函数.
     *
     * @param c the comparator to determine the ordering of elements
     */
    public MaxArrayDeque(Comparator<T> c) {
        super(); // 调用父类 ArrayDeque 的无参构造函数
        this.comparator = c; // 将传入的比较器保存到成员变量
    }

    /**
     * Returns the maximum element in the deque based on the comparator provided in the constructor.
     * Returns null if the deque is empty.
     *
     * @return the maximum element, or null if empty
     */
    public T max() {
        if (isEmpty()) {
            return null;
        }
        T maxItem = get(0);
        for (int i = 1; i < size(); i++) {
            T current = get(i);
            if (comparator.compare(current, maxItem) > 0) {
                maxItem = current;
            }
        }
        return maxItem;
    }

    /**
     * Returns the maximum element in the deque based on the given comparator.
     * Returns null if the deque is empty.
     *
     * @param c the comparator to determine the ordering of elements
     * @return the maximum element, or null if empty
     */
    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T maxItem = get(0);
        for (int i = 1; i < size(); i++) {
            T current = get(i);
            if (c.compare(current, maxItem) > 0) {
                maxItem = current;
            }
        }
        return maxItem;
    }

    /**
     * Checks if the given object is equal to this deque.
     * Implementation is optional as per requirements.
     *
     * @param o the object to compare with
     * @return true if equal, false otherwise
     */
//    @Override
//    public boolean equals(Object o) {
//        // 留空，等待你实现（可选）
//        return false; // 占位符
//    }
}
