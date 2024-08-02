package plus.fserver.utls.collections;

import net.minecraft.util.math.BlockPos;


public final class BlockPosQueue extends BlockPos.MutableBlockPos {
    private BlockPosQueue next;

    public BlockPosQueue(int x, int y, int z){
        super(x, y, z);
    }

    public BlockPosQueue(int x, int y, int z, BlockPosQueue next){
        super(x, y, z);
        this.next = next;
    }


    public void addToQueue(int x, int y, int z){
        this.next = new BlockPosQueue(x, y, z, next);
    }


    public BlockPosQueue next(){
        return next;
    }


    public void clear(){
        next = null;
    }


    public boolean contains(int x, int y, int z){
        if(equals(x, y, z))return true;
        if(next != null)return next.contains(x, y, z);
        return false;
    }


    public boolean containsInDeep(int x, int y, int z, int deep){
        if(equals(x, y, z))return true;
        if(next != null && deep >= 0)return next.containsInDeep(x, y, z, deep -1);
        return false;
    }
}
