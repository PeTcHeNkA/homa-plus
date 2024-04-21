package org.example;

import plus.cache.SyncTe4Cache;

public class M2 {
    public static void main(String[] args) {

        SyncTe4Cache<String,String> cache = new SyncTe4Cache<>();

        String[] keys = new String[]{
                "a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","r","s","t",
                "a1","b2","c3","d4","4e","4f","4g","h5","1i","j","k7","l5","1m","3n","1o","1p","r99","5s","3t",
                "sa1","b2s","cs3","d4","4se","s4f","4g","hs5","1si","sj","sk7","ls5","1sm","s3n","1so","s1p","rs99","5ss","s3t"
        };
        String[] keys2 = keys.clone();
        for (int i=0;i<keys2.length;i++){
            keys2[i] = keys2[i] + i + keys2[i];
        }

        testMap3(cache, keys2);
        cache.removeByStamp(0);

//        testErr(cache, keys);
//        testErr(cache, keys2);

//        testMap3(cache, keys);
//        testMap3(cache, keys2);
//
//        cache.remove("a");
//        cache.removeByStamp(0);
//        cache.removeByStamp(1);
//        cache.removeByStamp(2);

        System.out.println(cache.size());
    }

    public static void testErr(SyncTe4Cache<String,String> map, String... keys){
        for (String key:keys){
            map.put(key, key);
            if(!map.containsKey(key))throw new RuntimeException();
        }
        for (String key:keys){
            map.remove(key);
            if(map.containsKey(key))throw new RuntimeException();
        }
    }


    public static void testMap3(SyncTe4Cache<String,String> map, String... keys){
        //for (int stamp=0;stamp<3;stamp++){
            //map.removeByStamp(stamp);
            int count = 0;
            for (String key:keys){
                map.put(key, key, 0);
            }
            for (String key:keys){
                if(map.get(key)!=null && ++count==-1){
                    throw new RuntimeException();
                }
            }
        //}
    }
}
