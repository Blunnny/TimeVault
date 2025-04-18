package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/*
    * 房间相关的各类方法
 */
public class Room {
    // 房间属性
    int locationX;
    int locationY;
    int roomWidth;
    int roomHeight;

    // 默认构造函数
    public Room() {
        // 留空，属性将在 generateSingleRooms 中设置
    }

    public Room(int locationX, int locationY, int roomWidth, int roomHeight) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
    }


    // 根据信息生成房间
    public void generateSingleRooms(TETile[][] world, int width, int height, int locationX, int locationY, int roomWidth, int roomHeight, Random random) {
        // 判断房间是否存在重叠或超出边界，若有，则重新生成参数
        while (overlapping(world, locationX, locationY, roomWidth, roomHeight)
        || beyondBorder(width, height, locationX, locationY, roomWidth, roomHeight)) {
            int[] params = generateRandomRoomParams(width, height, random);
            locationX = params[0];
            locationY = params[1];
            roomWidth = params[2];
            roomHeight = params[3];
        }
        // 赋值
        this.locationX = locationX;
        this.locationY = locationY;
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;

        // 生成房间
        for (int i = 0; i < roomWidth; i++) {
            for (int j = 0; j < roomHeight; j++) {
                world[locationX + i][locationY + j] = Tileset.GRASS;
            }
        }
    }

    // 辅助方法 1：判断房间是否存在重叠
    public boolean overlapping(TETile[][] world, int locationX, int locationY, int roomWidth, int roomHeight) {
        for (int i = 0; i < roomWidth; i++) {
            for (int j = 0; j < roomHeight; j++) {
                if (world[locationX + i][locationY + j] != Tileset.NOTHING) {
                    return true;
                }
            }
        }
        return false;
    }

    // 辅助方法 2：判断房间是否超出边界
    public boolean beyondBorder(int width, int height, int locationX, int locationY, int roomWidth, int roomHeight) {
        for (int i = 0; i < roomWidth; i++) {
            for (int j = 0; j < roomHeight; j++) {
                if (locationX + i >= width || locationY + j >= height) {
                    return true;
                }
            }
        }
        return false;
    }

    // 辅助方法 3：随机生成房间参数
    public int[] generateRandomRoomParams(int width, int height, Random random) {
        // 房间大小（内部），考虑外墙后，最大宽度和高度要减去 2
        int maxRoomWidth = Math.min(10, width - 2);  // 最大宽度受地图限制
        int maxRoomHeight = Math.min(10, height - 2); // 最大高度受地图限制
        int roomWidth = random.nextInt(Math.min(6, maxRoomWidth - 4)) + 5;  // 5 到 10，但不超过地图边界
        int roomHeight = random.nextInt(Math.min(6, maxRoomHeight - 4)) + 5; // 5 到 10，但不超过地图边界

        // 房间位置，确保留出外墙空间
        int locationX = random.nextInt(width - roomWidth - 2) + 1;  // 0 到 width - roomWidth - 1
        int locationY = random.nextInt(height - roomHeight - 2) + 1; // 0 到 height - roomHeight - 5

        return new int[]{locationX, locationY, roomWidth, roomHeight};
    }

    // 辅助方法 4：获取房间中心点坐标
    public int[] getCenter() {
        int centerX = locationX + (roomWidth / 2);
        int centerY = locationY + (roomHeight / 2);
        return new int[]{centerX, centerY};
    }
}
