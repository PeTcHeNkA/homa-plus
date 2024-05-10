package net.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenTallGrass extends WorldGenerator {
    private final IBlockState tallGrassState;

    public WorldGenTallGrass(BlockTallGrass.EnumType type) {
        this.tallGrassState = Blocks.TALLGRASS.getDefaultState()
                .withProperty(BlockTallGrass.TYPE, type);
    }


    public boolean generate(World worldIn, Random rand, BlockPos position) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(position);
        for (IBlockState iblockstate = selector.getState(pos);
             (iblockstate.getBlock().isAir(iblockstate, worldIn, pos)
                        || iblockstate.getBlock().isLeaves(iblockstate, worldIn, pos))
                        && pos.getY() > 0; iblockstate = selector.getCurState(pos)) {
            pos.setY(pos.getY()-1);
        }
        position = new BlockPos(pos.getX(), pos.getY()+1, pos.getZ());

        for (int i = 0; i < 128; ++i) {
            FastRandom.INST.setRandom8x8x4(pos, position);
            if (pos.getY() > 0 && pos.getY() < 255 && selector.isAirBlock(pos)) {
                pos.setY(pos.getY()-1);
                if(BlockBush.canGrowFlower(Blocks.TALLGRASS, selector.getCurState(pos))){
                    selector.setCurStateFast(pos.getX(), pos.getY()+1, pos.getZ(), tallGrassState);
                }
            }
        }
        return true;
    }
}