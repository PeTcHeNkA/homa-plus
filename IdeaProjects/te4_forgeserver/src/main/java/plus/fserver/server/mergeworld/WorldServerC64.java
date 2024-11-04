package plus.fserver.server.mergeworld;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.ISaveHandler;
import plus.fserver.ElcaCV;


public abstract class WorldServerC64 extends WorldServerMulti {
    private int dim;

    public WorldServerC64(MinecraftServer server, ISaveHandler handler, int dim, WorldServer delegate, Profiler profiler) {
        super(server, handler, dim, delegate, profiler);
        this.dim = dim;
    }


    @Override //todo
    public IChunkProvider createChunkProvider() {
        IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
        return chunkProviderServer = new MergedChunkProviderServer(this, ichunkloader, createChunkGenerator());
    }


    protected abstract int foundDimByPos(int cx, int cz);


    @Override
    protected boolean onUpdateChunkSection(Entity entity, int cx, int cy, int cz) {
        int dim = foundDimByPos(cx >> 4, cz >> 4);
        if(dim == this.dim){
            return true;
        } else {
            double range = entity.elcaMinRange;
            if(range == range){
                entity.elcaMinRange = Double.NaN;
                entity.changeDimension(dim, EmptyTeleporter.INSTANCE);
                entity.elcaMinRange = range;

//                if (entity.addedToChunk) {
//                    Chunk chunk = chunkProvider.getLoadedChunk(entity.chunkCoordX, entity.chunkCoordZ);
//                    if (chunk != null) {
//                        if(ElcaCV.ENABLE_CHUNK_ADD_LOCKS) {
//                            synchronized (chunk) {
//                                chunk.removeEntityAtIndex(entity, entity.chunkCoordY); //sync
//                            }
//                        } else {
//                            chunk.removeEntityAtIndex(entity, entity.chunkCoordY); //def
//                        }
//                    }
//                }
            }
            return false;
        }
    }
}