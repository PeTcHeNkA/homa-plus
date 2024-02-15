package plus.fserver.world.generator.map;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import plus.fserver.world.generator.GenSession;
import java.util.Random;


public class EMapGenBase {
    protected int range = 8;
    protected World world;

    public EMapGenBase() {}


    public void generate(World world, GenSession session, int num1, int num2, ChunkPrimer primer) {
        int range = this.range;
        this.world = world;
        session.rand.setSeed(world.getSeed());
        long l1 = session.rand.nextLong();
        long l2 = session.rand.nextLong();

        for(int num3 = num1 - range; num3 <= num1 + range; ++num3) {
            for(int i = num2 - range; i <= num2 + range; ++i) {
                session.rand.setSeed(((long)num3 * l1) ^ ((long)i * l2) ^ world.getSeed());
                this.recursiveGenerate(world, session, num3, i, num1, num2, primer);
            }
        }
    }


    public static void setupChunkSeed(long seed, Random random, int num1, int num2) {
        random.setSeed(seed);
        long l1 = random.nextLong();
        long l2 = random.nextLong();
        long l3 = (long)num1 * l1;
        long l4 = (long)num2 * l2;
        random.setSeed(l3 ^ l4 ^ seed);
    }


    protected void recursiveGenerate(World world, GenSession session, int num1, int num2, int num3, int num4, ChunkPrimer primer) {
    }
}