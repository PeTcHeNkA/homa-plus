package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.layer.GenLayer;


public abstract class BiomeProvider {
    public static List<Biome> allowedBiomes = Lists.newArrayList(
            Biomes.FOREST, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS,
            Biomes.FOREST_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS
    );
    protected ChunkGeneratorSettings settings;


    public abstract List<Biome> getBiomesToSpawnIn();


    public Biome getBiome(BlockPos pos) {
        return this.getBiome(pos, null);
    }


    public Biome getBiome(BlockPos pos, Biome defaultBiome){
        return getBiome(pos.getX(), pos.getZ(), defaultBiome);
    }


    public abstract Biome getBiome(int x, int z, Biome defaultBiome);


    public abstract Object makeGenSession();


    public abstract void freeSession(Object session);


    public float getTemperatureAtHeight(float num1, int num2) {
        return num1;
    }


    public abstract Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height);


    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        return this.getBiomes(oldBiomeList, x, z, width, depth, true);
    }


    public abstract Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag);


    public abstract boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed);


    @Nullable
    public abstract BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random);


    public void cleanupCache(){
        throw new UnsupportedOperationException("CLASS: "+getClass());
    }


    public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
        net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens event = new net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens(worldType, seed, original);
        net.minecraftforge.common.MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.getNewBiomeGens();
    }


    public boolean isFixedBiome() {
        return this.settings != null && this.settings.fixedBiome >= 0;
    }


    public Biome getFixedBiome() {
        return this.settings != null && this.settings.fixedBiome >= 0 ? Biome.getBiomeForId(this.settings.fixedBiome) : null;
    }
}