package plus.fserver.server;

import io.netty.util.concurrent.FastThreadLocalThread;
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