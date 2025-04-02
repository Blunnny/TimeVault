package byow.Core;

import byow.StdDraw;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Random;
import java.awt.Color;
import java.awt.Font;

/*
    * 处理关卡相关的逻辑（关卡数管理、通关条件、胜利界面等）
 */
public class LevelManager {
    private int currentLevel; // 当前关卡数
    private static final int MAX_LEVEL = 10; // 最大关卡数
    private TETile[][] world; // 当前世界
    private Random random; // 随机数生成器
    private int playerX, playerY; // 玩家位置
    private int exitX, exitY; // 出口位置
    private final int width, height; // 世界尺寸

    // 构造函数，初始化关卡管理器
    public LevelManager(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.currentLevel = 1;
        this.random = new Random(seed);
        initializeLevel(); // 初始化第一关
    }

    // 初始化当前关卡
    private void initializeLevel() {
        long seed = random == null ? System.currentTimeMillis() : random.nextLong(); // ？首次使用时间戳，后续随机
        random = new Random(seed);
        WorldGenerator generator = new WorldGenerator(width, height, seed);
        world = generator.generateWorld(seed);
        placePlayerAndExit();
    }

    // ？放置玩家和出口
    private void placePlayerAndExit() {
        boolean placedPlayer = false;
        boolean placedExit = false;
        while (!placedPlayer || !placedExit) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            if (world[x][y] == Tileset.GRASS) {
                if (!placedPlayer) {
                    world[x][y] = Tileset.AVATAR;
                    playerX = x;
                    playerY = y;
                    placedPlayer = true;
                } else if (!placedExit) {
                    world[x][y] = Tileset.LOCKED_DOOR;
                    exitX = x;
                    exitY = y;
                    placedExit = true;
                }
            }
        }
    }

    // 检查并处理玩家移动，返回是否需要渲染
    public boolean movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;
        if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
            if (world[newX][newY] == Tileset.GRASS) {
                world[playerX][playerY] = Tileset.GRASS;
                world[newX][newY] = Tileset.AVATAR;
                playerX = newX;
                playerY = newY;
                return true; // 需要渲染
            } else if (world[newX][newY] == Tileset.LOCKED_DOOR) {
                nextLevel();
                return true; // 需要渲染新关卡
            }
        }
        return false; // 无需渲染
    }

    // 进入下一关
    private void nextLevel() {
        if (currentLevel < MAX_LEVEL) {
            currentLevel++;
            initializeLevel();
        } else {
            drawVictoryScreen();
            System.exit(0); // 通关后退出
        }
    }

    // 绘制胜利界面
    private void drawVictoryScreen() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(font);
        StdDraw.text(width / 2.0, height / 2.0, "Congratulations! You Win!");
        StdDraw.show();
        StdDraw.pause(3000);
    }

    // 获取当前世界
    public TETile[][] getWorld() {
        return world;
    }

    // 获取当前关卡数
    public int getCurrentLevel() {
        return currentLevel;
    }

    // 获取最大关卡数
    public int getMaxLevel() {
        return MAX_LEVEL;
    }
}