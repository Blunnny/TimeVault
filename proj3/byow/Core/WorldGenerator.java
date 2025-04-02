package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
    * 生成世界
 */
public class WorldGenerator {
    // 类属性
    private int width;
    private int height;
    private TETile[][] world;
    private List<Room> rooms = new ArrayList<>();
    private Random random;


    // 创建实例
    public WorldGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.world = new TETile[width][height];
        this.random = new Random(seed);
    }

    // 根据种子初始化并返回世界
    public TETile[][] generateWorld(long seed) {
        // 使用空白填充，初始化世界
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        // 生成房间
        generateRooms(seed);
        // 连接房间并生成走廊
        generateHallways();
        // 创建墙壁
        createWall();
        return world;
    }

    // 生成地图中的全部房间
    public void generateRooms(long seed) {
        // 根据给定的种子，设置地图房间数为随机 15 - 20
        int ranRoomNum = random.nextInt(6) + 15;
        // 生成房间
        for (int i = 0; i < ranRoomNum; i++) {
            // 创建房间
            Room room = new Room();
            // 随机设定房间参数
            int[] params = room.generateRandomRoomParams(width, height, random);
            // 根据参数绘制该房间
            room.generateSingleRooms(world, width, height, params[0], params[1], params[2], params[3], random);
            // 将房间添加到列表
            rooms.add(room);
        }
    }

    // 使用 MST 方法获取房间最优连接方法（走廊长度最短）并进行连接
    public void generateHallways() {
        RoomEdgeMST mstGenerator = new RoomEdgeMST(rooms);
        List<RoomEdgeMST.Edge> mstEdges = mstGenerator.getMST();
        // mstGenerator.printMST(); // 打印测试，确认 MST 边数量

        for (RoomEdgeMST.Edge edge : mstEdges) {
            createHallway(edge.getRoom1(), edge.getRoom2());
        }
    }

    // 连接两个房间，走廊长度为 2
    public void createHallway(Room room1, Room room2) {
        int room1X = room1.getCenter()[0];
        int room1Y = room1.getCenter()[1];
        int room2X = room2.getCenter()[0];
        int room2Y = room2.getCenter()[1];

        // 确保 room1X <= room2X，简化后续逻辑
        if (room1X > room2X) {
            // 交换 room1 和 room2 的坐标
            int tempX = room1X;
            int tempY = room1Y;
            room1X = room2X;
            room1Y = room2Y;
            room2X = tempX;
            room2Y = tempY;
        }

        // 水平段：从 room1X 到 room2X，Y 坐标保持 room1Y
        for (int x = room1X; x <= room2X; x++) {
            // 宽度为 2，绘制两条并行草地
            if (room1Y >= 0 && room1Y < height) {
                world[x][room1Y] = Tileset.GRASS;
            }
            if (room1Y + 1 >= 0 && room1Y + 1 < height) {
                world[x][room1Y + 1] = Tileset.GRASS;
            }
        }

        // 垂直段：从 room1Y 到 room2Y，X 坐标保持 room2X
        int startY = Math.min(room1Y, room2Y);
        int endY = Math.max(room1Y, room2Y);
        for (int y = startY; y <= endY; y++) {
            // 宽度为 2，绘制两条并行草地
            if (room2X >= 0 && room2X < width) {
                world[room2X][y] = Tileset.GRASS;
            }
            if (room2X + 1 >= 0 && room2X + 1 < width) {
                world[room2X + 1][y] = Tileset.GRASS;
            }
        }
    }

    // 沿着房间与走廊边缘生成墙壁
    public void createWall() {
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                if (world[x][y] == Tileset.NOTHING && ifWall(x, y)) {
                    world[x][y] = Tileset.WALL;
                }
            }
        }
    }

    // 辅助方法：判断是否应该生成墙壁
    public boolean ifWall(int x, int y) {
        // 方向数组：左、右、上、下、左上、右上、左下、右下
        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
        };

        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            // 只在 newX, newY 在边界内时才检查
            if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                if (world[newX][newY] == Tileset.GRASS) {
                    return true;
                }
            }
        }
        return false;
    }


}

