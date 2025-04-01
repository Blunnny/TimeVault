package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
    * 类 WorldGenerator 用于生成世界
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
        generateRooms();
        // 连接房间并生成走廊
        generateHallways();

        return world;
    }

    // 生成地图中的全部房间
    public void generateRooms() {
        // 设置地图房间数为随机 25 - 30
        Random roomNum = new Random();
        int ranRoomNum = roomNum.nextInt(1) + 10;
        // 生成房间
        for (int i = 0; i < ranRoomNum; i++) {
            // 创建房间
            Room room = new Room();
            // 随机设定房间参数
            int[] params = room.generateRandomRoomParams(width, height);
            // 根据参数绘制该房间
            room.generateSingleRooms(world, width, height, params[0], params[1], params[2], params[3]);
            // 将房间添加到列表
            rooms.add(room);
        }
    }

    // 使用 MST 方法获取房间最优连接方法（走廊长度最短）
    public void generateHallways() {
        RoomEdgeMST mstGenerator = new RoomEdgeMST(rooms);
        List<RoomEdgeMST.Edge> mstEdges = mstGenerator.getMST();
        // mstGenerator.printMST(); // 打印测试

        // 根据连接方法依次创建走廊
        for (RoomEdgeMST.Edge edge : mstEdges) {
            createHallway(edge.room1, edge.room2);
        }
    }

    // 根据 room1 和 room2 的位置和大小生成走廊
    public void createHallway(Room room1, Room room2) {
        // 获取房间的边界信息
        int x1Left = room1.locationX;
        int x1Right = room1.locationX + room1.roomWidth - 1;
        int y1Bottom = room1.locationY;
        int y1Top = room1.locationY + room1.roomHeight - 1;

        int x2Left = room2.locationX;
        int x2Right = room2.locationX + room2.roomWidth - 1;
        int y2Bottom = room2.locationY;
        int y2Top = room2.locationY + room2.roomHeight - 1;

//        if (y1Bottom < y2Top - 1) {
//            int randomNumber = random.nextInt(y2Top - y1Bottom - 1) + y1Bottom;
//
//        }
//
//        if (y1Bottom < y2Top || y1Top > y2Bottom) {
//            int randomNumber = random.nextInt(max - min - 1) + min;
//        }

        // 生成 room1 的四个边缘中点
        List<int[]> room1Edges = new ArrayList<>();
        room1Edges.add(new int[]{x1Right, (y1Bottom + y1Top) / 2}); // 东
        room1Edges.add(new int[]{x1Left, (y1Bottom + y1Top) / 2});  // 西
        room1Edges.add(new int[]{(x1Left + x1Right) / 2, y1Top});    // 北
        room1Edges.add(new int[]{(x1Left + x1Right) / 2, y1Bottom}); // 南

        // 生成 room2 的四个边缘中点
        List<int[]> room2Edges = new ArrayList<>();
        room2Edges.add(new int[]{x2Right, (y2Bottom + y2Top) / 2}); // 东
        room2Edges.add(new int[]{x2Left, (y2Bottom + y2Top) / 2});  // 西
        room2Edges.add(new int[]{(x2Left + x2Right) / 2, y2Top});    // 北
        room2Edges.add(new int[]{(x2Left + x2Right) / 2, y2Bottom}); // 南

        // 寻找曼哈顿距离最小的边缘点对
        int minDistance = Integer.MAX_VALUE;
        int[] bestStart = null;
        int[] bestEnd = null;

        for (int[] edge1 : room1Edges) {
            for (int[] edge2 : room2Edges) {
                int dx = Math.abs(edge1[0] - edge2[0]);
                int dy = Math.abs(edge1[1] - edge2[1]);
                int distance = dx + dy;
                if (distance < minDistance) {
                    minDistance = distance;
                    bestStart = edge1;
                    bestEnd = edge2;
                }
            }
        }

        if (bestStart == null || bestEnd == null) {
            return; // 没有找到有效路径
        }

        // 绘制横向路径部分
        int startX = bestStart[0];
        int startY = bestStart[1];
        int endX = bestEnd[0];
        int endY = bestEnd[1];

        // 横向部分（从 startX 到 endX，固定 y 为 startY）
        int xStart = Math.min(startX, endX);
        int xEnd = Math.max(startX, endX);
        for (int x = xStart; x <= xEnd; x++) {
            if (x >= 0 && x < width && startY >= 0 && startY < height) {
                world[x][startY] = Tileset.FLOOR;
            }
        }

        // 纵向部分（从 startY 到 endY，固定 x 为 endX）
        int yStart = Math.min(startY, endY);
        int yEnd = Math.max(startY, endY);
        for (int y = yStart; y <= yEnd; y++) {
            if (endX >= 0 && endX < width && y >= 0 && y < height) {
                world[endX][y] = Tileset.FLOOR;
            }
        }

        // 确保连接点处的墙壁被替换为地板（开口）
        world[bestStart[0]][bestStart[1]] = Tileset.FLOOR;
        world[bestEnd[0]][bestEnd[1]] = Tileset.FLOOR;
    }
}

