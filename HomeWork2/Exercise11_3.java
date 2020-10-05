import java.util.*;

public class Exercise11_3 {
    public static void main(String[] args) {
        int num = 0xfffffff;
        
        while (0 != num) {
            System.out.println("Now the number is: " + Integer.toBinaryString(num));
            num >>= 1;
        }
    }
}
