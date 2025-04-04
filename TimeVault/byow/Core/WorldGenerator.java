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
    public TETile[][] generateWorld(long seed, int level) {
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
        // 根据关卡数生成金币
        generateCoins(level);
        // 生成随机事件
        generateRandomEvents(level);
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

    // 生成地图中的全部金币
    private void generateCoins(int level) {
        // 基础金币数量 + 每关增加的数量
        int baseCoins = 10; // 基础金币数量为 10
        int[] values = {1, 2, 3, 4, 5}; // 每加 1 关随机增加 1-5 枚金币
        int coinsPerLevel = values[random.nextInt(values.length)];
        int coinCount = baseCoins + coinsPerLevel * level;

        int coinsPlaced = 0; // 已放置的金币数
        int attempts = 0; // 尝试放置次数
        int maxAttempts = 200; // 防止无限循环，最多尝试上限为 100

        while (coinsPlaced < coinCount && attempts < maxAttempts) {
            attempts++;
            int x = random.nextInt(width);
            int y = random.nextInt(height);

            // 只在草地上放置金币，且不与其他金币重叠
            if (world[x][y] == Tileset.GRASS && !isAdjacentToCoin(x, y)) {
                world[x][y] = Tileset.COIN;
                coinsPlaced++;
            }
        }
    }

    // 辅助方法：检查周围是否有金币，避免金币过于集中
    private boolean isAdjacentToCoin(int x, int y) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (world[nx][ny] == Tileset.COIN) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    // 生成随机事件
    private void generateRandomEvents(int level) {
        // 每关生成 1-3个随机事件
        int eventCount = 1 + random.nextInt(3);

        int eventsPlaced = 0;
        int attempts = 0;
        int maxAttempts = 500;

        while (eventsPlaced < eventCount && attempts < maxAttempts) {
            attempts++;
            int x = random.nextInt(width);
            int y = random.nextInt(height);

            // 只在草地上放置随机事件，且不与其他特殊图块相邻
            if (world[x][y] == Tileset.GRASS && !isNearSpecialTile(x, y)) {
                world[x][y] = Tileset.EVENT;
                eventsPlaced++;
            }
        }
    }

    // 辅助方法：检查周围是否有钥匙、金币或其他随机事件
    private boolean isNearSpecialTile(int x, int y) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    TETile tile = world[nx][ny];
                    if (tile == Tileset.KEY || tile == Tileset.COIN || tile == Tileset.EVENT ||
                            tile == Tileset.LOCKED_DOOR || tile == Tileset.UNLOCKED_DOOR) {
                        return true;
                    }
                }
            }
        }
        return false;
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

