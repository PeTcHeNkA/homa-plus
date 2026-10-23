package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenFlowers extends CWorldGenerator {
    private BlockFlower flower;
    private IBlockState state;

    @Keep
    public WorldGenFlowers(BlockFlower block, BlockFlower.EnumFlowerType type) {
        this.setGeneratedBlock(block, type);
    }


    @Keep
    public void setGeneratedBlock(BlockFlower block, BlockFlower.EnumFlowerType type) {
        this.flower = block;
        this.state = block.getDefaultState().withProperty(block.getTypeProperty(), type);
    }


    @Override
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos pos) {
        generate(selector, rand, pos, flower, state);
        return true;
    }


    public void generate(ChunkSelector selector, Random rand, BlockPos pos, BlockBush flower, IBlockState state) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for(int num = 0; num < 64; ++num) {
            FastRandom.setRandom8x8x4(rand, mut, pos);

            if (mut.getY() > 0 && mut.getY() < 255 && selector.isAirBlock(mut)) {
                mut.setY(mut.getY()-1);
                if(BlockBush.canGrowFlower(flower, selector.getCurState(mut)))
                    selector.setCurStateFast(mut.getX(), mut.getY()+1, mut.getZ(), state);
            }
        }
    }
}
