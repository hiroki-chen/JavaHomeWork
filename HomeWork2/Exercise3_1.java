import java.util.*;

class Exercise3_1 {
    public static void main(String[] args) {
        Example e = new Example();
        e.field = (float)1.2;
        System.out.println("e.field = " + e.field);
        foo(e);
        System.out.println("e.field = " + e.field);
    }

    static void foo(Example e) {
        e.field = (float)(666);
    }
}
