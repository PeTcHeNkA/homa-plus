package plus.fserver.world.addon;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import org.jetbrains.annotations.Nullable;
import plus.fserver.world.generator.GenUtils;
import java.util.List;


public class EmptyChunkGenerator implements IChunkGenerator {
    protected final World world;

    public EmptyChunkGenerator(World world) {
        this.world = world;
    }

    public static Chunk newEmptyChunk(World world, int x, int z){
        Chunk chunk = new Chunk(world, x, z);
        chunk.setLightPopulated(true);
        chunk.setTerrainPopulated(true);
        return chunk;
    }


    @Override
    public Chunk generateChunk(int x, int z) {
        return newEmptyChunk(world, x, z);
    }


    @Override
    public void populate(int x, int z) {}


    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }


    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return GenUtils.getPossibleCreatures(world, creatureType, pos);
    }


    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return null;
    }


    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {}


    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }
}
