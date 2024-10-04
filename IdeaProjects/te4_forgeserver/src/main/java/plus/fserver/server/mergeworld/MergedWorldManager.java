package plus.fserver.server.mergeworld;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import plus.fserver.api.WorldConstructor;


public class MergedWorldManager {
    private int maxIds;
    //private final int minId, maxId;

    public MergedWorldManager(int minId, int maxId, DimensionType type){

        //this.minId = minId; this.maxId = maxId;

        int shift = 0;
        for (int id = minId; id < maxId; id++){
            Bound bound = createBound(shift);
            WorldConstructor.register(id,
                    (server, saveHandler, dimId, delegate, profiler) ->
                            newC64(bound, server, saveHandler, dimId, delegate, profiler), type
            );
            ++shift;
        }
    }


    protected Bound createBound(int part){
        Bound bound = new Bound();
        bound.mask = 63;
        return bound;
    }


    protected WorldServerC64 newC64(Bound bound, MinecraftServer server, ISaveHandler saveHandler, int dimId, WorldServer delegate, Profiler profiler){
        return new WorldServerC64(bound, server, saveHandler, dimId, delegate, profiler) {
            @Override
            protected int foundDimByPos(int cx, int cz) {
                return 0;
            }
        };
    }



    public static class Bound{
        public int selfMinX, selfMinZ, selfMaxX, selfMaxZ, mask;

        public boolean isSelfPos(int cx, int cz){
            int mask = this.mask;
            cx = cx & mask; cz = cz & mask;
            return cx >= selfMinX && cx < selfMaxX &&
                    cz >= selfMinZ && cz < selfMaxZ;
        }
    }
}
