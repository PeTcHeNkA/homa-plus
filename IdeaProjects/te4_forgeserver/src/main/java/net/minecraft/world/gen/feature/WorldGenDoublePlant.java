package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenDoublePlant extends CWorldGenerator {
    private final ThreadLocal<BlockDoublePlant.EnumPlantType> TYPE = ThreadLocal.withInitial(() -> BlockDoublePlant.EnumPlantType.GRASS);

    public WorldGenDoublePlant() {}


    public void setPlantType(BlockDoublePlant.EnumPlantType type) {
        TYPE.set(type);
    }


    @Override
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos pos) {
        return generate(world, selector, rand, pos, TYPE.get());
    }


    public static boolean generate(World world, ChunkSelector selector, Random rand, BlockPos pos,  BlockDoublePlant.EnumPlantType type) {
        boolean generated = false;

        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        for(int i = 0; i < 64; ++i) {
            FastRandom.setRandom8x8x4(rand, mut, pos);

            if (selector.isAirBlock(mut) && (!world.provider.isNether() || mut.getY() < 254) && Blocks.DOUBLE_PLANT.canPlaceBlockAt(world, mut)) {
                placeAt(Blocks.DOUBLE_PLANT.getDefaultState(), selector, mut, type);
                generated = true;
            }
        }
        return generated;
    }


    private static void placeAt(IBlockState state, final ChunkSelector selector, final BlockPos.MutableBlockPos pos, final BlockDoublePlant.EnumPlantType type) {
        selector.setBlockState(pos, state.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER).withProperty(BlockDoublePlant.VARIANT, type), 2);
        pos.setY(pos.getY()+1);
        selector.setBlockState(pos, state.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 2);
    }
}