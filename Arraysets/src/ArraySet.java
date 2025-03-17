import java.util.Iterator;

public class ArraySet<T> implements Iterable<T> {
    private T[] items;
    private int size;

    public ArraySet() {
        items = (T[]) new Object[100];
        size = 0;
    }

    public boolean contains(T x) {
        for (int i = 0; i < size; i++) {
            if (items[i] == null) {
                if (x == null) {
                    return true;
                }
            }
            if (items[i].equals(x)) {
                return true;
            }
        }
        return false;
    }

    public void add(T x) {
        if (x == null) {
            throw new IllegalArgumentException("you cannot add null to an ArraySet!");
        }
        if (contains(x)) {
            return;
        }
        items[size] = x;
        size += 1;
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                return items[index++];
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder("{");
        for (T item : this) {  // 现在可以正确使用 for-each
            returnString.append(item).append(", ");
        }
        if (size > 0) {
            returnString.setLength(returnString.length() - 2); // 去除最后的 ", "
        }
        returnString.append("}");
        return returnString.toString();
    }

    public static void main(String[] args) {
        ArraySet<String> s = new ArraySet<>();
        s.add("horse");
        s.add("fish");
        s.add("house");
        s.add("fish");

        System.out.println(s); // 输出: {horse, fish, house}
    }
}
