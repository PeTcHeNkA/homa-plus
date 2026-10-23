import plus.tson.utl.Tuple;
import plus.utls.ArrayBuilder;
import plus.utls.bin.BinNum;
import plus.utls.bin.BinNumImp;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class test {
    private final Object a = null;
    private final boolean b = false;

    public static void main(String... args){
        System.out.println(Arrays.toString(createRandomSPM(200)));
    }

    public static int[] createRandomSPM(int power){
        ++power;
        int[] params = new int[10];
        Random random = ThreadLocalRandom.current();
        while (power>1){
            int cast = random.nextInt(power);
            power -= cast;
            params[random.nextInt(10)] += cast;
        }
        return params;
    }



    public static void test() {
        System.out.println(ofLong(toLong(10.5f, 15.5f)));
    }


    static Tuple<Float,Float> ofLong(long l){
        return new Tuple<>(Float.intBitsToFloat((int) (l>>32)), Float.intBitsToFloat((int)l));
    }

    static long toLong(Tuple<Float,Float> f){
        return toLong(f.A, f.B);
    }

    static long toLong(float f1, float f2){
        return ((long) Float.floatToIntBits(f1))<< 32 | Float.floatToIntBits(f2);
    }
}
















