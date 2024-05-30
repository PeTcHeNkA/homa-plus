package plus.fserver.server.chunk.io;

import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;


public class TChunkGenProvider extends TChunkTask implements Runnable{
    TChunkGenProvider(TQueuedChunk chunk, AnvilChunkLoader loader, ChunkProviderServer provider) {
        super(chunk, loader, provider);
    }


    @Override
    public void runTask() {
        chunk = worldServer().getChunkProvider().chunkGenerator.generateChunk(chunkInfo.x, chunkInfo.z);
    }


    @Override
    public void syncCallback() {
        try {
            if (chunk != null) {
                worldServer().getChunkProvider().onComplete(this);
            }
            this.runCallbacks();
        } catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }
}
