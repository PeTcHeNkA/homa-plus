package plus.fserver.server.chunk.io;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.FMLLog;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;


public abstract class TChunkTask implements Runnable{
    protected final TQueuedChunk chunkInfo;
    protected final AnvilChunkLoader loader;
    protected final ChunkProviderServer provider;
    protected Chunk chunk;
    protected final ConcurrentLinkedQueue<Consumer<Chunk>> callbacks = new ConcurrentLinkedQueue<>();
    protected boolean finished = false;


    TChunkTask(TQueuedChunk chunk, AnvilChunkLoader loader, ChunkProviderServer provider) {
        this.chunkInfo = chunk;
        this.loader = loader;
        this.provider = provider;
    }


    @Override
    public final void run() {
        synchronized(this) {
            if(finished)return;
            try {
                runTask();
            } catch (Throwable throwable) {
                FMLLog.log.error(
                        "Exception run chunk task at ["+chunkInfo.x+", "+chunkInfo.z+"]",
                        throwable
                );
            } finally {
                this.finished = true;
                this.notifyAll();
            }
        }
    }


    public final void await(){
        if(finished)return;
        run();
    }


    protected abstract void runTask() throws Exception;


    public abstract void syncCallback();


    public void addCallback(Consumer<Chunk> callback) {
        this.callbacks.add(callback);
    }


    public void removeCallback(Consumer<Chunk> callback) {
        this.callbacks.remove(callback);
    }


    public boolean isCompleted() {
        return this.finished;
    }


    public void cansel(){
        if(finished)return;
        synchronized(this) {
            finished = true;
        }
    }


    public boolean hasCallback() {
        return this.callbacks.size() > 0;
    }


    public void runCallbacks() {
        Chunk chunk = this.chunk;
        for (Consumer<Chunk> r : this.callbacks) {
            r.accept(chunk);
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


    public WorldServer worldServer(){
        return (WorldServer) chunkInfo.world;
    }
}
