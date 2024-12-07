package plus.fserver.world.addon;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;


public class MemoryChunkProviderServer extends ChunkProviderServer {
    public MemoryChunkProviderServer(WorldServer world){
        this(world, EmptyChunkLoader.get(), new EmptyChunkGenerator(world));
    }


    public MemoryChunkProviderServer(WorldServer world, IChunkLoader chunkLoader, IChunkGenerator chunkGeneratorIn) {
        super(world, chunkLoader, chunkGeneratorIn);
    }


    @Override
    public boolean canSave() {
        return false;
    }
}