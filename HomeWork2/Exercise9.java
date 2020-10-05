import java.util.*;

public class Exercise9 {
    public static void main(String[] args) {
        int num = Integer.parseInt(args[0]);
        Fibonacci f = new Fibonacci(num);
        f.calculate();

        for (int i = 0, j = 1; i < num; i++, j++) {
            System.out.print(j % 10 != 0 ? (f.tmp[i] + " ") : (f.tmp[i] + "\n"));
        }
    }
}

class Fibonacci {
    public long[] tmp;
    private long helper(long[] tmp, int num) {
        if (tmp[num - 1] != 0) { return tmp[num - 1]; }
        return tmp[num - 1] = helper(tmp, num - 1) + helper(tmp, num - 2);
    }

    public Fibonacci(int num) {
        this.tmp = new long[num];
        tmp[0] = 1;
        tmp[1] = 1;
    }

    public long calculate() {
        return this.helper(tmp, tmp.length);
    }
}