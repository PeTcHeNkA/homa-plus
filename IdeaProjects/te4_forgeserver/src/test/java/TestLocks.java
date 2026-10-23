import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TestLocks {


    public static void main(String[] args) {
        //for (int i = 0; i<10;i++){
        //    new Thread(TestLocks::run1).start();
        //}
        run1();
    }


    public static void run1(){
        int count = 2500_000;
        AtomicInteger r = new AtomicInteger();
        for (int i = 0; i < 5; i++){
            testTime("SYNC", count, () -> {
                r.addAndGet(test2(count - 1));
            });
            testTime("LOCK", count, () -> {
                r.addAndGet(test1(count + 1));
            });
        }
        if(r.get() == 0)throw new RuntimeException();
    }


    private static void testTime(String name, int count, Runnable r){
        long time = System.currentTimeMillis();
        for (int i = 0; i < count; i++){
            r.run();
        }
        time = System.currentTimeMillis() - time;
        System.out.println(name+": "+time);
    }


    private static final ReentrantLock lock = new ReentrantLock();
    public static int test1(int a){
//        lock.lock();
//        try {
            a = testI2(a);
//        } finally {
//            lock.unlock();
//        }
        return a;
    }


    public static int test2(int a){
        synchronized (lock){
            a = testI2(a);
        }
        return a;
    }


    private static int testI2(int d){
        return d * 17 + 21;
    }
}
