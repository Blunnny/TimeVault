package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Engine {
    TERenderer ter = new TERenderer();
    /* 设定窗口的宽度和高度 */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     * wait for phase 2
     */
    public void interactWithKeyboard() {
        return;
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // 使用正则表达式表示符合 “N + SEED + S” 格式的输入（不区分大小写），代表生成新世界
        Pattern pattern = Pattern.compile("N(\\d+)S", Pattern.CASE_INSENSITIVE);
        // 寻找符合匹配条件的结果，储存在 matcher 中
        Matcher matcher = pattern.matcher(input);

        // 若存在匹配项，则创建对应 seed 世界
        if (matcher.find()) {
            // 提取第一个捕获组匹配的子字符串，并转换成 Long 类型储存在 seed 中
            long seed = Long.parseLong(matcher.group(1));
            // 创建 WorldGenerator 对象
            WorldGenerator generator = new WorldGenerator(WIDTH, HEIGHT, seed);
            // 生成世界，并返回生成的 TETile[][] 数组
            return generator.generateWorld(seed);
        }
        return new TETile[WIDTH][HEIGHT];
    }
}
