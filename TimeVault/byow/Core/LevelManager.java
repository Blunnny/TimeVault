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
    public static final int MAX_LEVEL = 5; // 最大关卡数
    private final int width, height; // 世界尺寸

    private TETile[][] world; // 当前世界
    private Random random; // 随机数生成器

    private int playerX, playerY; // 玩家位置

    private int exitX, exitY; // 出口位置

    private int keyX, keyY; // 钥匙位置
    private boolean hasKey; // 是否持有钥匙

    private int score; // 玩家当前分数
    private int[] levelBonus = {0, 100, 200, 400, 800, 1600}; // 每关的基础分数奖励

    private long initialSeed; // 设定初始种子用于加载之前的游戏

    // 倒计时相关变量
    private long levelStartTime; // 关卡开始时间（毫秒）
    private static final int TIME_LIMIT_SECONDS = 120; // 每关限时 120 秒
    private long pauseStartTime = 0; // 暂停开始时间
    private boolean isPaused = false; // 是否暂停
    int pausedRemainingTime = 0; // 暂停时的剩余时间

    // 构造函数，初始化关卡管理器
    public LevelManager(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.currentLevel = 1;
        this.score = 0; // 初始分数为0
        this.initialSeed = seed; // 保存初始种子
        this.random = new Random(seed);
        initializeLevel(); // 初始化第一关
    }

    // 初始化当前关卡
    private void initializeLevel() {
        long seed = random.nextLong(); // 基于初始种子生成新种子，保证每关地图不同但都基于起始种子
        WorldGenerator generator = new WorldGenerator(width, height, seed);
        world = generator.generateWorld(seed, currentLevel);
        hasKey = false; // 尚未获取钥匙
        levelStartTime = System.currentTimeMillis(); // 记录关卡开始时间
        placePlayerAndExit();
    }

    // 获取分数的方法
    public int getScore() {
        return score;
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
            if (world[newX][newY] == Tileset.GRASS) { // 目标位置为草地，则移动到目标位置
                world[playerX][playerY] = Tileset.GRASS;
                world[newX][newY] = Tileset.AVATAR;
                playerX = newX;
                playerY = newY;
                return true; // 需要渲染新关卡
            } else if (world[newX][newY] == Tileset.KEY) { // 目标位置为钥匙，则获取该钥匙并修改门的状态
                world[playerX][playerY] = Tileset.GRASS;
                world[newX][newY] = Tileset.AVATAR;
                playerX = newX;
                playerY = newY;
                hasKey = true; // 拾取钥匙
                world[exitX][exitY] = Tileset.UNLOCKED_DOOR;
                drawMessage("门已解锁！"); // 提示门已解锁
                return true;
            } else if (world[newX][newY] == Tileset.LOCKED_DOOR) { // 目标为上锁的门，提示无法开门
                drawMessage("需要钥匙才能打开门！"); // 无钥匙时显示提示
                return false;
            } else if (world[newX][newY] == Tileset.UNLOCKED_DOOR) { // 目标为解锁的门，则进入下一关
                    nextLevel(); // 有钥匙时进入下一关
                    return true;
            } else if (world[newX][newY] == Tileset.COIN) { // 目标为金币，则增加相应分数
                world[playerX][playerY] = Tileset.GRASS;
                world[newX][newY] = Tileset.AVATAR;
                playerX = newX;
                playerY = newY;
                int[] values = {50, 100, 150, 200, 250, 300}; // 拾取金币随机获得 50 - 300 分
                int levelBonus = currentLevel * 50; // 关卡每加一关，拾取金币会得到额外 50 奖励分
                extraPoints(values[random.nextInt(values.length)] + levelBonus);
                return true;
            } else if (world[newX][newY] == Tileset.EVENT) { // 目标为随机事件，使用 triggerRandomEvent 方法处理
                world[playerX][playerY] = Tileset.GRASS;
                world[newX][newY] = Tileset.AVATAR;
                playerX = newX;
                playerY = newY;
                triggerRandomEvent();
                return true;
            }
        }
        return false; // 无需渲染
    }

    // 随机事件触发方法
    private void triggerRandomEvent() {
        // 随机选择事件类型 (1-3)
        int eventType = random.nextInt(3) + 1;
        switch (eventType) {
            case 1: // 情况 1：直接获得3000-5000分
                int reward = 3000 + random.nextInt(2001); // 3000-5000随机
                score += reward;
                drawMessage("幸运奖励！获得 " + reward + " 分!", Color.MAGENTA);
                break;
            case 2: // 情况 2：赌博事件
                handleGamblingEvent();
                break;
            case 3: // 情况 3：加时 60 秒
                levelStartTime += 60000; // 将开始时间往前调 60 秒
                drawMessage("时间延长60秒!", Color.CYAN);
                break;
        }
    }


    private void handleGamblingEvent() {
        drawMessage("来赌一手？", Color.YELLOW);
        drawMessage("积分翻倍与减半各有一半可能", Color.YELLOW);
        drawMessage("给你 10 秒钟时间考虑一下(Y/N)", Color.YELLOW);
        long startTime = System.currentTimeMillis();
        boolean answered = false;
        boolean gamble = false;
        // 等待玩家输入
        while (!answered && (System.currentTimeMillis() - startTime < 10000)) { // 5秒超时
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (key == 'Y') {
                    gamble = true;
                    answered = true;
                } else if (key == 'N') {
                    gamble = false;
                    answered = true;
                }
            }
        }

        if (!answered) {
            drawMessage("这么纠结？那就算了~", Color.GRAY);
            return;
        }

        if (gamble) {
            boolean win = random.nextBoolean();
            if (win) {
                score *= 2;
                drawMessage("运气不错！分数翻倍！", Color.GREEN);
            } else {
                score /= 2;
                drawMessage("手气不行？下次再来试试吧！", Color.RED);
            }
        } else {
            drawMessage("不想玩？那就算了~", Color.WHITE);
        }
    }

    // 进入下一关
    private void nextLevel() {
        if (currentLevel < MAX_LEVEL) {
            // *通关奖励 = 基础奖励 + 剩余时间奖励（完成倒计时后实现）
            int timeBonus = getRemainingTime() * 2; // 每剩余 1 秒得 2 分
            score += levelBonus[currentLevel] + timeBonus;

            currentLevel++;
            initializeLevel();
        } else {
            drawVictoryScreen();
            System.exit(0); // 通关后退出
        }
    }

    // 获取奖励分
    public void extraPoints(int points) {
        score += points;
        drawMessage("获得 " + points + " 分!");
    }


    // 绘制胜利界面
    private void drawVictoryScreen() {
        // 显示通关字样
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("三极泼墨体", Font.PLAIN, 200);
        StdDraw.setFont(font);
        String[] messages = {"通关！", "得胜！"};
        Random randomSuccess = new Random();
        StdDraw.text(width / 2.0, height / 2.0, messages[randomSuccess.nextInt(messages.length)]);
        StdDraw.show();
        StdDraw.pause(3000);

        // 显示最终分数
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font scoreFont = new Font("三极泼墨体", Font.PLAIN, 100);
        StdDraw.setFont(scoreFont);
        StdDraw.text(width / 2.0, height / 2.0, "最终分数: " + score);

        // 分数记录逻辑
        boolean isNewRecord = HighScoreManager.isNewRecord(initialSeed, score);
        HighScoreManager.addScore(initialSeed, score);

        // 显示记录提示
        if (isNewRecord) {
            StdDraw.setPenColor(Color.ORANGE);
            Font recordFont = new Font("三极泼墨体", Font.BOLD, 50);
            StdDraw.setFont(recordFont);
            StdDraw.text(width/2.0, height/2.0 - 8, "新纪录达成!");
        }

        StdDraw.show();
        StdDraw.pause(3000);
    }

    // 绘制失败界面
    void drawFailureScreen() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("三极泼墨体", Font.BOLD, 40);
        StdDraw.setFont(font);
        String[] messages = {"失败！", "败阵！", "得练！", "GG!"};
        Random randomFailure = new Random();
        StdDraw.text(width / 2.0, height / 2.0, messages[randomFailure.nextInt(messages.length)]);
        StdDraw.show();
        StdDraw.pause(3000);
    }

    // 绘制临时消息
    public void drawMessage(String message, Color color) {
        StdDraw.setPenColor(Color.DARK_GRAY);
        StdDraw.filledRectangle(width / 2.0, height / 2.0, width / 4.0, 2);
        StdDraw.setPenColor(color);
        Font font = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(font);
        StdDraw.text(width / 2.0, height / 2.0, message);
        StdDraw.show();
        StdDraw.pause(1500); // 显示1.5秒
    }

    // 保留原有无颜色参数的drawMessage方法作为重载
    public void drawMessage(String message) {
        drawMessage(message, Color.WHITE);
    }

    // 暂停游戏
    public void pauseGame() {
        if (!this.isPaused) {
            // 计算当前的实际剩余时间
            int currentTimeRemaining = this.getRemainingTime(); // isPaused 仍为 false，计算实时剩余时间
            this.pausedRemainingTime = currentTimeRemaining; // 保存剩余时间
            this.isPaused = true; // 设置暂停状态
            this.pauseStartTime = System.currentTimeMillis(); // 记录暂停开始时间戳
        }
    }

    // 恢复游戏
    public void resumeGame() {
        if (this.isPaused) {
            long pausedDuration = System.currentTimeMillis() - this.pauseStartTime;
            this.levelStartTime += pausedDuration; // 调整关卡开始时间以抵消暂停期
            this.isPaused = false; // 将 isPaused 状态设置回 false
        }
    }

    // 检查时间是否耗尽
    boolean isTimeUp() {
        if (isPaused) {
            return false; // 暂停时不检查时间耗尽
        }
        return getRemainingTime() <= 0;
    }

    // 获取剩余时间（秒）
    public int getRemainingTime() {
        if (isPaused) {
            return pausedRemainingTime; // 暂停时返回固定的剩余时间
        } else {
            long elapsedTime = (System.currentTimeMillis() - levelStartTime) / 1000;
            int remaining = TIME_LIMIT_SECONDS - (int) elapsedTime;
            return Math.max(0, remaining);
        }
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


    // 设置游戏数据的方法
    public void setCurrentLevel(int level) {
        this.currentLevel = level;
        initializeLevel();
    }
    public void setScore(int score) {
        this.score = score;
    }
    public void setRemainingTime(int seconds) {
        long elapsed = TIME_LIMIT_SECONDS - seconds;
        levelStartTime = System.currentTimeMillis() - elapsed * 1000;
    }
}