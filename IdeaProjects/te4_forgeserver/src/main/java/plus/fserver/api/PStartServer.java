package plus.fserver.api;

import net.minecraft.server.MinecraftServer;
import java.util.function.Consumer;


public final class PStartServer {
    private static Consumer<MinecraftServer>[] callBacks = new Consumer[0];
    private PStartServer(){}


    public static void onServerStart(MinecraftServer server){
        for(Consumer<MinecraftServer> consumer:callBacks) consumer.accept(server);
    }


    public static void addCallBack(Consumer<MinecraftServer> callBack){
        Consumer<MinecraftServer>[] callBacksOld = callBacks;
        callBacks = new Consumer[callBacks.length+1];
        System.arraycopy(callBacksOld, 0, callBacks, 0, callBacksOld.length);
        callBacks[callBacksOld.length] = callBack;
    }
}