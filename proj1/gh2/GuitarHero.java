package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {
    public static void main(String[] args) {
        // 初始化 StdDraw 画布
        StdDraw.setCanvasSize(600, 200);
        StdDraw.setXscale(0, 1);
        StdDraw.setYscale(0, 1);
        StdDraw.clear();

        // 定义键盘字符串和频率映射
        String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
        GuitarString[] strings = new GuitarString[37];

        // 初始化 37 个 GuitarString 对象
        for (int i = 0; i < 37; i++) {
            double frequency = 440 * Math.pow(2, (i - 24) / 12.0);
            strings[i] = new GuitarString(frequency);
        }

        // 主循环：持续运行
        while (true) {
            // 检查键盘输入
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int index = keyboard.indexOf(key);
                if (index >= 0 && index < 37) {
                    strings[index].pluck(); // 拨动对应的琴弦
                }
            }

            // 合成所有琴弦的样本
            double sample = 0.0;
            for (GuitarString string : strings) {
                sample += string.sample();
            }
            // 播放合成后的音频
            StdAudio.play(sample);
            // 更新所有琴弦的状态
            for (GuitarString string : strings) {
                string.tic();
            }
        }
    }
}
