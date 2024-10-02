package plus.fserver.server.chunk.io;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.FMLLog;
import java.util.concurrent.ConcurrentLinkedQueue;


public abstract class TChunkTask implements Runnable{
    protected final TQueuedChunk chunkInfo;
    protected final AnvilChunkLoader loader;
    protected final ChunkProviderServer provider;
    protected Chunk chunk;
    protected final ConcurrentLinkedQueue<Runnable> callbacks = new ConcurrentLinkedQueue<>();
    protected boolean finished = false;
    protected WorldServer world;


    TChunkTask(WorldServer world, TQueuedChunk chunk, AnvilChunkLoader loader, ChunkProviderServer provider) {
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


    public final void await(){
        if(finished)return;
        run();
    }


    protected abstract void runTask() throws Exception;


    public abstract void syncCallback();


    public void addCallback(Runnable callback) {
        this.callbacks.add(callback);
    }


    public void removeCallback(Runnable callback) {
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
        return this.callbacks.isEmpty();
    }


    public void runCallbacks() {
        ConcurrentLinkedQueue<Runnable> callbacks = this.callbacks;
        Runnable task = callbacks.poll();
        while (task != null){
            try {
                task.run();
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
