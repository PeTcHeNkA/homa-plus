import plus.fserver.utls.collections.ArrayListE10;
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.collections.IdentityTe4HashSet;
import plus.fserver.utls.math.FastRandom;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class TestI {
    public static void main(String[] args) {
        ArrayListE6 list = new ArrayListE6();

        for (int i=0;i<5;i++)
            list.addF("a");

        System.out.println(list);

//        int count = 50000_000;
//        Random rand1 = new Random(1);
//        FastRandom rand2 = new FastRandom(1);
//        ThreadLocalRandom rand3 = ThreadLocalRandom.current();
//
//        for (int i=0;i<5;i++){
//            test("R", rand1, count);
//            test("F", rand1, count);
//            test("C", rand1, count);
//        }
    }


    static void test(String name, Random rand, int count){
        long start = System.currentTimeMillis();
        float summ = 0f;
        for (int i=0;i<count;i++){
            summ += rand.nextInt();
        }
        start = System.currentTimeMillis() - start;
        System.out.println(name+ " time: "+start+", summ: "+summ);
    }
}