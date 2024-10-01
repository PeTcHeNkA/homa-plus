package plus.fserver.server.mergeworld;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;


public class MergedChunkProviderServer extends ChunkProviderServer {
    public MergedChunkProviderServer(WorldServer world, IChunkLoader chunkLoader, IChunkGenerator chunkGeneratorIn) {
        super(world, chunkLoader, chunkGeneratorIn);
    }


    @Override
    public synchronized void putChunk(long k, Chunk chunk) {
        super.put(k, chunk);
    }


    @Override
    public synchronized void removeChunk(long k) {
        super.remove(k);
    }
}