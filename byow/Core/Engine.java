package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Engine {
    // 创建 TERenderer 实例
    TERenderer ter = new TERenderer();
    // 设定世界的宽和高
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;

    // 收集用户输入的种子值
    private StringBuilder seedInput = new StringBuilder();
    // 标记当前是否处于 “等待用户输入种子” 的状态
    private boolean waitingForSeed = false;
    // 标记游戏是否已经开始
    private boolean gameStarted = false;

    // 默认字体和颜色，用于重置
    private static final Font DEFAULT_FONT = new Font("Monaco", Font.PLAIN, 14);
    private static final Color DEFAULT_COLOR = Color.WHITE;

    // 输入节流和指令队列相关变量
    private long lastInputTime = 0; // 上一次输入指令的时间戳
    private static final long INPUT_DELAY_MS = 100; // 输入间隔（毫秒）
    private static final long EXECUTE_DELAY_MS = 100; // 指令执行间隔（毫秒）
    private long lastExecuteTime = 0; // 上次执行指令的时间戳
    private Queue<Character> commandQueue = new LinkedList<>(); // 指令队列
    private static final int MAX_QUEUE_SIZE = 3; // 队列最大容量

    // 添加暂停状态变量
    private boolean isPaused = false;

    // 数据存储文件
    private static final String SAVE_FILE = "gamesave.dat";

    // 最高分存储相关变量
    private HighScoreManager highScoreManager = new HighScoreManager();
    private boolean showingHighScores = false;

    // 标记是否显示帮助界面
    private boolean showingHelp = false;

    // 关卡管理器
    private LevelManager levelManager;

    // 初始化渲染器
    public Engine() {
        ter.initialize(WIDTH, HEIGHT + 2); // 额外 2 行（顶部）给 HUD 指示栏
    }

    // 程序入口
    public static void main(String[] args) {
        new Engine().interactWithKeyboard();
    }

    /*
     * 处理键盘交互模式，程序的主循环。
     * 初始显示开始界面，监听用户输入，根据状态更新 UI。
     */
    public void interactWithKeyboard() {
        drawStartScreen(); // 调用方法显示初始界面

        // 添加最后渲染时间跟踪
        long lastRenderTime = 0;
        final long RENDER_INTERVAL_MS = 300; // 每 300ms渲染一次(频率更低时会出现屏幕闪烁)
        boolean needsPauseRender = false; // 标记是否需要在暂停后渲染一次

        while (true) { // 持续监听用户输入
            long currentTime = System.currentTimeMillis();
            // 检查时间是否耗尽
            if (gameStarted && levelManager.isTimeUp() && !isPaused) {
                levelManager.drawFailureScreen();
                System.exit(0);
            }
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                boolean wasPaused = isPaused; // 记录处理前的暂停状态
                handleKey(key);
                // 检查是否刚刚进入暂停状态
                if (!wasPaused && isPaused) {
                    needsPauseRender = true; // 设置标记，需要在下一次渲染循环画一次暂停画面
                }
            }
            // 在初始界面或种子输入阶段，直接处理按键，不使用队列
            if (gameStarted) {
                // 游戏开始后，使用队列和节流机制处理移动指令
                if (showingHelp) { // 如果正在显示帮助，持续绘制帮助界面
                    drawHelpScreen();
                } else if (isPaused) {
                    // 如果处于暂停状态
                    if (needsPauseRender) {
                        // 如果标记为需要渲染暂停画面，则渲染一次
                        renderWorldWithHUD(); // 绘制包含“暂停中”信息的 HUD
                        needsPauseRender = false; // 重置标记，之后不再渲染直到解除暂停
                    }
                }  else { // 游戏正常运行
                    needsPauseRender = false; // 确保非暂停状态下此标记为 false
                    // 执行指令队列中的指令
                    if (levelManager != null && currentTime - lastExecuteTime >= EXECUTE_DELAY_MS && !commandQueue.isEmpty()) {
                        char command = commandQueue.poll(); // 取出指令
                        handleKey(command); // 执行指令 (例如 WASD 移动)
                        lastExecuteTime = currentTime;
                        renderWorldWithHUD(); // 执行指令后立即渲染最新状态
                        lastRenderTime = currentTime; // 重置渲染计时器
                    } else if (levelManager != null && currentTime - lastRenderTime >= RENDER_INTERVAL_MS) {
                        // 如果没有指令执行，按固定间隔渲染
                        renderWorldWithHUD();
                        lastRenderTime = currentTime;
                    }
                }
            } else { // 游戏未开始 (种子输入界面)
                // 种子界面的绘制由 handleKey 内部的 drawSeedInputScreen() + show() 完成
                // 无需在此处添加代码
            }
            StdDraw.pause(10);
        }
    }

    // 绘制程序的初始界面，提示用户按 N 开始新游戏
    // *最后打包一下字体 *待添加闪烁效果
    private void drawStartScreen() {
        StdDraw.clear(Color.BLACK); // 清空画布，填充为黑色背景
        // 设置画笔颜色为白色
        StdDraw.setPenColor(Color.WHITE);
        // 标题字体为 三极泼墨体（粗体，100 号）
        Font fontTitle = new Font("三极泼墨体", Font.BOLD, 100);
        StdDraw.setFont(fontTitle);
        // 在指定坐标绘制文本, (WIDTH / 2.0, HEIGHT / 2.0) 是屏幕中心
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 + 2, "Time Vault");

        // 设置说明文字字体格式并绘制
        Font fontState = new Font("黑体", Font.PLAIN, 30);
        StdDraw.setFont(fontState);
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 - 10, "按'N'键开始"); // ***待添加闪烁效果
        // 将绘制内容从后台缓冲区刷新到屏幕上（双缓冲机制）
        StdDraw.show();

        // 重置字体和颜色，防止影响后续渲染
        StdDraw.setFont(DEFAULT_FONT);
        StdDraw.setPenColor(DEFAULT_COLOR);

    }

    // 根据当前状态和用户输入的键，更新程序状态
    private void handleKey(char key) {
        // 处理初始界面的输入
        if (!waitingForSeed && !gameStarted) {
            if (key == 'N' || key == 'n') {
                waitingForSeed = true; // 标记程序进入“等待种子”状态
                drawSeedInputScreen(); // 显示种子输入界面
            }
        } else if (waitingForSeed) { // 处理种子输入阶段的键盘输入
            if (key == 'C' || key == 'c') { // 如果用户按下 C 或 c，则展示最高分列表
                showingHighScores = !showingHighScores;
                drawSeedInputScreen();
            } else if (Character.isDigit(key)) { // 如果用户输入的是数字，则将数字存入 seedInput
                seedInput.append(key);
                drawSeedInputScreen();
            } else if (key == 'S' || key == 's') { // 如果用户按下 S 或 s，确认种子并生成世界
                long seed = Long.parseLong(seedInput.toString()); // 将输入的字符串（例如 "123"）转换为 long 类型的种子值
                levelManager = new LevelManager(WIDTH, HEIGHT, seed); // 初始化关卡管理器
                gameStarted = true; // 标记游戏开始
                waitingForSeed = false; // 退出种子输入模式
                renderWorldWithHUD(); // 立即渲染世界和 HUD
            } else {}
        } else if (gameStarted) { // 处理游戏开始后的键盘输入
            // --- 优先处理 H 键 ---
            if (key == 'H'|| key == 'h') {
                showingHelp = !showingHelp; // 切换帮助界面的显示状态
                return;
                }
            // --- 如果正在显示帮助，则忽略其他游戏相关按键 ---
            if (showingHelp) {
                return; // 在帮助界面时，不处理 Q, P, O, L, WASD 等
            }
            if (key == 'Q' || key == 'q') { // 按 Q 可以退出程序 ***待在游戏界面添加提示
                System.exit(0);
            } else if (key == 'P' || key == 'p') { // 按 P 键暂停/继续游戏
                isPaused = !isPaused;
                if (isPaused) {
                    levelManager.pauseGame(); // 通知关卡管理器暂停
                    // levelManager.drawMessage("游戏已暂停 (按P继续)", Color.YELLOW);
                } else {
                    levelManager.resumeGame(); // 通知关卡管理器恢复
                }
            } else if (!isPaused) { // 若未处于暂停状态，则实现其他操作逻辑
                if (key == 'W' || key == 'w') { // 向上移动一格
                    levelManager.movePlayer(0, 1);
                } else if (key == 'A' || key == 'a') { // 向左移动一格
                    levelManager.movePlayer(-1, 0);
                } else if (key == 'S' || key == 's') { // 向下移动一格
                    levelManager.movePlayer(0, -1);
                } else if (key == 'D' || key == 'd') { // 向右移动一格
                    levelManager.movePlayer(1, 0);
                } else if (key == 'L' || key == 'l') { // 加载存档
                    if (loadGame()) {
                        renderWorldWithHUD();
                    }
                }
                else if (key == 'O' || key == 'o') { // 保存存档
                    saveGame();
                }
            }
        }
    }

    // 绘制种子输入界面，显示当前输入的种子和提示。
    private void drawSeedInputScreen() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        // 种子输入提示字体为 三极泼墨体（粗体，80 号）
        Font fontSeed = new Font("三极泼墨体", Font.PLAIN, 80);
        StdDraw.setFont(fontSeed);
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 + 2, "输入种子号: " + seedInput.toString());

        if (showingHighScores) {
            drawHighScores();
        } else {
            Font fontState = new Font("黑体", Font.PLAIN, 30);
            StdDraw.setFont(fontState);
            StdDraw.text(WIDTH/2.0, HEIGHT/2.0 - 15, "按 C 查看高分榜");
            StdDraw.text(WIDTH/2.0, HEIGHT/2.0 - 10, "按'S'键开始游戏");
        }

        // 重置字体和颜色，防止影响后续渲染
        StdDraw.setFont(DEFAULT_FONT);
        StdDraw.setPenColor(DEFAULT_COLOR);

        StdDraw.show();
    }

    // 绘制最高分界面
    private void drawHighScores() {
        StdDraw.clear(Color.BLACK);
        List<HighScore> topScores = highScoreManager.getTopScores(5);
        StdDraw.setPenColor(Color.YELLOW);
        Font font = new Font("三极泼墨体", Font.BOLD, 40);
        StdDraw.setFont(font);

        StdDraw.text(WIDTH/2.0, HEIGHT/2.0 + 10, "=== 历史高分榜 ===");

        StdDraw.setPenColor(Color.WHITE);
        for (int i = 0; i < topScores.size(); i++) {
            HighScore score = topScores.get(i);
            String text = String.format("%d. 种子: %d  分数: %,d  时间: %s",
                    i+1, score.getSeed(), score.getScore(),
                    score.getTimestamp().substring(0, 16));
            StdDraw.text(WIDTH/2.0, HEIGHT/2.0 - i*3, text);
        }

        if (topScores.isEmpty()) {
            StdDraw.text(WIDTH/2.0, HEIGHT/2.0, "暂无记录");
        }
    }

    // 渲染世界并绘制 HUD
    private void renderWorldWithHUD() {
        // 确保渲染前的字体和颜色是默认值
        StdDraw.setFont(DEFAULT_FONT);
        StdDraw.setPenColor(DEFAULT_COLOR);

        ter.renderFrame(levelManager.getWorld()); // 渲染世界
        drawHUD(); // 绘制 HUD
    }

    // 绘制顶端状态栏
    private void drawHUD() {
        // 绘制 HUD 背景（覆盖顶部一行）
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(WIDTH / 2.0, HEIGHT + 1, WIDTH / 2.0, 2);

        // 设置字体和颜色
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("站酷酷黑", Font.PLAIN, 20);
        StdDraw.setFont(font);

        // 1. 在左上方显示当前关卡数
        String levelText = "第 " + levelManager.getCurrentLevel() + " / " + LevelManager.MAX_LEVEL + " 关 ";
        StdDraw.text(5.0, HEIGHT, levelText);
        // 2. 在右上方显示“按 H 键获取帮助”
        String quitText = "按 H 键获取帮助";
        StdDraw.text(WIDTH - 7.0, HEIGHT, quitText); // 靠右显示，距离右边缘 10 个单位
        // 3. 在中间偏左显示当前分数
        String scoreText = "当前分数 : " + levelManager.getScore();
        StdDraw.text(WIDTH / 2.0 - 13, HEIGHT, scoreText);
        // 4. 在中间偏右显示倒计时
        int remainingTime = levelManager.getRemainingTime();
        if (isPaused) {
            remainingTime = levelManager.pausedRemainingTime; // 直接访问 pausedRemainingTime（需改为 public 或添加 getter）
        }
        Color timeColor = getTimeColor(remainingTime); // 根据剩余时间改变颜色
        StdDraw.setPenColor(timeColor);
        String timeText = String.format("%02d:%02d", remainingTime / 60, remainingTime % 60);
        StdDraw.text(WIDTH / 2.0 + 10, HEIGHT, "剩余时间：" + timeText);

        // 暂停与帮助界面显示
        if (showingHelp) { // 如果正在显示帮助，优先显示帮助提示
            StdDraw.setPenColor(Color.CYAN); // 使用不同颜色区分
            StdDraw.text(WIDTH/2.0, HEIGHT - 1, "帮助菜单 (按 H 关闭)");
        } else if (isPaused) { // 否则，如果暂停了，显示暂停提示
            StdDraw.setPenColor(Color.YELLOW);
            StdDraw.text(WIDTH/2.0, HEIGHT - 1, "游戏暂停中 (按 P 继续)");
        }

        // 显示绘制内容
        StdDraw.show();

        // 重置字体和颜色，防止影响后续渲染
        StdDraw.setFont(DEFAULT_FONT);
        StdDraw.setPenColor(DEFAULT_COLOR);
    }

    // 辅助方法：根据剩余时间获取颜色（最后 60 秒变黄，最后 30 秒变红）
    private Color getTimeColor(int remainingTime) {
        if (remainingTime <= 30) {
            return Color.RED;
        } else if (remainingTime <= 60) {
            return Color.YELLOW;
        }
        return Color.WHITE;
    }

    // 绘制帮助界面
    private void drawHelpScreen() {
        // 1. 绘制半透明背景层
        StdDraw.setPenColor(new Color(0, 0, 0, 150)); // 半透明黑色
        StdDraw.filledRectangle(WIDTH / 2.0, HEIGHT / 2.0, WIDTH / 2.0, HEIGHT / 2.0 - 1);

        // 2. 设置文字颜色和字体
        StdDraw.setPenColor(Color.WHITE);
        Font helpFont = new Font("黑体", Font.PLAIN, 22); // 使用稍大一点的字体
        StdDraw.setFont(helpFont);

        // 3. 绘制帮助信息文本
        double startY = HEIGHT * 0.85; // 从屏幕上方开始绘制
        double lineSpacing = 2.0;     // 行间距

        StdDraw.text(WIDTH / 2.0, startY, "== 游戏帮助 ==");
        startY -= lineSpacing * 1.5; // 标题后多空一点

        StdDraw.textLeft(WIDTH * 0.15, startY, "- 主要玩法：");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  <1> 使用 W/A/S/D 移动角色 (O)。");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  <2> 找到钥匙 (★)，用钥匙打开锁着的门 ( █ )。");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  <3> 进入打开的门以进入下一关，通过所有关卡即可获胜。");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  <4> 拾取金币 ($) 以获得分数（关卡越靠后分数越高）！");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  <5> 踩中事件点 (?) 有惊喜！");

        startY -= lineSpacing * 1.5; // 分隔

        StdDraw.textLeft(WIDTH * 0.15, startY, "- 按键说明：");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  Q : 直接退出游戏。");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  O : 保存当前游戏进度。");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  L : 加载上次保存的进度。");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  P : 暂停 或 继续 游戏。");
        startY -= lineSpacing;
        StdDraw.textLeft(WIDTH * 0.20, startY, "  H : 打开/关闭 此帮助菜单。");

        startY -= lineSpacing * 2; // 底部提示
        StdDraw.setFont(new Font("黑体", Font.BOLD, 24));
        StdDraw.text(WIDTH / 2.0, startY, "按 H 键关闭帮助");

        // 4. 显示绘制内容
        StdDraw.show();

        // 5. 重置字体和颜色
        StdDraw.setFont(DEFAULT_FONT);
        StdDraw.setPenColor(DEFAULT_COLOR);
    }

    // 保存游戏
    private void saveGame() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE))) {
            SaveData data = new SaveData(
                    Long.parseLong(seedInput.toString()),
                    levelManager.getScore(),
                    levelManager.getCurrentLevel(),
                    levelManager.getRemainingTime()
            );
            oos.writeObject(data);
            levelManager.drawMessage("游戏已保存", Color.GREEN);
        } catch (IOException e) {
            levelManager.drawMessage("保存失败", Color.RED);
            e.printStackTrace();
        }
    }

    // 加载游戏
    private boolean loadGame() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(SAVE_FILE))) {

            SaveData data = (SaveData) ois.readObject();
            levelManager = new LevelManager(WIDTH, HEIGHT, data.getSeed());
            levelManager.setCurrentLevel(data.getCurrentLevel());
            levelManager.setScore(data.getScore());
            levelManager.setRemainingTime(data.getRemainingTime());

            gameStarted = true;
            waitingForSeed = false;
            renderWorldWithHUD();
            levelManager.drawMessage("存档已加载", Color.GREEN);
            return true;
        } catch (Exception e) {
            levelManager.drawMessage("加载存档失败", Color.RED);
            return false;
        }
    }
    /*
     * 处理字符串输入（例如 "N123S"），生成世界并返回
     * 模拟键盘模式的操作（N → 输入种子 → S），但以非交互的方式执行
     */
    public TETile[][] interactWithInputString(String input) {
        // 处理字符串输入，例如 "N123S"
        StringBuilder seed = new StringBuilder();
        for (char c : input.toCharArray()) { // 收集字符串中的数字部分（种子值）
            if (Character.isDigit(c)) {
                seed.append(c);
            } else if (c == 'S' || c == 's') { // 如果到达结尾（s），解析种子并生成世界
                long seedValue = Long.parseLong(seed.toString());
                levelManager = new LevelManager(WIDTH, HEIGHT, seedValue); // 初始化关卡管理器
                return levelManager.getWorld(); // 返回生成的世界
            }
        }
        return null;
    }

    // 将世界转换为字符串表示
    public String toString() {
        if (levelManager == null || levelManager.getWorld() == null) {
            return "World not initialized.";
        }
        TETile[][] world = levelManager.getWorld(); // 获取 LevelManager 的世界
        StringBuilder sb = new StringBuilder();
        for (int y = HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < WIDTH; x++) {
                if (world[x][y] == null) {
                    sb.append(' ');
                } else if (world[x][y] == Tileset.WALL) {
                    sb.append('#');
                } else if (world[x][y] == Tileset.GRASS) {
                    sb.append('.');
                } else if (world[x][y] == Tileset.NOTHING) {
                    sb.append(' ');
                } else if (world[x][y] == Tileset.AVATAR) {
                    sb.append('@');
                } else {
                    sb.append('?');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}