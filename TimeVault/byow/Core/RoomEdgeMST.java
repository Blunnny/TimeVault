package byow.Core;

import java.util.*;
/*
使用 Kruskal 算法获取房间的连接方式
 */

public class RoomEdgeMST {
    private List<Room> rooms; // 存放房间列表
    private List<Edge> edges; // 存放所有可能的边
    private int[] parent; // 并查集的数组，用于跟踪每个房间所属的集合
    private List<Edge> mst; // 存储最小生成树的边，用于生成走廊


    // 创建 Edge 类并实例化
    class Edge {
        int roomIndex1; // 使用存储索引
        int roomIndex2;
        int distance;

        Edge(int roomIndex1, int roomIndex2, int distance) {
            this.roomIndex1 = roomIndex1;
            this.roomIndex2 = roomIndex2;
            this.distance = distance;
        }

        @Override
        public String toString() {
            Room room1 = rooms.get(roomIndex1);
            Room room2 = rooms.get(roomIndex2);
            return "Edge{" + "room1=" + room1.getCenter()[0] + "," + room1.getCenter()[1] +
                    ", room2=" + room2.getCenter()[0] + "," + room2.getCenter()[1] +
                    ", distance=" + distance + "}";
        }

        public Room getRoom1() {
            return rooms.get(roomIndex1);
        }

        public Room getRoom2() {
            return rooms.get(roomIndex2);
        }
    }

    // 接收外部传入的 rooms 列表, 并调用 generateEdges 方法创建所有的边
    public RoomEdgeMST(List<Room> rooms) {
        this.rooms = rooms;
        this.edges = new ArrayList<>();
        this.mst = new ArrayList<>();
        this.parent = new int[rooms.size()]; // rooms.size() 表示数组大小
        // 初始时每个房间相互独立
        for (int i = 0; i < rooms.size(); i++) {
            parent[i] = i;
        }
        generateEdges(); // 生成所有边
        generateMST(); // 生成 MST
    }

    // 生成所有房间之边并添加到 edges 列表
    private void generateEdges() {
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                Room room1 = rooms.get(i);
                Room room2 = rooms.get(j);
                int distance = centerDistance(room1, room2);
                edges.add(new Edge(i, j, distance));
            }
        }
    }

    // 使用 Kruskal 算法逐个连通房间
    private void generateMST() {
        Collections.sort(edges, Comparator.comparingInt(e -> e.distance));
        for (Edge edge : edges) {
            int index1 = edge.roomIndex1;
            int index2 = edge.roomIndex2;
            if (find(index1) != find(index2)) {
                union(index1, index2);
                mst.add(edge);
            }
            if (mst.size() == rooms.size() - 1) {
                break;
            }
        }
    }


    // 辅助方法 1：计算并返回两点间的曼哈顿距离
    public int centerDistance(Room room1, Room room2) {
        int[] center1 = room1.getCenter();
        int[] center2 = room2.getCenter();
        int x1 = center1[0];
        int y1 = center1[1];
        int x2 = center2[0];
        int y2 = center2[1];
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    // 辅助方法 2：查找房间 x 的根节点
    private int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    // 辅助方法 3：合并房间 x 和 y （合并根节点）
    private void union(int x, int y) {
        parent[find(x)] = find(y);
    }

    // 辅助方法 4：获取 MST 的边列表
    public List<Edge> getMST() {
        return mst;
    }

    // 辅助方法 5：打印 MST 的边列表
    public void printMST() {
        System.out.println("Minimum Spanning Tree Edges:");
        for (Edge edge : mst) {
            System.out.println(edge);
        }
    }
}
