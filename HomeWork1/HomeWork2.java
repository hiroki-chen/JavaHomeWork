package JAVA;

public class HomeWork2 {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i ++) {
            Example e = new Example();
            e.foo();
        }
    }
}

class Example {
    public static int cnt = 100;

    public static void foo() {
        cnt += 1;
        System.out.println(cnt);
    }
}
