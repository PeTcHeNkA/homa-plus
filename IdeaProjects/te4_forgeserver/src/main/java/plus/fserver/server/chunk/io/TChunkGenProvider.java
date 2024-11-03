package plus.fserver.server.chunk.io;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;


public class TChunkGenProvider extends TChunkTask implements Runnable{
    TChunkGenProvider(WorldServer world, TQueuedChunk chunk, AnvilChunkLoader loader, ChunkProviderServer provider) {
        super(world, chunk, loader, provider);
    }


    @Override
    public void runTask() {
        TQueuedChunk inf = chunkInfo;
        chunk = world.getChunkProvider().chunkGenerator.generateChunk(inf.x, inf.z);
    }


    @Override
    public void syncCallback() {
        try {
            if (chunk != null) {
                world.getChunkProvider().onComplete(this);
            }
            this.runCallbacks();
        } catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }
}
