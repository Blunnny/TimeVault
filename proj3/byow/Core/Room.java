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
    public void generateSingleRooms(TETile[][] world, int width, int height, int locationX, int locationY, int roomWidth, int roomHeight) {
        // 初始赋值
        this.locationX = locationX;
        this.locationY = locationY;
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;

        // 判断房间是否存在重叠，若有重叠，则重新生成房间
        while (overlapping(world, width, height, locationX, locationY, roomWidth, roomHeight)) {
            int[] params = generateRandomRoomParams(width, height);
            locationX = params[0];
            locationY = params[1];
            roomWidth = params[2];
            roomHeight = params[3];
        }
        for (int i = 0; i < roomWidth; i++) {
            world[Math.min(width - 1, locationX + i)][locationY] = Tileset.WALL;
            world[Math.min(width - 1, locationX + i)][Math.min(height - 1, locationY + roomHeight + 1)] = Tileset.WALL;
        }
        for (int i = 0; i < roomHeight; i++) {
            world[locationX][Math.min(height - 1, locationY + i + 1)] = Tileset.WALL;
            world[Math.min(width - 1, locationX + roomWidth - 1)][Math.min(height - 1, locationY + i + 1)] = Tileset.WALL;

    }

    }

    // 辅助方法：判断方法是否存在重叠
    public boolean overlapping(TETile[][] world, int width, int height, int locationX, int locationY, int roomWidth, int roomHeight) {
        for (int i = 0; i < roomWidth; i++) {
            if (world[Math.min(width - 1, locationX + i)][locationY] != Tileset.NOTHING
                    ||  world[Math.min(width - 1, locationX + i)][Math.min(height - 1, locationY + roomHeight + 1)] != Tileset.NOTHING) {
                return true;
            }
        }
        for (int i = 0; i < roomHeight; i++) {
            if (world[locationX][Math.min(height - 1, locationY + i + 1)] != Tileset.NOTHING
                    || world[Math.min(width - 1, locationX + roomWidth - 1)][Math.min(height - 1, locationY + i + 1)] != Tileset.NOTHING) {
                return true;
            }
        }
        return false;
    }

    // 辅助方法：随机生成房间参数
    public int[] generateRandomRoomParams(int width, int height) {
        // 确定房间左上角的 X 坐标与 Y 坐标
        Random locX = new Random();
        int locationX = Math.max(0, locX.nextInt(width) - 3);  // 房间宽度（内部）至少为 3， X 坐标最大为倒数第五列
        Random locY = new Random();
        int locationY = Math.max(0, locY.nextInt(height) - 3);  // 房间高度（内部）至少为 3， Y 坐标最大为倒数第五行

        // 确定房间的大小，其中宽度与高度（内部）最小为 3， 最大为 10
        Random roomW = new Random();
        int roomWidth = roomW.nextInt(7) + 5;
        Random roomH = new Random();
        int roomHeight = roomH.nextInt(7) + 5;

        return new int[]{locationX, locationY, roomWidth, roomHeight};
    }

    // 辅助方法：获取房间中心点坐标
    public int[] getCenter() {
        int centerX = locationX + (roomWidth / 2);
        int centerY = locationY + (roomHeight / 2);
        return new int[]{centerX, centerY};
    }
}
