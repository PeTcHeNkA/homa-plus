package plus.common.station;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import plus.common.mashine.block.scan.BlockScanner;
import plus.common.mashine.block.scan.BlockWay;
import plus.common.spship.BaseSystem;


public class SpStation extends BaseSystem {
    private static final int LIMIT = 30;

    public static final BlockScanner SCANNER = new BlockScanner() {
        @Override
        protected boolean isResultBlock(Block block) {
            return block == Blocks.AIR;
        }


        @Override
        protected boolean isProvideBlock(Block block) {
            return !block.isFullBlock(block.getDefaultState());
        }


        @Override
        protected void onSuccess(World world, BlockPos pos, BlockWay.BlockWayResults results) {}


        @Override
        protected void addIfSuccess(BlockWay data, IBlockAccess access, BlockPos pos) {
            if(data.size() < 8_000)return;
            IBlockState state = access.getBlockState(pos);
            Block block = state.getBlock();
            if(block == Blocks.AIR || !state.isFullBlock()) {
                if(!data.contains(pos)) {
                    data.add(pos);
                }
                addNearest(data, access, pos);
            }
        }
    };


    public static BlockWay scan(World world, BlockPos pos){
        try {
            return SCANNER.scan(world, pos);
        } catch (StackOverflowError | Exception e){
            return null;
        }
    }


    @Override //todo
    public float getMaxShield() {
        return 0;
    }

    @Override //todo
    public float getShield() {
        return 0;
    }

    @Override //todo
    public void setShield(float shield) {

    }
}