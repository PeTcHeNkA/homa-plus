package plus.fserver.server;

import io.netty.util.concurrent.FastThreadLocalThread;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;
import org.jetbrains.annotations.Nullable;
import plus.fserver.ElcaCV;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.SyncListCache;
import plus.tson.utl.alloc.ConcurrentCasAllocator;


public class ElcaThread extends FastThreadLocalThread {
    private static final ConcurrentCasAllocator BB_GLOBAL_CACHE = new ConcurrentCasAllocator(4, 2);

    public final Cache cache = new Cache();

    public ElcaThread(@Nullable Runnable runnable){
        super(runnable);
    }



    public static class Cache extends ChunkSelector{
        public final BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos();
        public final BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos();
        public final BlockPos.MutableBlockPos pos3 = new BlockPos.MutableBlockPos();

        public boolean isAirBlock(int x, int y, int z){
            BlockPos.MutableBlockPos pos;
            (pos = pos3).setPosB(x, y, z);
            IBlockState state = getChunkE(pos.getX()>>4, pos.getZ()>>4).getBlockState(pos);
            return state.getBlock().isAir(state, this, pos);
        }


        public boolean setBlockState(int x, int y, int z, IBlockState newState) {
            BlockPos.MutableBlockPos pos;
            (pos = pos3).setPosB(x, y, z);
            return setBlockState(pos, newState, 2);
        }


        public boolean setBlockState(int x, int y, int z, IBlockState newState, int flags) {
            BlockPos.MutableBlockPos pos;
            (pos = pos3).setPosB(x, y, z);
            return setBlockState(pos, newState, flags);
        }
    }


    public void setup(ChunkProviderServer world){
        this.cache.define(world);

        World w = world.world;

        if(ElcaCV.MORE_MOBS_OPTIMIZER) {
            if(w.bbCache == null) {
                SyncListCache.AABB bbCache = (SyncListCache.AABB) BB_GLOBAL_CACHE.tryAllocRaw();
                if(bbCache == null) {
                    bbCache = new SyncListCache.AABB(10, 180);
                }
                w.bbCache = bbCache;
            }
        } else {
            w.bbCache = null;
        }
    }


    public void release(){
        World w = this.cache.getWorld();

        if(ElcaCV.MORE_MOBS_OPTIMIZER){
            w.bbCache.tick();

            if(BB_GLOBAL_CACHE.size() < 4){
                BB_GLOBAL_CACHE.freeObj(w.bbCache);
                w.bbCache = null;
            }
        } else {
            w.bbCache = null;
        }

        this.cache.release();
    }


    public static ElcaThread current(){
        return (ElcaThread) Thread.currentThread();
    }


    public static Cache cache(){
        return ((ElcaThread) Thread.currentThread()).cache;
    }


    public static Cache cache(World world){
        Cache cache = ((ElcaThread) Thread.currentThread()).cache;
        if(cache.getWorld() != world)throw new RuntimeException();
        return cache;
    }
}