package plus.fserver.server.chunk.io;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.FMLLog;


public class TChunkGenProvider extends TChunkTask implements Runnable{
    TChunkGenProvider(TQueuedChunk chunk, AnvilChunkLoader loader, ChunkProviderServer provider) {
        super(chunk, loader, provider);
    }


    public void addCallback(Runnable callback) {
        this.callbacks.add(callback);
    }


    public void removeCallback(Runnable callback) {
        this.callbacks.remove(callback);
    }
//
//
//    public long chunkHash(){
//        return ChunkPos.asLong(chunkInfo.x, chunkInfo.z);
//    }

    //private static Object LOCK = new Object();

    @Override
    public void run() {
        synchronized(this) {
            WorldServer world = (WorldServer) world();
            try {
                //synchronized (world) {
                    chunk = world.getChunkProvider().chunkGenerator.generateChunk(chunkInfo.x, chunkInfo.z);
                //}
            } catch (Throwable throwable) {
                FMLLog.log.error(
                        "Exception generating new chunk at ["+chunkInfo.x+", "+chunkInfo.z+"]",
                        throwable
                );
            } finally {
                this.finished = true;
                this.notifyAll();
            }
        }
    }


    @Override
    public void syncCallback() {
        try {
            if (chunk != null) {
                ((WorldServer) world()).getChunkProvider().onComplete(this);
            }
            this.runCallbacks();
        } catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }
}
