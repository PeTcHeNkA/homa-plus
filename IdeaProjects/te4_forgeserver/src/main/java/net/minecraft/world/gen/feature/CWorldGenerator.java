package net.minecraft.world.gen.feature;


import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.utls.ChunkSelector;
import java.util.Random;


public abstract class CWorldGenerator extends WorldGenerator{
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        return generate(worldIn, new ChunkSelector(worldIn), rand, position);
    }


    @Override
    public abstract boolean generate(World worldIn, ChunkSelector selector, Random rand, BlockPos position);
}