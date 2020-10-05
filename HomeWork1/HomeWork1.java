package JAVA;
import java.util.*;

public class HomeWork1 {
    public static void main(String[] args) {
        System.out.println("Please input three arguments.");
        Scanner sc = new Scanner(System.in);
        sc.useLocale(Locale.US);

        try {
            int a = sc.nextInt();
            double b = sc.nextDouble();
            boolean c = sc.nextBoolean();
            DataOnly dataOnly = new DataOnly(a, b, c);
            dataOnly.print();
        } catch (final InputMismatchException e) {
            System.out.println("WRONG INPUT! PLEASE TRY AGAIN.");
        }
        sc.close();
    }
}

class DataOnly {
    private int i;
    private double d;
    private boolean b;

    public DataOnly(int a, double b, boolean c) {
        this.i = a;
        this.d = b;
        this.b = c;
    }

    public void print() {
        System.out.println("DataOnly.i = " + this.i);
        System.out.println("DataOnly.d = " + this.d);
        System.out.println("DataOnly.b = " + this.b);
    }
}