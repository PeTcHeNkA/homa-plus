package col;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import plus.tson.utl.Te4HashMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestLists {
    public static void main(String[] args) {
        int count = 500_000;
        for (int i=0;i<10;i++) {
            test("DEF", new Long2ObjectOpenHashMap<>(), count);
            test("LIN", new Long2ObjectLinkedOpenHashMap<>(), count);
            test("LI2", new LinkedHashMap<>(), count);
            test("DF2", new HashMap<>(), count);
            test("TE4", new Te4HashMap<>(), count);
        }
    }


    private static void test(String nm, Map<Long, String> map, int count){
        testTime(nm, new Runnable() {
            String last;
            @Override
            public void run() {
                for (int i=0;i<count;i++){
                    map.put((long) i, "V"+i);
                }
                int c2 = count*50;
                for (int i=0;i<c2;i++){
                    last = map.get((long) i);
                }
            }
        });
    }


    private static void testTime(String h, Runnable r){
        long start = System.currentTimeMillis();
        r.run();
        System.out.println(h+":"+(System.currentTimeMillis()-start));
    }
}
