package gh2;

import deque.Deque;
import deque.LinkedListDeque;

//Note: This file will not compile until you complete the Deque implementations
public class GuitarString {
    /**
     * Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday.
     */
    private static final int SR = 44100;      // 采样率
    private static final double DECAY = .996; // 能量衰减因子


    /* Buffer for storing sound data. */
    private Deque<Double> buffer;
    private final int capacity;

    /* Create a guitar string of the given frequency.  */
    public GuitarString(double frequency) {
        // 初始化 buffer 为一个 Deque<Double>，容量为 SR / frequency（采样率除以频率），表示一个振动周期的样本数
        // 用 0.0 填充缓冲区，模拟初始静音状态
        buffer = new LinkedListDeque<>();
        capacity = (int) Math.round(SR / frequency); // 采样率除以频率
        for (int i = 0; i < capacity; i++) {
            buffer.addLast(0.0);
        }
    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        // 模拟拨弦，将 buffer 中的所有元素替换为随机噪声（值在 -0.5 到 0.5 之间）
        while (!buffer.isEmpty()) {
            buffer.removeFirst();
        }
        for (int i = 0; i < capacity; i++) {
            buffer.addLast(Math.random() - 0.5);
        }
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        if (buffer.size() < 2) {
            throw new IllegalStateException("Buffer must have at least 2 elements");
        }
        // 移除前端样本,与下一个样本平均并衰减,加入末端
        Double front = buffer.removeFirst();
        Double newFront = buffer.get(0);
        double newSample = (front + newFront) * 0.5 * DECAY;
        buffer.addLast(newSample);
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        if (!buffer.isEmpty()) {
            return buffer.get(0);
        }
        return 0.0;
    }
}
