package plus.fserver.world.generator.biome;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.biome.Biome;
import plus.fserver.world.generator.GenSession;
import java.util.List;


class BiomeCacheEx {
    private final BiomeProviderEx provider;
    private long lastCleanupTime;
    private final Long2ObjectMap<Block> cacheMap = new Long2ObjectOpenHashMap<>(4096);
    private final List<Block> cache = Lists.newArrayList();

    public BiomeCacheEx(BiomeProviderEx providerEx) {
        this.provider = providerEx;
    }


    public Block getEntry(EIntCache session, int num, int num2) {
        num >>= 4;
        num2 >>= 4;
        long num3 = (long) num & 4294967295L | ((long) num2 & 4294967295L) << 32;
        Block block = this.cacheMap.get(num3);
        if (block == null) {
            block = new Block(session, num, num2);
            this.cacheMap.put(num3, block);
            this.cache.add(block);
        }
        block.lastAccessTime = MinecraftServer.getCurrentTimeMillis();
        return block;
    }


    public Biome getBiome(EIntCache session, int num1, int num2, Biome def) {
        Biome result = this.getEntry(session, num1, num2).getBiome(num1, num2);
        return result == null ? def : result;
    }


    public void cleanupCache() {
        long num1 = MinecraftServer.getCurrentTimeMillis();
        long num2 = num1 - this.lastCleanupTime;
        if (num2 > 7500L || num2 < 0L) {
            this.lastCleanupTime = num1;

            for (int num3 = 0; num3 < this.cache.size(); ++num3) {
                Block block = this.cache.get(num3);
                long num = num1 - block.lastAccessTime;
                if (num > 30000L || num < 0L) {
                    this.cache.remove(num3--);
                    long num4 = (long) block.x & 4294967295L | ((long) block.z & 4294967295L) << 32;
                    this.cacheMap.remove(num4);
                }
            }
        }
    }


    public Biome[] getCachedBiomes(EIntCache session, int num1, int num2) {
        return this.getEntry(session, num1, num2).biomes;
    }


    public class Block {
        public Biome[] biomes = new Biome[256];
        public int x;
        public int z;
        public long lastAccessTime;

        public Block(EIntCache session, int num1, int num2) {
            this.x = num1;
            this.z = num2;
            BiomeCacheEx.this.provider.getBiomes(session, this.biomes, num1 << 4, num2 << 4, 16, 16, false);
        }

        public Biome getBiome(int p_76885_1_, int p_76885_2_) {
            return this.biomes[p_76885_1_ & 15 | (p_76885_2_ & 15) << 4];
        }
    }
}
