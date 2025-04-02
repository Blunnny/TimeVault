package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class Engine {
    // 创建 TERenderer 实例
    TERenderer ter = new TERenderer();
    // 设定世界的宽和高
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;
    // 创建 world, 用于存储由 WorldGenerator 生成的世界
    private TETile[][] world;
    // 收集用户输入的种子值
    private StringBuilder seedInput = new StringBuilder();
    // 标记当前是否处于 “等待用户输入种子” 的状态
    private boolean waitingForSeed = false;
    // 标记游戏是否已经开始
    private boolean gameStarted = false;

    // 默认字体和颜色，用于重置
    private static final Font DEFAULT_FONT = new Font("Monaco", Font.PLAIN, 14);
    private static final Color DEFAULT_COLOR = Color.WHITE;

    // 玩家位置
    private int playerX;
    private int playerY;
    private Random random; // 用于随机选择玩家初始位置
    
    // 初始化渲染器
    public Engine() {
        ter.initialize(WIDTH, HEIGHT + 2); // 额外 2 行（顶部）给 HUD 指示栏
    }

    /*
     * 处理键盘交互模式，程序的主循环。
     * 初始显示开始界面，监听用户输入，根据状态更新 UI。
     */
    public void interactWithKeyboard() {
        drawStartScreen(); // 调用方法显示初始界
        while (true) { // 持续监听用户输入
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                handleKey(key); // 根据按下的键更新程序状态
                if (gameStarted) { // 如果游戏已开始，则渲染世界并绘制 HUD
                    renderWorldWithHUD();
                }
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
                WorldGenerator generator = new WorldGenerator(WIDTH, HEIGHT, seed); // 创建 WorldGenerator 实例
                world = generator.generateWorld(seed); // 生成世界，存储到 world
                random = new Random(seed); // 初始化随机数生成器
                initializePlayer(); // 初始化玩家位置
                gameStarted = true; // 标记游戏开始
                waitingForSeed = false; // 退出种子输入模式
            }
        } else if (gameStarted) { // 处理游戏开始后的键盘输入
            if (key == 'Q' || key == 'q') { // 按 Q 可以退出程序 ***待在游戏界面添加提示
                System.exit(0);
            } else if (key == 'W' || key == 'w') { // 向上移动一格
                movePlayer(0, 1);
            } else if (key == 'A' || key == 'a') { // 向左移动一格
                movePlayer(-1, 0);
            } else if (key == 'S' || key == 's') { // 向下移动一格
                movePlayer(0, -1);
            } else if (key == 'D' || key == 'd') { // 向右移动一格
                movePlayer(1, 0);
            }
        }
    }

    // 初始化玩家位置，随机选择一个草地位置作为玩家初始位置
    private void initializePlayer() {
        boolean placed = false;
        while (!placed) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            if (world[x][y] == Tileset.GRASS) {
                world[x][y] = Tileset.AVATAR;
                playerX = x;
                playerY = y;
                placed = true;
            }
        }
    }

    // 移动玩家位置
    private void movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;
        // 检查新位置是否在边界内且可通行（草地）
        if (world[newX][newY] == Tileset.GRASS) {
            // 清除当前位置（恢复为草地）
            world[playerX][playerY] = Tileset.GRASS;
            // 更新新位置为玩家
            world[newX][newY] = Tileset.AVATAR;
            // 更新玩家坐标
            playerX = newX;
            playerY = newY;
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

        ter.renderFrame(world); // 渲染世界
        drawHUD(); // 绘制 HUD
    }

    //???
    private void drawHUD() {
        // 获取鼠标位置
        double mouseX = StdDraw.mouseX();
        double mouseY = StdDraw.mouseY();

        // 转换为世界坐标
        int tileX = (int) mouseX;
        int tileY = (int) mouseY;

        // 获取鼠标下的图块
        String tileDescription = "Nothing";
        if (tileX >= 0 && tileX < WIDTH && tileY >= 0 && tileY < HEIGHT) {
            TETile tile = world[tileX][tileY];
            if (tile == Tileset.WALL) {
                tileDescription = "Wall";
            } else if (tile == Tileset.GRASS) {
                tileDescription = "Grass";
            }
        }

        // 绘制 HUD（在世界网格上方）
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(WIDTH / 2.0, HEIGHT + 1, WIDTH / 2.0, 1);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2.0, HEIGHT + 1, "Tile: " + tileDescription);
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
                WorldGenerator generator = new WorldGenerator(WIDTH, HEIGHT, seedValue);
                world = generator.generateWorld(seedValue);
                random = new Random(seedValue); // 初始化随机数生成器
                initializePlayer(); // 初始化玩家位置
                break;
            }
        }
        return world;
    }

    // 将世界转换为字符串表示
    public String toString() {
        if (world == null) {
            return "World not initialized.";
        }
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