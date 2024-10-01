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
    public int selfMinX, selfMinZ, selfMaxX, selfMaxZ, selfMinY, selfMaxY;

    public WorldServerC64(MinecraftServer server, ISaveHandler handler, int dim, WorldServer delegate, Profiler profiler) {
        super(server, handler, dim, delegate, profiler);
    }


    public boolean isSelfPos(int cx, int cy, int cz){
        return cx >= selfMinX && cx < selfMaxX &&
               cz >= selfMinZ && cz < selfMaxZ &&
               cy >= selfMinY && cy < selfMaxY;
    }


    @Override //todo
    public IChunkProvider createChunkProvider() {
        IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
        return chunkProviderServer = new MergedChunkProviderServer(this, ichunkloader, createChunkGenerator());
    }


    protected abstract int foundDimByPos(int cx, int cy, int cz);


    @Override
    protected boolean onUpdateChunkSection(Entity entityIn, int cx, int cy, int cz) {
        if(isSelfPos(cx, cy, cz)){
            return true;
        } else {
            entityIn.changeDimension(foundDimByPos(cx, cy, cz), EmptyTeleporter.INSTANCE);
            return false;
        }
    }
}