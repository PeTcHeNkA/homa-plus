package utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;


public class TestMap2 {
    public static void main(String[] args) {

        String[] data = new String[]{
                "a","b","c","d","e","f","g","h","i","j",
                "k","l","m","n","o","p","q","r","s","t",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1", "i1", "j1",
                "k1", "l1", "m1", "n1", "o1", "p1", "q1", "r1", "s1", "t1",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", "i2", "j2",
                "k2", "l2", "m2", "n2", "o2", "p2", "q2", "r2", "s2", "t2"
        };

        int count = 10_000;

        Map<String,String> map  = new java.util.HashMap<>();
        Map<String,String> map2 = new Object2ObjectOpenHashMap<>();

        for (int i=0;i<10;i++){
            testTime("JAVA",   count, () -> testMap(map , data));
            testTime("SPEEDY", count, () -> testMap(map2, data));
        }
    }


    private static void testMap(Map<String,String> map, String[] data){
        String last = null;
        for (String d : data) {
            map.put(d, d);
        }
        for (int i = 0;i<5;i++) {
            for (String d : data) {
                last = map.get(d);
            }
        }
        map.clear();
        if(last == null)throw new RuntimeException();
    }


    private static void testTime(String h, int count, Runnable r){
        long start = System.currentTimeMillis();
        while (count-- > 0) r.run();
        System.out.println(h+":"+(System.currentTimeMillis()-start));
    }
}