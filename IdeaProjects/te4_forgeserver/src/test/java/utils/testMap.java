package utils;

import com.google.common.collect.ImmutableMap;
import plus.fserver.utls.collections.HoleList;

public class testMap {
    public static void main(String[] args) {
        HoleList<String> hole = new HoleList<>();

        ImmutableMap<String,String> map1 = ImmutableMap.of("K1","V1", "K2", "V2", "K3", "V3");


        int count = 15000000;


//        map1.put(new BlockPos(-110, 0, -20), "A");
//
//        System.out.println(map1.get(new BlockPos(-110, 0, 20)));

        for (int i=0;i<20;i++){
            testTime("SPEEDY", new Runnable() {
                @Override
                public void run() {
                    for (int i1 = 0; i1 <count; i1++){
                        hole.add(map1.get("K"+i1));
                    }
                }
            });


        }


//        for (int i=0;i<20;i++){
//            testTime("SPEEDY", () -> {
//                for (int i12 = 0; i12 <count; i12++){
//                    map1.put(new BlockPos(10- i12, i12, i12 +10), "K"+ i12);
//                }
//                for (int i12 = 0; i12 <count; i12++){
//                    hole.add(map1.get(new BlockPos(10- i12, i12, i12 +10)));
//                }
//            });
//            testTime("NORMAL", () -> {
//                for (int i1 = 0; i1 <count; i1++){
//                    map2.put(new BlockPos(10- i1, i1, i1 +10), "K"+ i1);
//                }
//                for (int i1 = 0; i1 <count; i1++){
//                    hole.add(map2.get(new BlockPos(10- i1, i1, i1 +10)));
//                }
//            });
//        }
    }

    private static void testTime(String h, Runnable r){
        long start = System.currentTimeMillis();
        r.run();
        System.out.println(h+":"+(System.currentTimeMillis()-start));
    }
}
