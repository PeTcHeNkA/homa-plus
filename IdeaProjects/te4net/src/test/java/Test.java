
import io.netty.util.collection.IntObjectHashMap;
import java.util.HashMap;


public class Test {
    //public static RouteMap<String, RouteKey> map = new RouteMap<>();

    public static void main(String[] args) throws Exception {
        HashMap<Integer,String> mp = new HashMap<>();
        IntObjectHashMap<String> fm = new IntObjectHashMap<>();

        int count = 500_000_000;
        for(int i=0;i<50;i++){
            String key = "k_"+i;
            String val = "v_"+i;
            mp.put(key.hashCode(), val);
            fm.put(key.hashCode(), val);
        }


        for(int i=0;i<10;i++) {
            int hs = "k_14".hashCode();
            test(() -> mp.get(hs), count, "===:");
            test(() -> fm.get(hs), count, "F__:");
        }
    }


    public static void test(Runnable r, int count, String name){
        long time = System.currentTimeMillis();
        for(int i=0;i<count;i++) r.run();
        System.out.println(name+(System.currentTimeMillis()-time));
    }
}
