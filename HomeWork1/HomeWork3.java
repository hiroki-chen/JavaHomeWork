package JAVA;
import java.util.InputMismatchException;
import java.util.Scanner;

public class HomeWork3 {
    public static void main(String[] args) {
        System.out.println("PLEASE INPUT AN INTEGER OF THE COLOR: ");
        AllTheColorsOfTheRainbow testCase = new AllTheColorsOfTheRainbow();
        Scanner sc = new Scanner(System.in);
        try {
            int newHue = sc.nextInt();
            testCase.changeTheHueOfTheColor(newHue);
            testCase.print();
        } catch (final InputMismatchException e ) {
            System.out.println("WRONG INPUT!!!");
        }
        sc.close();
    }
}

class AllTheColorsOfTheRainbow {
    private int anIntegerRepresentingColors = 1;

    public void changeTheHueOfTheColor (int newHue) {
        this.anIntegerRepresentingColors = newHue;
    }

    public void print() {
        System.out.println("The current color of the rainbow is: " + this.anIntegerRepresentingColors);
    }
}
