package plus.fserver.world.addon.mergeworld;

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
    public boolean tick() {
        return false;
    }


    public boolean realTick(){
        return super.tick();
    }
}