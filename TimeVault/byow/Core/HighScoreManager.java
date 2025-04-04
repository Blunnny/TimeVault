package byow.Core;

import java.io.*;
import java.util.*;

/*
    * 该类用于管理高分
 */
public class HighScoreManager {
    private static final String HIGHSCORE_FILE = "highscores.dat";
    private static final Map<Long, Integer> seedRecords = new HashMap<>();
    private static final List<HighScore> allScores = new ArrayList<>();

    public HighScoreManager() {
        loadScores();
    }

    private void loadScores() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(HIGHSCORE_FILE))) {

            allScores.addAll((List<HighScore>) ois.readObject());
            updateSeedRecords();
        } catch (IOException | ClassNotFoundException e) {
            // 首次运行时文件不存在是正常的
        }
    }

    private static void updateSeedRecords() {
        seedRecords.clear();
        for (HighScore score : allScores) {
            seedRecords.merge(score.getSeed(), score.getScore(), Math::max);
        }
    }

    public static void addScore(long seed, int score) {
        HighScore newScore = new HighScore(seed, score);
        allScores.add(newScore);
        Collections.sort(allScores);

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(HIGHSCORE_FILE))) {

            oos.writeObject(allScores);
            updateSeedRecords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<HighScore> getTopScores(int count) {
        return allScores.subList(0, Math.min(count, allScores.size()));
    }

    public static boolean isNewRecord(long seed, int score) {
        return score > seedRecords.getOrDefault(seed, 0);
    }
}