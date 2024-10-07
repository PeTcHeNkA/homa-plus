package plus.fserver.entity;

import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkProviderServer;
import plus.fserver.server.elca.ChunkLSECache;


public class NavChunkLSECache extends ChunkLSECache {
    private final PathNodeType.Set localSet = new PathNodeType.Set();
    public final BlockPos.MutableBlockPos pooledPos = new BlockPos.MutableBlockPos();
    public Object rawData;

    public NavChunkLSECache(ChunkProviderServer provider, int x, int z) {
        super(provider, x, z);
    }


    public PathNodeType.Set localSet(){
        PathNodeType.Set localSet;
        (localSet = this.localSet).clear();
        return localSet;
    }
}
