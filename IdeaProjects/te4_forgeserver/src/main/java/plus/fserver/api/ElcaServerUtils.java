package plus.fserver.api;

import com.google.common.collect.ImmutableMap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.util.AttributeKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import plus.fserver.ElcaCV2;
import plus.fserver.api.inject.FieldAccessor;
import plus.fserver.api.offline.TaskType;
import plus.fserver.plugin.FieldAccessGen;
import plus.fserver.server.ElcaThread;
import plus.fserver.utls.collections.EntityChunkIter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static net.minecraft.network.NetworkSystem.SERVER_EPOLL_EVENTLOOP;
import static net.minecraft.network.NetworkSystem.SERVER_NIO_EVENTLOOP;


public final class ElcaServerUtils {
    private ElcaServerUtils(){}

    public static boolean hasElcaServer(){
        return !ElcaCV2.LIBBUILD;
    }


    public static TextComponentString textCompOf(String src){
        if(ElcaCV2.LIBBUILD)return new TextComponentString(src);
        return new TextComponentAuto(src);
    }


    public static ISaveHandler getSaveHandler(MinecraftServer server){
        return server.getSaveHandler();
    }


    public static Iterable<Entity> getEntities(Chunk chunk){
        if(ElcaCV2.LIBBUILD)throw new UnsupportedOperationException("Use only on Elca Server!");
        return new EntityChunkIter(chunk);
    }


    public static IBlockAccess access(World world){
        if(ElcaCV2.LIBBUILD)return world;
        else return world.access();
    }


    public static IBlockAccess access(){
        if(ElcaCV2.LIBBUILD)throw new UnsupportedOperationException("Use only on Elca Server!");
        return ElcaThread.cache();
    }


    public static World world(IBlockAccess access){
        if(ElcaCV2.LIBBUILD){
            if(access instanceof World)return (World)access;
            throw new UnsupportedOperationException("Use only on Elca Server!");
        }
        return access.getWorld();
    }


    public static Map<String,String> modList(EntityPlayerMP player){
        try {
            final NetworkDispatcher dispatcher = (NetworkDispatcher) player.connection.netManager.channel()
                    .attr((AttributeKey) NetworkDispatcher.FML_DISPATCHER).get();
            if (dispatcher == null) return ImmutableMap.of();
            return dispatcher.getModList();
        } catch (Exception e){
            e.printStackTrace();
            return ImmutableMap.of();
        }
    }


    public FireWall getFireWall(MinecraftServer server){
        if(ElcaCV2.LIBBUILD)return null;
        return server.getNetworkSystem().fireWall();
    }


    public void setFireWallLog(MinecraftServer server, boolean b){
        if(ElcaCV2.LIBBUILD)return;
        server.getNetworkSystem().fireWall().setLog(b);
    }


    public static ScheduledExecutorService getPool(MinecraftServer server){
        if(!ElcaCV2.LIBBUILD) {
            if (server.getNetworkSystem().usedPool != null) return server.getNetworkSystem().usedPool;
        }
        LazyLoadBase<? extends EventLoopGroup> lazyloadbase;

        if (Epoll.isAvailable() && server.shouldUseNativeTransport()) {
            lazyloadbase = SERVER_EPOLL_EVENTLOOP;
        } else {
            lazyloadbase = SERVER_NIO_EVENTLOOP;
        }
        return lazyloadbase.getValue();
    }


    public static <I, T> FieldAccessor<I, T> compileAccess(Class<I> clazz, Class<T> type, String name){
        if(ElcaCV2.LIBBUILD)throw new UnsupportedOperationException("Use only on Elca Server!");
        Object obj = FieldAccessGen.compile(FieldAccessor.class.getClassLoader(), clazz.getName(), type.getName(), name);
        return (FieldAccessor<I, T>) obj;
    }


    public static <V extends IForgeRegistryEntry<V>> void unsafeRegister(ForgeRegistry<V> registry, int id, V obj) {
        registry.addU(id, obj);
    }


    public static void applyForPlayerSafe(MinecraftServer server, UUID uuid, Consumer<EntityPlayerMP> task, TaskType type, boolean advancements){
        if(ElcaCV2.LIBBUILD)throw new UnsupportedOperationException("Use only on Elca Server!");
        server.getPlayerList().applyForPlayerSafe(uuid, task, type, advancements);
    }
}