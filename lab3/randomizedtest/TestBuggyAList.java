import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import randomizedtest.AListNoResizing;

import static org.junit.Assert.assertEquals;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {

        // 创建 AListNoResizing 与 AListNoResizing 实例
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        randomizedtest.BuggyAList<Integer> buggy = new randomizedtest.BuggyAList<>();

        // 添加相同的 3 个值：4, 5, 6
        correct.addLast(4);
        buggy.addLast(4);
        correct.addLast(5);
        buggy.addLast(5);
        correct.addLast(6);
        buggy.addLast(6);

        // 验证大小是否正确
        assertEquals(correct.size(), buggy.size());

        // 移除 3 次并检查返回值
        assertEquals(correct.removeLast(), buggy.removeLast()); // 预期两者都返回 6
        assertEquals(correct.removeLast(), buggy.removeLast()); // 预期两者都返回 5
        assertEquals(correct.removeLast(), buggy.removeLast()); // 预期两者都返回 4

        // 验证大小是否回到 0
        assertEquals(correct.size(), buggy.size());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        randomizedtest.BuggyAList<Integer> buggy = new randomizedtest.BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4); // 随机生成 0 - 3的随机数
            int randVal = 0;
            if (operationNumber == 0) {
                // 随机生成 0 到 99 的整数并添加到末尾
                randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggy.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int sizeL = L.size();
                int sizeBuggy = buggy.size();
                assertEquals(sizeL, sizeBuggy);
                System.out.println("sizeL: " + sizeL + "sizeBuggy: "+ sizeBuggy);
            } else if (operationNumber == 2 && L.size() > 0) {
                // getLast
                int lastL = L.getLast();
                int lastBuggy = buggy.getLast();
                assertEquals(lastL, lastBuggy);
                System.out.println("lastL: " + lastL + "lastBuggy: " + lastBuggy);
            } else if (operationNumber == 3 && L.size() > 0) {
                // removeLast
                int n = L.getLast();
                int m = buggy.getLast();
                L.removeLast();
                buggy.removeLast();
                System.out.println("removeLastL: " + n + "removeLastBuggy: " + m);
            }
        }
    }
}
