package byow.Core;

import java.io.Serializable;

/*
    * 该类用于保存游戏数据
 */
public class SaveData implements Serializable {
    private long seed;
    private int score;
    private int currentLevel;
    private int remainingTime;

    // 保存关键数据保存
    public SaveData(long seed, int score, int currentLevel, int remainingTime) {
        this.seed = seed;
        this.score = score;
        this.currentLevel = currentLevel;
        this.remainingTime = remainingTime;
    }

    // 获取关键数据的方法
    public long getSeed() { return seed; }
    public int getScore() { return score; }
    public int getCurrentLevel() { return currentLevel; }
    public int getRemainingTime() { return remainingTime; }
}