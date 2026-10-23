package utils;

import plus.asyutl.buf.FastBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;



public class TestFifo {
    private static final ConcurrentLinkedQueue<String> c1 = new ConcurrentLinkedQueue<>();
    private static final FastBuffer<String> c2 = new FastBuffer<>();

    public static void main(String[] args) {
        String[] data = {"val1","val2","val3","val4","val5","val6"};
        int count = 10_000_000;

        for (int i=0;i<10;i++) {
            testTime("CQ", () -> testCQ(count, data));
            testTime("FB", () -> testFB(count, data));
        }
    }


    private static void testTime(String h, Runnable r){
        long start = System.currentTimeMillis();
        r.run();
        System.out.println(h+":"+(System.currentTimeMillis()-start));
    }



    private static void testCQ(int count, String... data){
        for (int i=0;i<count;i++) {
            for (String s : data) {
                c1.add(s);
            }
            while (!c1.isEmpty()) {
                c1.poll();
            }
        }
    }


    private static void testFB(int count, String... data){
        for (int i=0;i<count;i++) {
            for (String s : data) {
                c2.put(s);
            }
            while (c2.hasNext()) {
                c2.flush();
            }
        }
    }
}









