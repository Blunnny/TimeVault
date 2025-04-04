package byow.Core;

import byow.Core.Engine;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

/*
    WorldGeneratorTest 用于测试 WorldGenerator 生成世界的效果
 */
public class WorldGeneratorTest {
    public static void main(String args[]) {
        // 创建 engine 实例
        Engine engine = new Engine();
        // 指定种子
        String input = "N3336775S";
        // 使用 interactWithInputString 方法获取生成的地图
        TETile[][] world = engine.interactWithInputString(input);
        // 创建 TERenderer 实例并初始化渲染器
        TERenderer ter = new TERenderer();
        ter.initialize(Engine.WIDTH, Engine.HEIGHT);
        // 渲染地图
        ter.renderFrame(world);
    }
}
