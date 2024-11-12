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
    boolean async = false;

    TChunkUnloadTask(WorldServer world, TQueuedChunk key, AnvilChunkLoader loader, ChunkProviderServer provider, Chunk chunk) {
        super(world, key, loader, provider);
        chunk.setSaveStatus(true);
        this.chunk = chunk;

        List<NextTickListEntry> pendings = null;
        try {
            pendings = chunk.getWorld().getPendingBlockUpdates(chunk, true);
        } catch (Exception e){
            e.printStackTrace();
        }
        this.pendings = pendings;
    }


    @Override
    public void await(){
        if(finished)return;
        async = true;
        run();
    }


    @Override
    public void runTask() {
        Chunk chunk = this.chunk;
        try {
            chunk.getLightingEngine().awaitUnlock();
            provider.saveChunkData(pendings, chunk, async);
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
            chunk.setSaveStatus(false);
            if(hasCallback()){
                List<NextTickListEntry> pendings = this.pendings;
                if(pendings != null) {
                    WorldServer world = this.world;
                    for (NextTickListEntry entry:pendings)
                        world.scheduleBlockUpdate(entry);
                }
                provider.onComplete(this);
                this.runCallbacks();
            } else {
                onUnload(chunk);
            }
        } catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }


    public static void onUnload(Chunk chunk){
        chunk.setModified(false);
        ForgeChunkManager.putDormantChunk(ChunkPos.asLong(chunk.x, chunk.z), chunk);
    }
}