package plus.fserver.server.chunk.io;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;


public abstract class TChunkTask implements Runnable{
    protected final ConcurrentLinkedQueue<Consumer<Chunk>> callbacks = new ConcurrentLinkedQueue<>();
    protected final TQueuedChunk chunkInfo;
    protected final IChunkLoader loader;
    protected final ChunkProviderServer provider;
    protected WorldServer world;
    protected Chunk chunk;
    protected boolean finished = false;


    TChunkTask(WorldServer world, TQueuedChunk chunk, IChunkLoader loader, ChunkProviderServer provider) {
        this.world = world;
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
                onError(new RuntimeException("Exception run chunk task at ["+chunkInfo.x+", "+chunkInfo.z+"]", throwable));
            } finally {
                this.finished = true;
                this.notifyAll();
            }
        }
    }


    public void await(){
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


    public boolean cansel(){
        if(finished)return true;
        synchronized(this) {
            finished = true;
        }
        return true;
    }


    public boolean hasCallback() {
        return !this.callbacks.isEmpty();
    }


    public void runCallbacks() {
        Chunk chunk = this.chunk;
        ConcurrentLinkedQueue<Consumer<Chunk>> callbacks = this.callbacks;
        Consumer<Chunk> task = callbacks.poll();
        while (task != null){
            try {
                task.accept(chunk);
            } catch (Throwable e){
                onError(e);
            }
            task = callbacks.poll();
        }
    }


    protected final void onError(Throwable e){
        world.getMinecraftServer().ticker().onError(world, e);
    }


    public TQueuedChunk getChunkInfo() {
        return chunkInfo;
    }


    public Chunk getChunk() {
        return this.chunk;
    }


    public WorldServer world(){
        return world;
    }
}
