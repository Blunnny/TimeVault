package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world that contains RANDOM tiles.
 */
public class RandomWorldDemo {
    // 确定窗口的高度和宽度
    private static final int WIDTH = 160;
    private static final int HEIGHT = 100;
    // 设定随机种子
    private static final long SEED = 12341245;
    // 随机数生成器
    private static final Random RANDOM = new Random(SEED);

    /**
     * 使用随机瓦片填充窗口
     * @param tiles
     */
    public static void fillWithRandomTiles(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = randomTile();
            }
        }
    }

    /** 返回一个随机的瓦片类型，其中 wall、flower、empty的可能性各为 33%
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(3);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.NOTHING;
            default: return Tileset.NOTHING;
        }
    }

    public static void main(String[] args) {
        // 初始化渲染器
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        // 创建空的瓦片数组
        TETile[][] randomTiles = new TETile[WIDTH][HEIGHT];
        // 使用瓦片填充数组
        fillWithRandomTiles(randomTiles);
        // 渲染显示
        ter.renderFrame(randomTiles);
    }


}
