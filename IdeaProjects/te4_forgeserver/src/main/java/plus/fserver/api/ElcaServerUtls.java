package plus.fserver.api;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.text.TextComponentString;
import plus.fserver.ElcaCV2;
import java.util.concurrent.ScheduledExecutorService;
import static net.minecraft.network.NetworkSystem.SERVER_EPOLL_EVENTLOOP;
import static net.minecraft.network.NetworkSystem.SERVER_NIO_EVENTLOOP;


public final class ElcaServerUtls {
    private ElcaServerUtls(){}

    public static TextComponentString textCompOf(String src){
        if(ElcaCV2.LIBBUILD)return new TextComponentString(src);
        return new TextComponentAuto(src);
    }


    public static ScheduledExecutorService getPool(MinecraftServer server){
        if(server.getNetworkSystem().usedPool != null)return server.getNetworkSystem().usedPool;
        LazyLoadBase<? extends EventLoopGroup> lazyloadbase;

        if (Epoll.isAvailable() && server.shouldUseNativeTransport()) {
            lazyloadbase = SERVER_EPOLL_EVENTLOOP;
        } else {
            lazyloadbase = SERVER_NIO_EVENTLOOP;
        }
        return lazyloadbase.getValue();
    }
}