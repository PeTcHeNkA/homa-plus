package plus.fserver.server.chunk.io;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import plus.fserver.server.SStrings;

import java.io.IOException;


public class TChunkIOProvider extends TChunkTask {
    private NBTTagCompound nbt;

    TChunkIOProvider(WorldServer world, TQueuedChunk chunk, AnvilChunkLoader loader, ChunkProviderServer provider) {
        super(world, chunk, loader, provider);
    }


    @Override
    public void runTask() throws IOException {
        TQueuedChunk chunkInfo = this.chunkInfo;
        Object[] data = this.loader.loadChunk__Async(world, chunkInfo.x, chunkInfo.z);
        if (data != null) {
            this.nbt = (NBTTagCompound) data[1];
            this.chunk = (Chunk) data[0];
        }
    }


    @Override
    public void syncCallback() {
        try {
            Chunk chunk = this.chunk;
            if (chunk != null) {
                WorldServer world;
                this.loader.loadEntities((world = this.world), this.nbt.getCompoundTag(SStrings.CL_LEVEL), chunk);

                MinecraftForge.EVENT_BUS.postF(new ChunkDataEvent.Load(chunk, this.nbt));

                chunk.setLastSaveTime(world.getTotalWorldTime());
                ChunkProviderServer provider;
                (provider = this.provider).chunkGenerator.recreateStructures(chunk, chunk.x, chunk.z);

                provider.onComplete(this);
            }
            this.runCallbacks();
        } catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }
}