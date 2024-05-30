package plus.fserver.server.chunk.io;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import plus.fserver.server.SStrings;

import java.io.IOException;


public class TChunkIOProvider extends TChunkTask {
    private NBTTagCompound nbt;

    TChunkIOProvider(TQueuedChunk chunk, AnvilChunkLoader loader, ChunkProviderServer provider) {
        super(chunk, loader, provider);
    }


    @Override
    public void runTask() throws IOException {
        Object[] data = this.loader.loadChunk__Async(chunkInfo.world, chunkInfo.x, chunkInfo.z);
        if (data != null) {
            this.nbt = (NBTTagCompound) data[1];
            this.chunk = (Chunk) data[0];
        }
    }


    @Override
    public void syncCallback() {
        try {
            if (chunk != null) {
                this.loader.loadEntities(this.chunkInfo.world, this.nbt.getCompoundTag(SStrings.CL_LEVEL), this.chunk);

                MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(this.chunk, this.nbt));

                this.chunk.setLastSaveTime(provider.world.getTotalWorldTime());
                this.provider.chunkGenerator.recreateStructures(this.chunk, this.chunkInfo.x, this.chunkInfo.z);
                provider.loadedChunks.put(ChunkPos.asLong(this.chunkInfo.x, this.chunkInfo.z), this.chunk);
                this.chunk.onLoad();
                this.chunk.populate(provider, provider.chunkGenerator);
            }

            this.runCallbacks();
        } catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }
}