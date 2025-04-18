package byow.Core;

import java.io.Serializable;

/*
    * 该类用于记录最高分
 */
public class HighScore implements Serializable, Comparable<HighScore> {
    private final long seed;
    private final int score;
    private final String timestamp;

    // 记录种子、分数以及时间
    public HighScore(long seed, int score) {
        this.seed = seed;
        this.score = score;
        this.timestamp = new java.util.Date().toString();
    }

    // 比较分数
    @Override
    public int compareTo(HighScore other) {
        return Integer.compare(other.score, this.score); // 降序排列
    }

    // 获取信息
    public long getSeed() { return seed; }
    public int getScore() { return score; }
    public String getTimestamp() { return timestamp; }
}