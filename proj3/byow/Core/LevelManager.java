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
    private static final int MAX_LEVEL = 5; // 最大关卡数
    private final int width, height; // 世界尺寸

    private TETile[][] world; // 当前世界
    private Random random; // 随机数生成器

    private int playerX, playerY; // 玩家位置

    private int exitX, exitY; // 出口位置

    private int keyX, keyY; // 钥匙位置
    private boolean hasKey; // 是否持有钥匙

    // 倒计时相关变量
    private long levelStartTime; // 关卡开始时间（毫秒）
    private static final int TIME_LIMIT_SECONDS = 120; // 每关限时 120 秒

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
        long seed = random.nextLong(); // 基于初始种子生成新种子，保证每关地图不同但都基于起始种子
        WorldGenerator generator = new WorldGenerator(width, height, seed);
        world = generator.generateWorld(seed);
        hasKey = false; // 尚未获取钥匙
        levelStartTime = System.currentTimeMillis(); // 记录关卡开始时间
        placePlayerAndExit();
    }

    // 放置玩家和出口
    private void placePlayerAndExit() {
        boolean placedPlayer = false;
        boolean placedExit = false;
        boolean placedKey = false;
        while (!placedPlayer || !placedExit || !placedKey) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            if (world[x][y] == Tileset.GRASS) {
                if (!placedPlayer) {
                    world[x][y] = Tileset.AVATAR;
                    playerX = x;
                    playerY = y;
                    placedPlayer = true;
                } else if (!placedExit) {
                    if (Math.abs(x - playerX) + Math.abs(y - playerY) > 40) { // 确保出口与玩家距离至少为 40
                        world[x][y] = Tileset.LOCKED_DOOR;
                        exitX = x;
                        exitY = y;
                        placedExit = true;
                    }
                } else if (!placedKey) {
                    if (Math.abs(x - playerX) + Math.abs(y - playerY) > 40
                        && Math.abs(x - exitX) + Math.abs(y - exitY) > 40) { // 钥匙与玩家和出口有一定距离
                        world[x][y] = Tileset.KEY; // 使用 Tileset.KEY 表示钥匙
                        keyX = x;
                        keyY = y;
                        placedKey = true;
                    }
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
                return true; // 需要渲染新关卡
            } else if (world[newX][newY] == Tileset.KEY) {
                world[playerX][playerY] = Tileset.GRASS;
                world[newX][newY] = Tileset.AVATAR;
                playerX = newX;
                playerY = newY;
                hasKey = true; // 拾取钥匙
                world[exitX][exitY] = Tileset.UNLOCKED_DOOR;
                drawMessage("门已解锁！"); // 提示门已解锁
                return true;
            } else if (world[newX][newY] == Tileset.LOCKED_DOOR) {
                if (hasKey) {
                    nextLevel(); // 有钥匙时进入下一关
                    return true;
                } else {
                    drawMessage("需要钥匙才能打开门！"); // 无钥匙时显示提示
                    return false;
                }
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
        Font font = new Font("三极泼墨体", Font.BOLD, 100);
        StdDraw.setFont(font);
        String[] messages = {"通关！", "得胜！"};
        Random randomSuccess = new Random();
        StdDraw.text(width / 2.0, height / 2.0, messages[randomSuccess.nextInt(messages.length)]);
        StdDraw.show();
        StdDraw.pause(3000);
    }

    // 绘制失败界面
    void drawFailureScreen() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("三极泼墨体", Font.BOLD, 40);
        StdDraw.setFont(font);
        String[] messages = {"失败！", "败阵！", "弱鸡！", "菜鸡！", "得练！", "GG!"};
        Random randomFailure = new Random();
        StdDraw.text(width / 2.0, height / 2.0, messages[randomFailure.nextInt(messages.length)]);
        StdDraw.show();
        StdDraw.pause(3000);
    }

    // 绘制临时消息
    private void drawMessage(String message) {
        StdDraw.setPenColor(Color.GRAY);
        StdDraw.filledRectangle(width / 2.0, height / 2.0, width / 4.0, 2);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setFont(font);
        StdDraw.text(width / 2.0, height / 2.0, message);
        StdDraw.show();
        StdDraw.pause(1000); // 显示 1 秒
    }

    // 检查时间是否耗尽
    boolean isTimeUp() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = (currentTime - levelStartTime) / 1000; // 转换为秒
        return elapsedTime >= TIME_LIMIT_SECONDS;
    }

    // 获取剩余时间（秒）
    public int getRemainingTime() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = (currentTime - levelStartTime) / 1000;
        return Math.max(0, TIME_LIMIT_SECONDS - (int) elapsedTime);
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
    public static int getMaxLevel() {
        return MAX_LEVEL;
    }
}