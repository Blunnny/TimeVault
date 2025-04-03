package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.LinkedList;
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

    // 关卡管理器
    private LevelManager levelManager;

    // 初始化渲染器
    public Engine() {
        ter.initialize(WIDTH, HEIGHT + 2); // 额外 2 行（顶部）给 HUD 指示栏
    }

    /*
     * 处理键盘交互模式，程序的主循环。
     * 初始显示开始界面，监听用户输入，根据状态更新 UI。
     */
    public void interactWithKeyboard() {
        drawStartScreen(); // 调用方法显示初始界面

        while (true) { // 持续监听用户输入
            long currentTime = System.currentTimeMillis();
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                // 在初始界面或种子输入阶段，直接处理按键，不使用队列
                if (!gameStarted) {
                    handleKey(key);
                    if (waitingForSeed) {
                        drawSeedInputScreen(); // 实时更新种子输入界面
                    }
                } else {
                    // 游戏开始后，使用队列和节流机制处理移动指令
                    long inputTime = System.currentTimeMillis();
                    if (inputTime - lastInputTime >= INPUT_DELAY_MS) {
                        if (commandQueue.size() < MAX_QUEUE_SIZE) {
                            commandQueue.offer(key);
                        }
                        lastInputTime = inputTime;
                    }
                }
            }

            // 游戏开始后，执行队列中的指令
            if (gameStarted && currentTime - lastExecuteTime >= EXECUTE_DELAY_MS && !commandQueue.isEmpty()) {
                char key = commandQueue.poll(); // 取出并移除队列头部指令
                handleKey(key);
                renderWorldWithHUD();
                lastExecuteTime = currentTime;
            }
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
            if (Character.isDigit(key)) { // 如果用户输入的是数字，则将数字存入 seedInput
                seedInput.append(key);
                drawSeedInputScreen();
            } else if (key == 'S' || key == 's') { // 如果用户按下 S 或 s，确认种子并生成世界
                long seed = Long.parseLong(seedInput.toString()); // 将输入的字符串（例如 "123"）转换为 long 类型的种子值
                levelManager = new LevelManager(WIDTH, HEIGHT, seed); // 初始化关卡管理器
                gameStarted = true; // 标记游戏开始
                waitingForSeed = false; // 退出种子输入模式
                renderWorldWithHUD(); // 立即渲染世界和 HUD
            }
        } else if (gameStarted) { // 处理游戏开始后的键盘输入
            if (key == 'Q' || key == 'q') { // 按 Q 可以退出程序 ***待在游戏界面添加提示
                System.exit(0);
            } else if (key == 'W' || key == 'w') { // 向上移动一格
                levelManager.movePlayer(0, 1);
            } else if (key == 'A' || key == 'a') { // 向左移动一格
                levelManager.movePlayer(-1, 0);
            } else if (key == 'S' || key == 's') { // 向下移动一格
                levelManager.movePlayer(0, -1);
            } else if (key == 'D' || key == 'd') { // 向右移动一格
                levelManager.movePlayer(1, 0);
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

        Font fontState = new Font("黑体", Font.PLAIN, 30);
        StdDraw.setFont(fontState);
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 - 10, "按'S'键开始游戏");
        StdDraw.show();

        // 重置字体和颜色，防止影响后续渲染
        StdDraw.setFont(DEFAULT_FONT);
        StdDraw.setPenColor(DEFAULT_COLOR);
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
        Font font = new Font("站酷酷黑", Font.PLAIN, 30);
        StdDraw.setFont(font);

        // 1. 在正上方（中心）显示当前关卡数
        String levelText = "第 " + levelManager.getCurrentLevel() + " / 10 关 ";
        StdDraw.text(WIDTH / 2.0, HEIGHT, levelText);
        // 2. 在右上方显示“按 Q 键退出游戏”
        String quitText = "按 Q 键退出游戏";
        StdDraw.text(WIDTH - 10.0, HEIGHT, quitText); // 靠右显示，距离右边缘 10 个单位

        // 倒计时功能待完成
        // todo

        // 显示绘制内容
        StdDraw.show();

        // 重置字体和颜色，防止影响后续渲染
        StdDraw.setFont(DEFAULT_FONT);
        StdDraw.setPenColor(DEFAULT_COLOR);
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