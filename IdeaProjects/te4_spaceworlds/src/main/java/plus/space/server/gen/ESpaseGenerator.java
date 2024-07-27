package plus.space.server.gen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;


public class ESpaseGenerator {


    public static Chunk generateMeteorChunk(World world, IBlockState[] blocks, int x0, int z0) {
        if(x0>50 || z0 > 50 || x0<-50 || z0<-50)return new Chunk(world, x0, z0);
        ChunkPrimer chunkprimer = new ChunkPrimer();

        loopX: for (int x = 0; x < 16; ++x) {
            loopZ:
            for (int z = 0; z < 16; ++z) {
                if (world.rand.nextInt(9) != 5) continue loopZ;
                int xOx = (x0 << 4) + x;
                int z0z = (z0 << 4) + z;
                int delta = xOx * xOx + z0z * z0z;
                if (!(delta < 25000 && delta > 9000)) continue loopZ;

                loopY:
                for (int y = 10; y < 100; ++y) {
                    if (world.rand.nextInt(9) != 5) continue loopY;
                    IBlockState iblockstate = blocks[world.rand.nextInt(blocks.length)];
                    chunkprimer.setBlockState(x, y, z, iblockstate);
                }

            }
        }


        Chunk chunk = new Chunk(world, chunkprimer, x0, z0);
        Biome[] abiome = world.getBiomeProvider().getBiomes((Biome[])null, x0 * 16, z0 * 16, 16, 16);
        byte[] abyte = chunk.getBiomeArray();

        for (int l = 0; l < abyte.length; ++l) {
            abyte[l] = (byte)Biome.getIdForBiome(abiome[l]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }
}
