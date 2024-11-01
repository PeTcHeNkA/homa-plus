package plus.fserver.server.chunk.io;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.ForgeChunkManager;
import java.util.List;


public class TChunkUnloadTask extends TChunkTask{
    private final List<NextTickListEntry> pendings;

    public TChunkUnloadTask(WorldServer world, TQueuedChunk key, AnvilChunkLoader loader, ChunkProviderServer provider, Chunk chunk) {
        super(world, key, loader, provider);
        this.chunk = chunk;
        chunk.setSaveStatus(true);

        List<NextTickListEntry> pendings = null;
        try {
            chunk.onUnload();
            pendings = chunk.getWorld().getPendingBlockUpdates(chunk, false);
        } catch (Exception e){
            e.printStackTrace();
        }
        this.pendings = pendings;
    }


    @Override
    public void runTask() {
        Chunk chunk = this.chunk;
        try {
            chunk.getLightingEngine().awaitUnlock();
            if(pendings != null) provider.saveChunkData(pendings, chunk);
            provider.saveChunkExtraData(chunk);
        } finally {
            chunk.setSaveStatus(false);
        }
    }


    @Override
    public boolean cansel() {
        return false;
    }


    @Override
    public void syncCallback() {
        try {
            Chunk chunk = this.chunk;
            ForgeChunkManager.putDormantChunk(ChunkPos.asLong(chunk.x, chunk.z), chunk);
            this.runCallbacks();
        } catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }
}