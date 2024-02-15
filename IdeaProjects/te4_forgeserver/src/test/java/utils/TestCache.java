package utils;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import plus.tson.utl.Te4HashMap;

import java.util.concurrent.TimeUnit;

public class TestCache {


    public static void main(String[] args) {
        int count = 1;
        int count2 = 90000000;
        String[] data0 = new String[]{"key1", "key2", "key3", "key4", "key5", "key6", "key7", "key8"};

        for (int e=0;e<10;e++) {
            test(() -> {
                Cache<String, String> data = new Cache2kBuilder<String, String>() {
                }.expireAfterWrite(500L, TimeUnit.MILLISECONDS).build();

                for (int i = 0; i < 20; i++) {
                    String k = data0[i % data0.length];
                    data.put(k, k);
                }
                for (int i = 0; i < count2; i++) {
                    String k = data0[i % data0.length];
                    data.get(k);
                }
            }, count);

            test(() -> {
                Te4HashMap<String, String> data = new Te4HashMap<>();
                Te4HashMap<String, String> data2 = new Te4HashMap<>();
                for (int i = 0; i < 20; i++) {
                    String k = data0[i % data0.length];
                    data.fput(k, k);
                    data2.fput(k, k);
                }
                for (int i = 0; i < count2; i++) {
                    String k = data0[i % data0.length];
                    data.get(k);
                }
            }, count);

        }
    }






    public static void test(Runnable r, int count){
        long l = System.currentTimeMillis();
        for (int i=0;i<count;i++){
            r.run();
        }
        System.out.println("TIME:"+(System.currentTimeMillis()-l));
    }
}
