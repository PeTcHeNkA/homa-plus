package plus.fserver.server.mergeworld;

import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.ISaveHandler;


public abstract class WorldServerC64 extends WorldServerMulti {
    public final MergedWorldManager.Bound bound;
    //public int selfMinX, selfMinZ, selfMaxX, selfMaxZ;

    public WorldServerC64(MergedWorldManager.Bound bound, MinecraftServer server, ISaveHandler handler, int dim, WorldServer delegate, Profiler profiler) {
        super(server, handler, dim, delegate, profiler);
        this.bound = bound;
        //this.selfMinX = bound.selfMinX;
    }


    public boolean isSelfPos(int cx, int cz){
        return bound.isSelfPos(cx, cz);
//        int mask = this.mask;
//        cx = cx & mask; cz = cz & mask;
//        return cx >= selfMinX && cx < selfMaxX &&
//                cz >= selfMinZ && cz < selfMaxZ;
    }


    @Override //todo
    public IChunkProvider createChunkProvider() {
        IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
        return chunkProviderServer = new MergedChunkProviderServer(this, ichunkloader, createChunkGenerator());
    }


    protected abstract int foundDimByPos(int cx, int cz);


    @Override
    protected boolean onUpdateChunkSection(Entity entityIn, int cx, int cy, int cz) {
        if(isSelfPos(cx, cz)){
            return true;
        } else {
            entityIn.changeDimension(foundDimByPos(cx, cz), EmptyTeleporter.INSTANCE);
            return false;
        }
    }
}