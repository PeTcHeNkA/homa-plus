package org.example;

import plus.cache.SyncTe4Cache;

public class M2 {
    public static void main(String[] args) {

//        SyncTe4Cache<String,String> cache = new SyncTe4Cache<>();
//
//        String[] keys = new String[]{
//                "a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","r","s","t",
//                "a1","b2","c3","d4","4e","4f","4g","h5","1i","j","k7","l5","1m","3n","1o","1p","r99","5s","3t",
//                "sa1","b2s","cs3","d4","4se","s4f","4g","hs5","1si","sj","sk7","ls5","1sm","s3n","1so","s1p","rs99","5ss","s3t"
//        };
//
//        testMap3(cache, keys);
//
//        cache.removeByStamp(0);
//        cache.removeByStamp(1);
//        cache.removeByStamp(2);
//
//        System.out.println(cache.toString());
    }



    public static void testMap3(SyncTe4Cache<String,String> map, String... keys){
        for (int stamp=0;stamp<3;stamp++){
            map.removeByStamp(stamp);
            int count = 0;
            for (String key:keys){
                map.put(key, key, stamp);
            }
            for (String key:keys){
                if(map.get(key)!=null && ++count==-1){
                    throw new RuntimeException();
                }
            }
        }
    }
}
