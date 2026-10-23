import plus.def.IntHashProxy;
import plus.tson.utl.Te4HashMap;

import java.util.HashMap;
import java.util.Map;

public class TestHash2 {



    public static void main(String[] args) {
        int count = 1000_000;
        String[] testKeys = new String[]{
                "a","b","c","d","e","f","g","h","i","j","i","j","k","l","m","n","o","p","q","r","s",
                "p","r","s","t","u","v","w","x","y","z","a1","a2","a3","a4","a5","a6","a7","a8","a9",
                "b1", "b2", "b3", "b4", "b5","1c","2c","3c","4c","5c","6c","7c","8c","9c","10c","11c","12c",
                "14c","15c","16c","s1","s2","s3","s4","s5"
        };

        IntHashProxy<String> proxy = new IntHashProxy<>();
        Te4HashMap<String,String> map = new Te4HashMap<>();

        for (int i = 0; i < 10; i++) {
            testTime("T4", count, () -> test1(map, testKeys));
            testTime("MC", count, () -> test2(proxy, testKeys));
        }

    }


    static void test2(IntHashProxy<String> map, String... testItems) {
        int count = -1;
        for (String testItem : testItems) {
            map.put(testItem, testItem);
        }
        for (String testItem : testItems) {
            if (map.containsKey(testItem)) {
                ++count;
            }
        }
        if (count == testItems.length) {
            throw new RuntimeException();
        }
        map.clearMap();
    }


    static void test1(Map<String,String> map, String... testItems) {
        int count = -1;
        for (String testItem : testItems) {
            map.put(testItem, testItem);
        }
        for (String testItem : testItems) {
            if (map.containsKey(testItem)) {
                ++count;
            }
        }
        if (count == testItems.length) {
            throw new RuntimeException();
        }
        map.clear();
    }


    static void testTime(String label, int count, Runnable r) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            r.run();
        }
        long end = System.currentTimeMillis();
        System.out.println(label+" time: " + (end - start));
    }
}
