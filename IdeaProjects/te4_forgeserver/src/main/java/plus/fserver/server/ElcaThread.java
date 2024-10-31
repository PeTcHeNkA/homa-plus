package plus.fserver.server;

import io.netty.util.concurrent.FastThreadLocalThread;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
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


    public void setWorld(WorldServer world){
        this.cache.define(world.chunkProviderServer);
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
}