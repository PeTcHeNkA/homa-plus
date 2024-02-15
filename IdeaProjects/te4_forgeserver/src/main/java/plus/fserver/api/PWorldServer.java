package plus.fserver.api;

import net.minecraft.world.gen.IChunkGenerator;
import plus.fserver.server.chunk.io.TChunkTask;
import plus.fserver.server.chunk.io.TQueuedChunk;
import java.util.concurrent.ConcurrentHashMap;


public interface PWorldServer{
    default IChunkGenerator createChunkGenerator(){throw new UnsupportedOperationException();}
    default ConcurrentHashMap<TQueuedChunk, TChunkTask> getChunkTasks(){return null;};
    default void addSyncTask(Runnable r){}
    default void addAsyncTask(Runnable r){}
    default boolean canUnload(){throw new UnsupportedOperationException();}
}