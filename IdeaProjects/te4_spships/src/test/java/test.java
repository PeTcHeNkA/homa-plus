import plus.utls.ArrayBuilder;
import plus.utls.bin.BinNum;
import plus.utls.bin.BinNumImp;

public class test {
    public static void main(String... args){
        try {
            //long start = System.currentTimeMillis();
            test();
            //System.out.printf("END: %s %sms%n", list.getClass(), System.currentTimeMillis()-start);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public static void test() {
        BinNum bn = BinNum.create(2, 2);
        bn.setVal(0, 3);
        bn.setVal(1, 3);


        System.out.println(bn.toInt());
    }
}
















