package plus.proto.tparsers;

import java.util.HashMap;
import java.util.function.Supplier;


public class ReadFuncRegistry {
    private static final HashMap<String, Supplier<IntFuncExecutor>> registry = new HashMap<>();
    //private static final Supplier<IntFuncExecutor> DEFAULT = UPack::new;


    public static IntFuncExecutor create(String name) {
        Supplier<IntFuncExecutor> func = registry.get(name);
        if(func == null)return new UPack(name);
        try {
            return func.get();
        } catch (Exception e){
            e.printStackTrace();
        }
        return new UPack(name);
    }

    public static void reg(String name, Supplier<IntFuncExecutor> supplier) {
        registry.put(name.toLowerCase(), supplier);
    }

    static {
        reg("itemgetlist"  , ItemGetListProc::new);
        reg("avgcontent"   , AvgContentProc::new);
        reg("videosubtitle", VideoSubtitleProc::new);
    }
}