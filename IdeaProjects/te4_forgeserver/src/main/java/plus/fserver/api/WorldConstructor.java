package plus.fserver.api;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.DimensionManager;


public class WorldConstructor {
    private static final IntObjectHashMap<Constructor> constructors = new IntObjectHashMap<>();

    public static void register(int id, Constructor constructor, DimensionType type){
        constructors.put(id, constructor);
        if(!DimensionManager.isDimensionRegistered(id))
            DimensionManager.registerDimension(id, type);
    }


    public static WorldServerMulti newWorld(MinecraftServer server,
                                            ISaveHandler saveHandler, int dimId,
                                            WorldServer delegate, Profiler profiler){

        Constructor constructor = constructors.get(dimId);
        if(constructor!=null)return constructor.create(server, saveHandler, dimId, delegate, profiler);
        return new WorldServerMulti(server, saveHandler, dimId, delegate, profiler);
    }


    @FunctionalInterface
    public interface Constructor{
        WorldServerMulti create(MinecraftServer server,
                                ISaveHandler saveHandler, int dimId,
                                WorldServer delegate, Profiler profiler);
    }
}