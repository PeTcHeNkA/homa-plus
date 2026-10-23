package plus.fserver.world.generator.vr;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import plus.fserver.world.generator.struct.IStruct;


@Deprecated
public class MultiChunkCache{
    protected final IntObjectHashMap<ChunkPrimer> cache = new IntObjectHashMap<>();
    protected int prevCX = Integer.MIN_VALUE, prevZX = Integer.MIN_VALUE;
    protected ChunkPrimer prevPrimer;

    public ChunkPrimer primer(int chunkX, int chunkZ){
        if (chunkX == prevCX && chunkZ == prevZX) return prevPrimer;
        int key = chunkX << 16 | (chunkZ & 0xFFFF);
        ChunkPrimer result = cache.get(key);
        synchronized (this) {
            if (result == null) cache.put(key, result = new ChunkPrimer());
            prevCX = chunkX;
            prevZX = chunkZ;
            return prevPrimer = result;
        }
    }


    final class Proxy extends ChunkPrimer {
        private final World world;
        private Chunk chunk;
        private int lx = Integer.MIN_VALUE;
        private int lz = Integer.MIN_VALUE;

        Proxy(World world) {
            this.world = world;
        }

        @Override
        public void setBlockState(int x, int y, int z, IBlockState state) {
            if(prevCX != lx || prevZX != lz){
                chunk = world.getChunk(lx = prevCX, lz = prevZX);
            }
            chunk.setBlockState(new BlockPos(x, y, z), state);
        }
        @Override
        public IBlockState getBlockState(int x, int y, int z) {
            if(prevCX != lx || prevZX != lz){
                chunk = world.getChunk(lx = prevCX, lz = prevZX);
            }
            return chunk.getBlockState(x, y, z);
        }
    }


    public ChunkPrimer primerSafe(World world, int chunkX, int chunkZ){
        if(chunkX == prevCX && chunkZ == prevZX) return prevPrimer;
        int key = chunkX<<16|(chunkZ & 0xFFFF);
        ChunkPrimer result = cache.get(key);

        synchronized (this) {
            if (result == null) {
                if (world.isChunkGeneratedAt(chunkX, chunkZ)) {
                    result = new Proxy(world);
                } else {
                    result = new ChunkPrimer();
                    cache.put(key, result);
                }
            }
            prevCX = chunkX;
            prevZX = chunkZ;
            return prevPrimer = result;
        }
    }


    public void clean(int chunkX, int chunkZ){
        synchronized (this) {
            cache.remove(chunkX << 16 | (chunkZ & 0xFFFF));
            prevZX = prevCX = Integer.MIN_VALUE;
            prevPrimer = null;
        }
    }


    public final void setBlock(int chunkX, int chunkZ, int x, int y, int z, IBlockState state){
        primer(chunkX, chunkZ).setBlockState(x, y, z, state);
    }


    public void place(int chunkX, int chunkZ, int x, int y, int z, IStruct structure){
        structure.generate(this, (chunkX<<4)+x, y, (chunkZ<<4)+z);
    }


    public final IBlockState getBlock(int x, int y, int z){
        return getBlock(x>>4, z>>4, x & 15, y, z & 15);
    }


    public final void setBlockSafe(int x, int y, int z, IBlockState state){
        primer(x>>4, z>>4).setBlockState(x & 15, y, z & 15, state);
    }


    public final void setBlockSafe(int chunkX, int chunkZ, int x, int y, int z, IBlockState state){
        if(x>15){
            chunkX++; x = x-16;
        } else if(x<0){
            chunkX--; x = x+16;
        }
        if(z>15){
            chunkZ++; z = z-16;
        } else if(x<0){
            chunkZ--; z = z+16;
        }
        primer(chunkX, chunkZ).setBlockState(x, y, z, state);
    }


    protected final IBlockState getBlock(int chunkX, int chunkZ, int x, int y, int z){
        return primer(chunkX, chunkZ).getBlockState(x, y, z);
    }
}