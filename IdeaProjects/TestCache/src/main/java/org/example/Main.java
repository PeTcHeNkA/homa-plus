package org.example;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.KeyValueStore;
import plus.cache.SyncTe4Cache;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


public class Main {

    public static void main(String[] args) {
        Executor executor = Runnable::run;
        System.out.println("Hello world!");


        Cache<String,String> cache2k = new Cache2kBuilder<String, String>() {}
                .executor(executor)
                .expireAfterWrite(5000, TimeUnit.MILLISECONDS).build();

        com.github.benmanes.caffeine.cache.Cache<String,String> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(5000, TimeUnit.MILLISECONDS)
                .executor(executor)
                .build();

        SyncTe4Cache<String,String> te4C = new SyncTe4Cache<>();


        testMap(cache2k);
        testMap2(caffeine);

        int count = 1200;
        String[] keys = new String[]{
                "a","b","c","d","e","f","aag","h","i","j","k","l","m","n","o","p","r","s","t",
                "a1","b2","c3","d4","4e","4f","4g","h5","1i","j","k7","l5","1m","3n","1o","1p","r99","5s","3t",
                "sa1","b2s","cs3","d4","4se","s4f","4g","hs5","1si","sj","sk7","ls5","1sm","s3n","1so","s1p","rs99","5ss","s3t",
                "saa1","ab2s","cds3","d4d","4sed","sd4f","d4g","dhs5","1si","sj","s9k7","l8s5","19sm","s3n",
                "1so1","s13p","ars99","5dss","sfg3t"
        };

        for (int i=0;i<10;i++){
            test("Cache2k", count, () -> testMap(cache2k, keys));
            test("Caffeine", count, () -> testMap2(caffeine, keys));
            test("Te4", count, () -> testMap3(te4C, keys));
        }
    }


    public static void test(String name, int count, Runnable runnable){
        long start = System.currentTimeMillis();
        for (int i=0;i<count;i++)runnable.run();
        start = System.currentTimeMillis() - start;
        System.out.println(name+" spend time: "+start+"ms");
    }


    public static void testMap3(SyncTe4Cache<String,String> map, String... keys){
        for (int stamp=0;stamp<20;stamp++){
            int nextRem = (stamp + 1) % 20;
            map.removeByStamp(nextRem);
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


    public static void testMap(Cache<String,String> map, String... keys){
        for (int stamp=0;stamp<20;stamp++) {
            int count = 0;
            for (String key : keys) {
                map.put(key, key);
            }
            for (String key : keys) {
                if (map.get(key) != null && ++count == -1) {
                    throw new RuntimeException();
                }
            }
        }
    }


    public static void testMap2(com.github.benmanes.caffeine.cache.Cache<String,String> map0, String... keys){
        for (int stamp=0;stamp<20;stamp++) {
            ConcurrentMap<String, String> map = map0.asMap();
            int count = 0;
            for (String key : keys) {
                map.put(key, key);
            }
            for (String key : keys) {
                if (map.get(key) != null && ++count == -1) {
                    throw new RuntimeException();
                }
            }
        }
    }
}

















