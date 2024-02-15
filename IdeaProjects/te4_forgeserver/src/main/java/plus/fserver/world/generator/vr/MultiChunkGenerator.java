package plus.fserver.world.generator.vr;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;


public abstract class MultiChunkGenerator extends MultiChunkCache implements IChunkGenerator {
    public final World world;
    private final Proxy proxy;

    protected MultiChunkGenerator(World world) {
        this.world = world;
        proxy = new Proxy(world);
    }


    public ChunkPrimer primer(int chunkX, int chunkZ){
        if(chunkX == prevCX && chunkZ == prevZX){
            return prevPrimer;
        }
        int key = chunkX<<16|(chunkZ & 0xFFFF);
        ChunkPrimer result = cache.get(key);
        if(result == null){
            if(world.isChunkGeneratedAt(chunkX, chunkZ)){
                result = proxy;
            } else {
                result = new ChunkPrimer();
                cache.put(key, result);
            }
        }
        prevCX = chunkX;
        prevZX = chunkZ;
        return prevPrimer = result;
    }


    private ChunkPrimer getAndClean(int chunkX, int chunkZ){
        ChunkPrimer result = cache.remove(chunkX<<16|(chunkZ & 0xFFFF));
        prevZX = prevCX = Integer.MIN_VALUE;
        prevPrimer = null;
        return result==null?new ChunkPrimer():result;
    }


    @Override
    public final Chunk generateChunk(int chunkX, int chunkZ){
        onGenerate(chunkX, chunkZ);
        Chunk chunk = new Chunk(world, getAndClean(chunkX, chunkZ), chunkX, chunkZ);
        Biome[] abiome = world.getBiomeProvider().getBiomes(
                null, chunkX * 16, chunkZ * 16, 16, 16);
        byte[] abyte = chunk.getBiomeArray();

        for (int l = 0; l < abyte.length; ++l) {
            abyte[l] = (byte)Biome.getIdForBiome(abiome[l]);
        }
        chunk.generateSkylightMap();
        return chunk;
    }


    protected abstract void onGenerate(int chunkX, int chunkZ);
}