package plus.space.server.gen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.world.generator.struct.IStruct;
import plus.fserver.world.generator.struct.Structure;
import plus.fserver.world.generator.vr.MultiChunkGenerator;
import plus.space.server.SolarWorldItem;
import plus.tson.TsonField;
import javax.annotation.Nullable;
import java.util.List;


public class EOrbitGenerator extends MultiChunkGenerator {
    private final IBlockState[] blocks;
    public EOrbitGenerator(WorldServer world, SolarWorldItem item) {
        super(world);
        blocks = new IBlockState[]{
                Block.getBlockById(1).getDefaultState(),
                Block.getBlockById(2).getDefaultState(),
                Block.getBlockById(3).getDefaultState(),
                Block.getBlockById(4).getDefaultState()
        };
    }


    private static final IStruct meteor = TsonField.build(Structure.manager(),
            "<(##), '[]test'>"
    ).getField(IStruct.class);


    @Override
    protected void onGenerate(int chunkX, int chunkZ) {
        if(1==1)return;//TODO CRITICAL ERROR IN TEMPLATE
        if(!inCircle(chunkX, chunkZ, 0, 0, 100, 500))return;
        for(int count = 1+world.rand.nextInt(3);count>0;count--){
            super.place(chunkX, chunkZ,
                    world.rand.nextInt(16),
                    16+world.rand.nextInt(96),
                    world.rand.nextInt(16),
                    meteor
            );
        }
    }


    private boolean inCircle(int chunkX, int chunkZ, int p0x, int p0z, int minSq, int maxSq){
        int x = chunkX-p0x;
        int z = chunkZ-p0z;
        x = x*x+z*z;
        return x>minSq && x<maxSq;
    }


//    @Override
//    public Chunk generateChunk(int x, int z) {
//        throw new RuntimeException();
//    }


    @Override
    public void populate(int x, int z) {

    }


    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }


    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return null;
    }


    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return null;
    }


    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {

    }


    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }
}