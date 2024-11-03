package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.ElcaThread;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenTallGrass extends CWorldGenerator {
    private final IBlockState tallGrassState;

    @Keep
    public WorldGenTallGrass(BlockTallGrass.EnumType type) {
        this.tallGrassState = Blocks.TALLGRASS.getDefaultState()
                .withProperty(BlockTallGrass.TYPE, type);
    }


    @Override
    public boolean generate(World worldIn, ChunkSelector selector, Random rand, BlockPos pos) {
        ElcaThread.Cache cache = ElcaThread.cache(worldIn);
        BlockPos.MutableBlockPos mut = cache.pos1.setPos(pos);
        for (IBlockState iblockstate = selector.getState(mut);
             (iblockstate.getBlock().isAir(iblockstate, worldIn, mut)
                        || iblockstate.getBlock().isLeaves(iblockstate, worldIn, mut))
                        && mut.getY() > 0; iblockstate = selector.getCurState(mut)) {
            mut.setY(mut.getY()-1);
        }
        pos = cache.pos2.setPos(mut.getX(), mut.getY()+1, mut.getZ());

        for (int i = 0; i < 128; ++i) {
            FastRandom.INST.setRandom8x8x4(mut, pos);
            if (mut.getY() > 0 && mut.getY() < 255 && selector.isAirBlock(mut)) {
                mut.setY(mut.getY()-1);
                if(BlockBush.canGrowFlower(Blocks.TALLGRASS, selector.getCurState(mut))){
                    selector.setCurStateFast(mut.getX(), mut.getY()+1, mut.getZ(), tallGrassState);
                }
            }
        }
        return true;
    }
}