package byow.lab12;
import org.junit.Test;

import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * 绘制六边形
 */
public class HexWorld {
    // 确定窗口的宽度和高度
    private static final int WIDTH = 50;
    private static final int HEIGHT = 30;
    // 设定随机种子
    private static final long SEED = 124;
    // 随机数生成器
    private static final Random RANDOM = new Random(SEED);

    // 绘制单个六边形
    public void addHexagon(int x, int y, int s, TETile[][] world) {
        TETile[] tileTypes = {Tileset.WALL, Tileset.FLOOR, Tileset.GRASS};
        TETile randomTileType = tileTypes[RANDOM.nextInt(tileTypes.length)];

        Random random = new Random();
        // 绘制上半部分
        for (int j = 0; j < s; j++) {
            for (int i = 0; i < s + 2 * j; i++) {
                // 随机为 flower 中的元素选择颜色
                TETile randomTile = TETile.colorVariant(randomTileType, 50, 50, 50, random);
                world[x + (s - 1 - j) + i][y] = randomTile;
            }
            y += 1;
        }
        // 绘制下半部分
        for (int j = s - 1; j >= 0; j--) {
            for (int i = 0; i < s + 2 * j; i++) {
                // 随机为 flower 中的元素选择颜色
                TETile randomTile = TETile.colorVariant(randomTileType, 50, 50, 50, random);
                world[x + (s - 1 - j) + i][y] = randomTile;
            }
            y += 1;
        }
    }

    // 围绕六边形绘制一圈六边形
    public void combHexagon(int x, int y, int s, TETile[][] world){
        // 绘制中心六边形
        addHexagon(x, y, s, world);
        // 上方六边形
        addHexagon(x, y + s * 2, s, world);
        // 右上方六边形
        addHexagon(x + s * 2 - 1, y + s, s, world);
        // 右下方六边形
        addHexagon(x + s * 2 - 1, y - s, s, world);
        // 上方六边形
        addHexagon(x, y - s * 2, s, world);
        // 左下方六边形
        addHexagon(x - s * 2 + 1, y - s, s, world);
        // 左上方六边形
        addHexagon(x - s * 2 + 1, y + s, s, world);
    }


    public static void main(String[] args){
        // 初始化渲染器
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        // 创建世界数组实例
        TETile[][] world = new TETile[WIDTH][HEIGHT];

        // 初始化世界数组为 NOTHING
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        // 绘制六边形
        HexWorld hexWorld = new HexWorld();
        // hexWorld.addHexagon(25, 10, 3, world);
        hexWorld.combHexagon(25, 10, 3, world);

        // 渲染显示
        ter.renderFrame(world);
    }
}
