import java.util.*;

public class Exercise14_3 {
    public static void main(String[] args) {
        String[] str = new String[4];
        str[0] = "NKU";
        str[1] = "NKU";
        str[2] = "PKU";
        str[3] = "THU";

        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                boolean[] tmp = foo(str[i], str[j]);
                print(str[i], str[j], tmp);
            }
        }
        
    }

    static boolean[] foo(String lhs, String rhs) {
        boolean[] ans = new boolean[3];
        ans[0] = lhs == rhs;
        ans[1] = lhs != rhs;
        ans[2] = lhs.equals(rhs);
        return ans;
    }

    static void print(String a, String b, boolean[] res) {
        System.out.println(a + " == " + b + "? " + res[0]);
        System.out.println(a + " != " + b + "? " + res[1]);
        System.out.println(a + " equals() " + b + "? " + res[2]);
    }
}
