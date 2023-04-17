package plus.common.mashine.block.scan;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public abstract class BlockScanner {
    public BlockWay scan(BlockWay positions, World world, BlockPos pos){
        try {
            addIfSuccess(positions, world, pos);
            return positions;
        } catch (StackOverflowError e){
            int i=1;
            for(BlockPos pos0:positions){
                System.out.printf("|%s| %s%n", i, pos0);
                i++;
            }
        }
        return positions;
    }


    public void doScan(World world, BlockPos pos){
        completeScan(scan(new BlockWay(), world, pos), world, pos);
    }


    public void completeScan(BlockWay way, World world, BlockPos pos){
        BlockWay.BlockWayResults results = way.getResults();
        if (results.size()>0) {
            onSuccess(world, pos, results);
        }
    }

    
    protected void addNearest(BlockWay data, World world, BlockPos pos){
        addIfSuccess(data, world, yPositive(pos));
        addIfSuccess(data, world, yNegative(pos));
        addIfSuccess(data, world, xPositive(pos));
        addIfSuccess(data, world, xNegative(pos));
        addIfSuccess(data, world, zPositive(pos));
        addIfSuccess(data, world, zNegative(pos));
    }


    protected void addIfSuccess(BlockWay data, World world, BlockPos pos){
        Block block = world.getBlockState(pos).getBlock();
        if(block != Blocks.AIR) {
            if(!data.contains(pos)) {
                if (isProvideBlock(block)) {
                    data.add(pos);
                    addNearest(data, world, pos);
                } else if (isResultBlock(block)) {
                    data.add(pos);
                    data.addResult(pos);
                    addNearest(data, world, pos);
                }
            }
        }
    }


    public BlockWay joinScan(BlockWay way, World world, BlockPos startPos, BlockPos curPos){
        if(!way.contains(curPos)) {
            completeScan(way, world, startPos);
            return scan(new BlockWay(), world, curPos);
        }
        return way;
    }


    protected abstract boolean isResultBlock(Block block);


    protected abstract boolean isProvideBlock(Block block);


    protected abstract void onSuccess(World world, BlockPos pos, BlockWay.BlockWayResults results);


    private BlockPos yPositive(BlockPos pos){
        return new BlockPos(pos.getX(), pos.getY()+1, pos.getZ());
    }
    private BlockPos yNegative(BlockPos pos){
        return new BlockPos(pos.getX(), pos.getY()-1, pos.getZ());
    }
    private BlockPos xPositive(BlockPos pos){
        return new BlockPos(pos.getX()+1, pos.getY(), pos.getZ());
    }
    private BlockPos xNegative(BlockPos pos){
        return new BlockPos(pos.getX()-1, pos.getY(), pos.getZ());
    }
    private BlockPos zPositive(BlockPos pos){
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ()+1);
    }
    private BlockPos zNegative(BlockPos pos){
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ()-1);
    }
}