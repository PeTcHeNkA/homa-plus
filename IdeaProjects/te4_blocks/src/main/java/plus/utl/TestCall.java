package plus.utl;

import java.util.HashMap;

public class TestCall {
    private static boolean enabled = true;
    private static final HashMap<String, IntV> cache = new HashMap<>();

    public static final void call(){
        if(!enabled){
            throw new RuntimeException("Test call is disabled! Remove reduce code!");
        }
        String call = Thread.currentThread().getStackTrace()[2].toString();
        System.out.printf("%s : %s%n", call,
                cache.containsKey(call)?cache.get(call).add():((RRunnable<IntV>) o -> {
                    cache.put(call, o);
                    return o;
                }).run(new IntV()).add()
        );
    }
}
class IntV{
    public int I = 0;
    public int add(){
        I+=1;
        return I;
    }
    public int get(){
        return I;
    }
}