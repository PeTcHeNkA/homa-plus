package plus.common.mashine.block.scan;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import plus.fserver.api.ElcaServerUtls;


public abstract class BlockScanner {
    public BlockWay scan(BlockWay positions, World world, BlockPos pos){
        try {
            addIfSuccess(positions, ElcaServerUtls.access(world), pos);
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


    public BlockWay scan(World world, BlockPos pos){
        BlockWay way = new BlockWay();
        scan(way, world, pos);
        return way;
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

    
    protected void addNearest(BlockWay data, IBlockAccess access, BlockPos pos){
        addIfSuccess(data, access, new BlockPos(pos.getX(), pos.getY()-1, pos.getZ()));
        addIfSuccess(data, access, new BlockPos(pos.getX(), pos.getY()+1, pos.getZ()));
        addIfSuccess(data, access, new BlockPos(pos.getX()-1, pos.getY(), pos.getZ()));
        addIfSuccess(data, access, new BlockPos(pos.getX()+1, pos.getY(), pos.getZ()));
        addIfSuccess(data, access, new BlockPos(pos.getX(), pos.getY(), pos.getZ()-1));
        addIfSuccess(data, access, new BlockPos(pos.getX(), pos.getY(), pos.getZ()+1));
    }


    protected void addIfSuccess(BlockWay data, IBlockAccess access, BlockPos pos){
        Block block = access.getBlockState(pos).getBlock();
        if(block != Blocks.AIR) {
            if(!data.contains(pos)) {
                if (isProvideBlock(block)) {
                    data.add(pos);
                    addNearest(data, access, pos);
                } else if (isResultBlock(block)) {
                    data.add(pos);
                    data.addResult(pos);
                    addNearest(data, access, pos);
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
}