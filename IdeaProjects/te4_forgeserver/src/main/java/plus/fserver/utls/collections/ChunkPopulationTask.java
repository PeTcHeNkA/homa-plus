package plus.fserver.utls.collections;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ChunkPopulationTask extends ConcurrentLinkedQueue<Chunk> implements Runnable {
    private final ChunkProviderServer provider;
    private boolean isRunning = false;

    public ChunkPopulationTask(ChunkProviderServer provider) {
        this.provider = provider;
    }


    public void reRunIfNeed() {
        if (isRunning) return;
        isRunning = true;
        provider.world.getMinecraftServer().ticker().service.execute(this);
    }


    @Override
    public void run() {
        isRunning = true;
        Chunk chunk = poll();
        while (chunk != null) try {
            chunk.populate(provider, provider.chunkGenerator);
            chunk = poll();
        } catch (Exception e){
            provider.world.getMinecraftServer().ticker().onError(provider.world, e);
        }
        isRunning = false;
    }
}