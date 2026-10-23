package plus.gen.utl;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import java.util.Random;


public interface GenProxy {
    void gen(Random random, int chunkX, int chunkZ, World world,
             IChunkGenerator chunkGenerator, IChunkProvider chunkProvider);
}
