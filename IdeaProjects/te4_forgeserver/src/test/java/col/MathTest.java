package col;

import plus.fserver.utls.collections.ArrayRemSwapListE6;
import plus.fserver.utls.collections.IdentityTe4HashSet;
import plus.fserver.utls.math.FastRandom;

import java.util.TreeSet;

public class MathTest {
    public static void main(String[] args) {
        Object[] arr = new Object[]{
                "A", "B", "C", "D"
        };
        ArrayRemSwapListE6<String> list = new ArrayRemSwapListE6<>(
                arr
        );
        list.add("E");
        list.remove("A");

        IdentityTe4HashSet<String> set = new IdentityTe4HashSet<>();
        for (Object s: arr)set.add((String) s);

        list.removeAll(set);

        System.out.println(list);

//        Integer[] nums = new Integer[100];
//        for (int i = 0; i < nums.length; i++){
//            nums[i] = i;
//        }
//
//        FastRandom.INST.shuffle(nums, nums.length);
//
//        for (int i = 0; i < 100; i++){
//            System.out.println(nums[i]);
//        }
    }
}
