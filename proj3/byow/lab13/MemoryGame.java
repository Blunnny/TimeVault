package byow.lab13;

import byow.Core.RandomUtils;
import byow.StdDraw;


import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** 界面宽度 */
    private int width;
    /** 界面高度 */
    private int height;
    /** 玩家当前轮次 */
    private int round;
    /** 随机生成字符串 */
    private Random rand;
    /** 标记游戏是否结束 */
    private boolean gameOver;
    /** 标记是否轮到玩家操作（用于后续的“Helpful UI”功能） */
    private boolean playerTurn;
    /** 可用字符集（小写字母 a-z） */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** 鼓励性短语数组（用于后续 UI 增强） */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    // 接受命令行参数（种子值），创建 MemoryGame 实例并启动游戏
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        this.rand = new Random(seed);
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    // 生成一个长度为 n 的随机字符串，字符从 CHARACTERS 中选取
    public String generateRandomString(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int index = rand.nextInt(CHARACTERS.length);  // 随机选择一个字符索引
            sb.append(CHARACTERS[index]);
        }
        return sb.toString();
    }

    // * 如果游戏未结束 (!gameOver)，在顶部显示相关信息（如当前轮次）。
    private void drawRoundInfo() {
        StdDraw.setPenColor(Color.WHITE);
        String info = "Round: " + round;
        StdDraw.text(width / 2.0, height - 1, info);
        StdDraw.show();
    }


    // 绘制中心内容，不清屏整个屏幕
    public void drawFrame(String s) {
        // 只清除中心区域（避免影响顶部信息）
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(width / 2.0, height / 2.0, width / 2.0, height / 2.0 - 2);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(width / 2.0, height / 2.0, s);
        StdDraw.show();
    }

    // 逐个显示 letters 中的字符，每次显示后清屏并短暂暂停
    public void flashSequence(String letters) {
        for (char c : letters.toCharArray()) {
            drawFrame(String.valueOf(c));
            StdDraw.pause(1000);
            StdDraw.clear(Color.BLACK); // 这里仍然全屏清空，因为是序列显示阶段
            drawRoundInfo(); // 重新绘制顶部信息
            StdDraw.show();
            StdDraw.pause(500);
        }
    }

    // 读取玩家输入的 n 个字符并返回
    public String solicitNCharsInput(int n) {
        StringBuilder input = new StringBuilder();
        while (input.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                input.append(c);
                drawFrame(input.toString()); // 只更新中心内容，顶部信息保持不变
            }
        }
        return input.toString();
    }

    // 初始化游戏并运行主循环
    public void startGame() {
        round = 1;
        gameOver = false;

        while (!gameOver) {
            StdDraw.clear(Color.BLACK); // 每轮开始清屏一次
            drawRoundInfo(); // 显示顶部信息
            drawFrame("Round: " + round + " - Watch the sequence!");
            StdDraw.pause(1000);

            String target = generateRandomString(round);
            flashSequence(target);
            drawFrame("Your turn! Type the sequence:");

            String input = solicitNCharsInput(round);
            if (input.equals(target)) {
                int encourageIdx = rand.nextInt(ENCOURAGEMENT.length);
                drawFrame("Correct! " + ENCOURAGEMENT[encourageIdx]);
                StdDraw.pause(1000);

                drawFrame("Next round!");
                StdDraw.pause(1000);

                drawFrame("ready? ");
                for (int i = 3; i > 0; i--) {
                    drawFrame("" + i);
                    StdDraw.pause(1000);
                }
                round++;
            } else {
                gameOver = true;
                drawFrame("Game Over! Final Round: " + round);
                StdDraw.pause(2000);
            }
        }
    }
}