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
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.collections.EntityChunkIter;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import static net.minecraft.network.NetworkSystem.SERVER_EPOLL_EVENTLOOP;
import static net.minecraft.network.NetworkSystem.SERVER_NIO_EVENTLOOP;


public final class ElcaServerUtls {
    private static final HashMap<String,WoldLocal> localRegistry    = new HashMap<>();
    private static final ArrayListE6<WoldLocal> localRegistryValues = new ArrayListE6<>();

    private ElcaServerUtls(){}

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


    public static World world(IBlockAccess access){
        if(ElcaCV2.LIBBUILD){
            if(access instanceof World)return (World)access;
            throw new UnsupportedOperationException("Use only on Elca Server!");
        }
        return access.getWorld();
    }


    public static WoldLocal registerWorldLocal(String name, Function<World,Object> func){
        if(ElcaCV2.LIBBUILD)throw new UnsupportedOperationException("Use only on Elca Server!");
        synchronized (localRegistry) {
            if (localRegistry.containsKey(name)) {
                return localRegistry.get(name);
            }
            int id = localRegistryValues.size();
            WoldLocal woldLocal = new WoldLocal(id, func);

            localRegistry.put(name, woldLocal);
            localRegistryValues.add(woldLocal);

            return woldLocal;
        }
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


    public static Object[] createLocals(World world){
        ArrayListE6<WoldLocal> values = localRegistryValues;
        Object[] locals = new Object[values.sizeC()];
        for (int i = 0; i < values.sizeC(); i++) {
            locals[i] = values.get(i).factory.apply(world);
        }
        return locals;
    }


    public static final class WoldLocal{
        private final int id;
        private final Function<World,Object> factory;

        public WoldLocal(int id, Function<World, Object> factory) {
            this.id = id;
            this.factory = factory;
        }


        public int getId() {
            return id;
        }


        public Object get(World world){
            if(ElcaCV2.LIBBUILD)throw new UnsupportedOperationException("Use only on Elca Server!");
            return world.getLocal(id);
        }
    }
}