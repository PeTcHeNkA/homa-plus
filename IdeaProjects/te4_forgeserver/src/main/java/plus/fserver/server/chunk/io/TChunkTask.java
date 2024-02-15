package plus.fserver.server.chunk.io;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class TChunkTask implements Runnable{
    protected final TQueuedChunk chunkInfo;
    protected final AnvilChunkLoader loader;
    protected final ChunkProviderServer provider;
    protected Chunk chunk;
    protected final ConcurrentLinkedQueue<Runnable> callbacks = new ConcurrentLinkedQueue<>();
    protected boolean finished = false;


    TChunkTask(TQueuedChunk chunk, AnvilChunkLoader loader, ChunkProviderServer provider) {
        this.chunkInfo = chunk;
        this.loader = loader;
        this.provider = provider;
    }


    public abstract void syncCallback();


    public void addCallback(Runnable callback) {
        this.callbacks.add(callback);
    }


    public void removeCallback(Runnable callback) {
        this.callbacks.remove(callback);
    }


    public boolean runFinished() {
        return this.finished;
    }


    public boolean hasCallback() {
        return this.callbacks.size() > 0;
    }


    public void runCallbacks() {
        for (Runnable r : this.callbacks) {
            r.run();
        }
        this.callbacks.clear();
    }


    public TQueuedChunk getChunkInfo() {
        return chunkInfo;
    }


    public Chunk getChunk() {
        return this.chunk;
    }


    public World world(){
        return chunkInfo.world;
    }
}
