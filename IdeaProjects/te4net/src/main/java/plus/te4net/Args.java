package plus.te4net;

import plus.util.cons.Logger;
import plus.tson.TsonMap;


public class Args extends TsonMap {
    public static TsonMap ARGS;
    public static void insert(String... args){
        if(ARGS!=null)throw new RuntimeException("Ready defined!");
        ARGS = new Args(args);
    }
    private Args(String... args){
        super(preInit(args));
        ARGS = this;
    }
    private static String preInit(String... args){
        String arg = String.join(" ", args).trim().replace('\'','"');
        Logger.get().info(arg);
        return arg;
    }
}