package byow.Core;

/** This is the main entry point for the program. This class simply parses
 *  the command line inputs, and lets the byow.Core.Engine class take over
 *  in either keyboard or input string mode.
 */
public class Main {
    // 解析命令行参数 args，决定以何种模式运行
    public static void main(String[] args) {
        // 参数多于 2，报错并退出
        if (args.length > 2) {
            System.out.println("Can only have two arguments - the flag and input string");
            System.exit(0);
        }
        // 参数为 2 且第一个参数为"-s"，启用调试模型，输出生成的地图
        else if (args.length == 2 && args[0].equals("-s")) {
            Engine engine = new Engine();
            engine.interactWithInputString(args[1]); // 根据生成世界，但不渲染
            System.out.println(engine.toString());
        }
        // 参数为 2 且第一个参数为"-p"，启用回放模型（待实现）
        else if (args.length == 2 && args[0].equals("-p")) { System.out.println("Coming soon."); }
        // 其他情况，启用正常游戏模型
        else {
            Engine engine = new Engine();
            engine.interactWithKeyboard();
        }
    }
}
