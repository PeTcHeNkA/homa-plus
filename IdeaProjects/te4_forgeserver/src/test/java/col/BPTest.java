package col;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.util.math.BlockPos;
import plus.fserver.utls.collections.BlockMap;
import plus.fserver.utls.debug.ScanMem;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BPTest {
    static int mx = 99500000;
    static String res = "";

    private static String[] keys = new String[]{
            "key1", "key2", "key3", "key4", "key5"
    };

    public static void main(String[] args) {
        //Object2ObjectLinkedOpenHashMap<BlockPos,String> map1 = new Object2ObjectLinkedOpenHashMap<>();
        LinkedHashMap<String,String> map1 = new LinkedHashMap<>();
        //plus.fserver.utls.collections.LinkedHashMap<String,String> map2 = new plus.fserver.utls.collections.LinkedHashMap<>();
        for (String key:keys){
            map1.put(key, "V"+key);
            //map2.put(key, "V"+key);
        }

//        map1.trim();

        Map<String,String> map2 = ImmutableMap.copyOf(map1);

        System.out.println(map2.getClass());

        for (int i=0;i<10;i++) {
            testTime("LIN", new Task(map1));
            testTime("IMU", new Task(map2));
        }
//        System.out.println(new ScanMem().scan(map1));
//        System.out.println(new ScanMem().scan(map2));
    }

    static class Task implements Runnable{
        private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final Map<String,String> map;
        String last;
        Task(Map<String, String> map) {
            this.map = map;
        }

        @Override
        public void run() {
            for (int x=-mx;x<mx;x++){
                last = map.get(keys[mx& keys.length]);
            }
        }
    }


    private static void testTime(String h, Runnable r){
        long start = System.currentTimeMillis();
        r.run();
        System.out.println(h+":"+(System.currentTimeMillis()-start));
    }

}
