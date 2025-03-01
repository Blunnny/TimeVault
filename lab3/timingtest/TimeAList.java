package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        /* Ns:列表大小   times:总耗时   opCounts:操作次数   opCounts:调用平均花费的微秒数
        */
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();

        int[] testSizes = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000};

        for (int n : testSizes) {
            AList<Integer> list = new AList<>(); // 创建一个新的 AList 实例
            Stopwatch sw = new Stopwatch(); // 开始计时
            for (int i = 0; i < n; i++) { // 执行 N 次 addLast 操作
                list.addLast(i); // 添加任意值，这里用 i
            }
            double timeInSeconds = sw.elapsedTime(); // 停止计时并记录时间（秒）
            // 将结果存入对应的 AList
            Ns.addLast(n);           // 列表大小
            times.addLast(timeInSeconds); // 总耗时
            opCounts.addLast(n);     // 操作次数（等于 N）
        }
        printTimingTable(Ns, times, opCounts);
    }
}
