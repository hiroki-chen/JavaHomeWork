import java.util.*;

class Exercise2_1 {
    public static void main(String[] args) {
        System.out.println("This is an example for name override.");
        Example e1 = new Example();
        Example e2 = new Example();

        e1.field = (float)2.1;
        e2.field = (float)3.1;
        System.out.println("1. e1.field = " + e1.field + ", e2.field = " + e2.field);
        e1 = e2;
        System.out.println("Doing e1 = e2!");
        e1.field = (float)6.6;
        System.out.println("let e1.field = 6.6!");
        System.out.println("2. e1.field = " + e1.field + ", e2.field = " + e2.field);
    }
}

class Example {
    public float field;
}