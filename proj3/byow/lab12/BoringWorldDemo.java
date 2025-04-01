package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

/**
 *  Draws a world that is mostly empty except for a small region.
 */
public class BoringWorldDemo {

    // 确定窗口的宽度和高度
    private static final int WIDTH = 70;
    private static final int HEIGHT = 40;

    public static void main(String[] args) {
        // 初始化渲染器
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        // 初始化世界数组
        TETile[][] world = new TETile[WIDTH][HEIGHT];

        // 默认使用空白填充
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        // 部分区域使用 “wall” 瓦片填充
        for (int x = 20; x < 55; x += 1) {
            for (int y = 5; y < 20; y += 1) {
                world[x][y] = Tileset.FLOOR;
            }
        }

        // 渲染显示
        ter.renderFrame(world);
    }


}
