package plus.fserver.api;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import plus.fserver.ElcaCV2;
import plus.fserver.utls.collections.EntityChunkIter;

import java.util.concurrent.ScheduledExecutorService;
import static net.minecraft.network.NetworkSystem.SERVER_EPOLL_EVENTLOOP;
import static net.minecraft.network.NetworkSystem.SERVER_NIO_EVENTLOOP;


public final class ElcaServerUtls {
    private ElcaServerUtls(){}

    public static TextComponentString textCompOf(String src){
        if(ElcaCV2.LIBBUILD)return new TextComponentString(src);
        return new TextComponentAuto(src);
    }


    public static ISaveHandler getSaveHandler(MinecraftServer server){
        return server.getSaveHandler();
    }


    public static Iterable<Entity> getEntities(Chunk chunk){
        return new EntityChunkIter(chunk);
    }


    public static IBlockAccess access(World world){
        if(ElcaCV2.LIBBUILD)return world;
        else return world.access();
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