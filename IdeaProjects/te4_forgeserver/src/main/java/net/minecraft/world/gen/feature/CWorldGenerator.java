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
    public boolean generate(World world, Random rand, BlockPos pos) {
        return generate(world, new ChunkSelector(world), rand, pos);
    }


    @Keep
    @Override
    public abstract boolean generate(World world, ChunkSelector selector, Random rand, BlockPos position);
}