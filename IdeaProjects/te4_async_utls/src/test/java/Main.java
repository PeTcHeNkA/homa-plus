import plus.asyutl.thread.PopThreadedPool;
import plus.asyutl.thread.ThreadedPool;

import java.util.Random;

public class Main {

    public static void main(String... args){
        try {
            new Main().test();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Main(){}

    public static void testTime(Runnable r){
        long cur = System.currentTimeMillis();
        r.run();
        System.out.println("Time: "+(System.currentTimeMillis()-cur));
    }

    public void test(){
        System.out.println("Started.");
        testTime(this::task);
    }


    public void task() {
        //Buffer<Runnable> pool = new Buffer<>();
        //ThreadedPool<Runnable> pool = new ThreadedPool<>(1);
        PopThreadedPool<Runnable> pool = new PopThreadedPool<>(4);

        Random r = new Random();
        final long[] in = {0, 0};
        //final StringBuilder[] s = {new StringBuilder()};

        for(int i = 0;i<5000_000;i++){
            int I = i;
            pool.put(() -> {
                int num = I*I*I*I*I%20000;
                synchronized (in) {
                    in[0] +=num;
                    in[1] +=1;
                }
            });
        }

        //pool.asyncFlushAll();
        System.out.printf("Completed [%s], [%s] %n", in[0], in[1]);
    }
}