import java.util.*;

public class Exercise10 {
    public static void main(String[] args) {
        HashSet<Integer> ans = new HashSet<Integer>();

        for (int i = 10; i < 100; i++) {
            for (int j = i + 1; j < 100; j++) {
                int sum = i * j;
                if (sum < 1000 || sum > 9999 || sum % 100 == 0) { continue; }
                
                int[] nums1 = {sum / 1000, sum / 100 % 10, sum / 10 % 100 % 10, sum % 10};
                int[] nums2 = {i / 10, i % 10, j / 10, j % 10};
                Arrays.sort(nums1);
                Arrays.sort(nums2);
                if (Arrays.equals(nums1, nums2)) { ans.add(sum); }
            }
        }
        ArrayList<Integer> aaa = new ArrayList<Integer>(ans);
        Collections.sort(aaa);
        System.out.println(aaa);
    }
}
