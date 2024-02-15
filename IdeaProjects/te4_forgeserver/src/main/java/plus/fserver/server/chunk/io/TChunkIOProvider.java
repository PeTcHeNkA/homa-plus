package plus.fserver.server.chunk.io;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import java.io.IOException;


public class TChunkIOProvider extends TChunkTask {
    private NBTTagCompound nbt;


    TChunkIOProvider(TQueuedChunk chunk, AnvilChunkLoader loader, ChunkProviderServer provider) {
        super(chunk, loader, provider);
    }


    public void addCallback(Runnable callback) {
        this.callbacks.add(callback);
    }


    public void removeCallback(Runnable callback) {
        this.callbacks.remove(callback);
    }


    @Override
    public void run() {
        synchronized(this) {
            try {
                Object[] data = null;
                try {
                    data = this.loader.loadChunk__Async(chunkInfo.world, chunkInfo.x, chunkInfo.z);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (data != null) {
                    this.nbt = (NBTTagCompound) data[1];
                    this.chunk = (Chunk) data[0];
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
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
                this.loader.loadEntities(this.chunkInfo.world, this.nbt.getCompoundTag("Level"), this.chunk);

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