public class Main {
    public static void main(String[] args) {
        System.out.println("count:" + prime_factors(100));
    }

    static int prime_factors(int N) {
        int factor = 2;
        int count = 0;
        while (factor * factor <= N) {
            while (N % factor == 0) {
                System.out.println("factor:" + factor);
                count += 1;
                N = N / factor;
                System.out.println("count:" + count);
                System.out.println("N:" + N);
            }
            factor += 1;
        }
        return count;
    }
    // Best Case: Θ( ), Worst Case: Θ( )
}