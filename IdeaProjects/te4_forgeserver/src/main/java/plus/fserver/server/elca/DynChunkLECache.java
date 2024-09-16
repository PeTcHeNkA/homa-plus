package plus.fserver.server.elca;

import net.minecraft.world.World;
import plus.fserver.utls.math.MathUtls;


public class DynChunkLECache {
    private ChunkLCache cache;

    public DynChunkLECache(World world, int x, int z){
        cache = new ChunkLCache(world, x, z);
    }


    public ChunkLCache getExecutor(int x, int z){
        if(cache.x == x && cache.z == z)return cache;
        int dx = MathUtls.range(cache.x, x);
        if(dx > 10 || dx <-10) return new ChunkLCache(cache.world, cache.x+x, cache.z+z);
        int dz = MathUtls.range(cache.z, z);
        if(dz > 10 || dz <-10) return new ChunkLCache(cache.world, cache.x+x, cache.z+z);

        if(Math.abs(dx)+Math.abs(dz)>10){
            return cache = cache.move(dx, dz);
        }
        return cache.getExecutor(x, z);
    }
}









