package plus.fserver.server;

import io.netty.util.concurrent.FastThreadLocalThread;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;
import org.jetbrains.annotations.Nullable;
import plus.fserver.server.elca.ChunkSelector;


public class ElcaThread extends FastThreadLocalThread {
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
    }


    public void release(){
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