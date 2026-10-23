package net.minecraft.world.biome;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import plus.fserver.world.generator.biome.EIntCache;


public class BiomeProviderSingle extends BiomeProvider {
    private final List<Biome> biomeList;
    private final Biome biome;

    public BiomeProviderSingle(Biome biomeIn) {
        this.biome = biomeIn;
        biomeList = Collections.singletonList(biome);
    }


    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return biomeList;
    }


    @Override
    public Biome getBiome(BlockPos pos) {
        return this.biome;
    }


    @Override
    public Biome getBiome(BlockPos pos, Biome defaultBiome) {
        return this.biome;
    }


    @Override
    public Biome getBiome(int x, int y, Biome defaultBiome) {
        return this.biome;
    }


    @NotNull
    @Override
    public EIntCache allocCache() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void freeCache(@NotNull EIntCache session) {}


    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }

        Arrays.fill(biomes, 0, width * height, this.biome);
        return biomes;
    }


    @Override
    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        if (oldBiomeList == null || oldBiomeList.length < width * depth) {
            oldBiomeList = new Biome[width * depth];
        }
        Arrays.fill(oldBiomeList, 0, width * depth, this.biome);
        return oldBiomeList;
    }


    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.getBiomes(listToReuse, x, z, width, length);
    }


    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return biomes.contains(this.biome) ? new BlockPos(x - range + random.nextInt(range * 2 + 1), 0, z - range + random.nextInt(range * 2 + 1)) : null;
    }


    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        return allowed.contains(this.biome);
    }


    @Override
    public boolean isFixedBiome() {
        return true;
    }


    @Override
    public Biome getFixedBiome() {
        return this.biome;
    }
}