package plus.space.server.gen.v2;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import java.util.List;


public class CChunkGenerator implements IChunkGenerator {
    private final boolean asyncGen, asyncPop;
    private final Generator generator;
    private final Populator populator;
    private final World world;

    public CChunkGenerator(World world, Generator generator, Populator populator) {
        this.world = world;
        this.generator = generator;
        this.populator = populator;
        asyncGen = this.generator.maybeAsync();
        asyncPop = this.populator.maybeAsync();
    }

    public interface Generator{
        default boolean maybeAsync(){return false;};
        default void cleanupCache(){};
        Chunk generateChunk(int x, int z);
        default void setBlocksInChunk(int x, int z, ChunkPrimer primer){};
    }


    public interface Populator{
        default boolean maybeAsync(){return false;};
        void populate(int x, int z);
        default void recreateStructures(Chunk chunkIn, int x, int z){};
        default boolean generateStructures(Chunk chunkIn, int x, int z){return false;};
        BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored);
        default boolean isInsideStructure(World worldIn, String structureName, BlockPos pos){return false;}
    }


    @Override
    public void cleanupCache() {
        generator.cleanupCache();
    }


    @Override
    public Chunk generateChunk(int x, int z) {
        return generator.generateChunk(x, z);
    }


    @Override
    public void populate(int x, int z) {
        populator.populate(x, z);
    }


    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return populator.generateStructures(chunkIn, x, z);
    }


    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        Biome biome = this.world.getBiome(pos);
        return biome.getSpawnableList(creatureType);
    }


    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return populator.getNearestStructurePos(worldIn, structureName, position, findUnexplored);
    }


    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        populator.recreateStructures(chunkIn, x, z);
    }


    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return populator.isInsideStructure(worldIn, structureName, pos);
    }


    @Override
    public boolean maybeAsync() {
        return asyncGen;
    }


    @Override
    public boolean maybeAsyncPopulate() {
        return asyncPop;
    }
}
