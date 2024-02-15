package plus.fserver.mixins.light;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.*;
import net.minecraft.world.*;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import org.spongepowered.asm.mixin.*;
import java.util.*;
import plus.fserver.world.light.api.ILightingEngineProvider;


@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer {
    @Shadow @Final public WorldServer world;
    @Shadow @Final private LongSet droppedChunks;
    @Shadow @Final public Long2ObjectMap<Chunk> loadedChunks;
    @Shadow protected abstract void saveChunkExtraData(Chunk chunkIn);
    @Shadow protected abstract void saveChunkData(Chunk chunkIn);
    @Shadow @Final public IChunkLoader chunkLoader;

    /**
     * @author Phosphor + Te4hno
     * @reason faster light
     */
    @Overwrite
    public boolean saveChunks(boolean all) {
        ((ILightingEngineProvider)this.world).getLightingEngine().processLightUpdates();
        ObjectCollection<Chunk> vls = this.loadedChunks.values();
        if(vls == null || vls.isEmpty())return true;
        List<Chunk> list = new ArrayList<>(vls);
        for (int i = 0, j = 0; j < list.size(); ++j) {
            Chunk chunk = list.get(j);
            if (all) this.saveChunkExtraData(chunk);
            if (chunk.needsSaving(all)) {
                this.saveChunkData(chunk);
                chunk.setModified(false);
                ++i;
                if (i == 24 && !all) return false;
            }
        }
        return true;
    }

//
//    /**
//     * @author Phosphor + Te4hno
//     * @reason faster light
//     */
//    @Overwrite
//    public boolean tick() {
//        if (!this.world.disableLevelSaving) {
//            if (!this.droppedChunks.isEmpty()) {
//                //((ILightingEngineProvider)this.world).getLightingEngine().processLightUpdates();
////                for (ChunkPos forced : this.world.getPersistentChunks().keySet())
////                    this.droppedChunks.remove(ChunkPos.asLong(forced.x, forced.z));
//
//                LongIterator iterator = this.droppedChunks.iterator();
//
//                for (int i = 0; i < 100 && iterator.hasNext(); iterator.remove()) {
//                    long olong = iterator.next();
//                    Chunk chunk = this.loadedChunks.get(olong);
//
//                    if (chunk != null && chunk.unloadQueued) {/
//                        chunk.onUnload();
//                        ForgeChunkManager.putDormantChunk(ChunkPos.asLong(chunk.x, chunk.z), chunk);
//                        this.saveChunkData(chunk);
//                        this.saveChunkExtraData(chunk);
//                        this.loadedChunks.remove(olong);
//                        ++i;
//                    }
//                }
//            }
//            if (this.loadedChunks.isEmpty()) DimensionManager.unloadWorld(this.world.provider.getDimension());
//
//            this.chunkLoader.chunkTick();
//        }
//        return false;
//    }
}