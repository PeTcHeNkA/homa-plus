import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGeneratorFactory;


public class test {

    public static void main(String... args){
        try { test(args); } catch (Exception e){e.printStackTrace();}
    }
    public static void test(String... args){
        testSpeed(new FastRandom());
        testSpeedLocalRandom();
        RandomGeneratorFactory.all();
    }
    public static final int range = 1000000;
    public static final int count = 1000000000;
    public static void testSpeed(Random r){
        long time = System.currentTimeMillis();
        for(int i=0;i<count;i++){
            r.nextInt(range);
        }
        System.out.println(System.currentTimeMillis()-time);
    }
    public static void testSpeedLocalRandom(){
        ThreadLocalRandom t = ThreadLocalRandom.current();
        long time = System.currentTimeMillis();
        for(int i=0;i<count;i++){
            t.nextInt(range);
        }
        System.out.println(System.currentTimeMillis()-time);
    }
}
class FastRandom extends Random {
    private int seed = 1; private short count = 0;

    @Override public int nextInt(int maxIndex){
        seed = (seed * (12119+count++) + 31);
        return seed % (maxIndex);
    }
}