import plus.sol.api.IVarProxy;
import plus.tson.TsonFile;

import java.util.Locale;

public class Test2 {
    public static void main(String... args){

        try {
            test();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void test(){
        String[] a = {"a", "b"};
        String[] b = {"c", "d"};

        int shift = a.length-1;
        String[] items = new String[shift+b.length];
        System.arraycopy(a, 0, items, 0, shift);
        System.arraycopy(b, 0, items, shift, b.length);

        items[shift] = items[shift]+1;

        for(String s:items){
            System.out.println(">>"+s);
        }
    }




}
