package plus.common.mashine.block.scan;

import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.HashSet;


public class BlockWay extends HashSet<BlockPos> {
    private final BlockWayResults results = new BlockWayResults();


    public void addResult(BlockPos pos) {
        results.add(pos);
    }


    public BlockWayResults getResults() {
        results.countStructureBlocks = this.size();
        return results;
    }


    public static class BlockWayResults extends ArrayList<BlockPos>{
        private int countStructureBlocks = 0;
        public int getStructSize(){
            return countStructureBlocks;
        }
    }
}