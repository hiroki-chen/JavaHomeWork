import java.util.*;

public class Exercise8_2 {
    public static void main(String[] args) {
        long oct = 01234567654321l;
        long hex = 0x12345678l;
        System.out.println("Original data: " + Long.toOctalString(oct) + " " + Long.toHexString(hex));
        System.out.println("Binary data: " + Long.toBinaryString(oct) + " " + Long.toBinaryString(hex));
    }
}
