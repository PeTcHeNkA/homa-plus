package net.minecraft.world.gen.feature;


import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;
import java.util.Random;


@Keep
public abstract class CWorldGenerator extends WorldGenerator{
    @Keep
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        return generate(worldIn, new ChunkSelector(worldIn), rand, position);
    }


    @Keep
    @Override
    public abstract boolean generate(World worldIn, ChunkSelector selector, Random rand, BlockPos position);
}