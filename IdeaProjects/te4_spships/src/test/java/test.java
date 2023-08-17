import plus.tson.utl.Tuple;
import plus.utls.ArrayBuilder;
import plus.utls.bin.BinNum;
import plus.utls.bin.BinNumImp;

public class test {
    private final Object a = null;
    private final boolean b = false;

    public static void main(String... args){
        for(Object o:test.class.getDeclaredFields()){
            System.out.println(o);
        }
//        try {
//            //long start = System.currentTimeMillis();
//            test();
//            //System.out.printf("END: %s %sms%n", list.getClass(), System.currentTimeMillis()-start);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
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
















