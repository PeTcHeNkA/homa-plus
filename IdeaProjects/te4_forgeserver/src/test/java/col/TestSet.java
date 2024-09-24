package col;

import plus.tson.utl.Te4HashSet;

public class TestSet {
    public static void main(String[] args) {
        Te4HashSet<String> set1 = new Te4HashSet<>(7);
        Te4HashSet<String> set2 = new Te4HashSet<>();

        for (int i = 0;i<5;i++){
            set1.add("X"+i);
            set2.add("X"+i);
        }

        System.out.println(set1);
        System.out.println(set2);
    }
}
