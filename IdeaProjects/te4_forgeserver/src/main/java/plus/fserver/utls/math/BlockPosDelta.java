package plus.fserver.utls.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;


public class BlockPosDelta extends BlockPos {
    private int sx, sy, sz;
    public BlockPosDelta(int x, int y, int z) {
        super(x, y, z);
        this.sx = x;
        this.sy = y;
        this.sz = z;
    }


    public BlockPosDelta(Entity entity) {
        super(entity);
        sx = super.getX();
        sy = super.getY();
        sz = super.getZ();
    }


    public BlockPosDelta(BlockPos pos) {
        super(pos);
        sx = pos.getX();
        sy = pos.getY();
        sz = pos.getZ();
    }


    public BlockPosDelta delta(int dx, int dy, int dz){
        editRoot(
                sx + dx,
                sy + dy,
                sz + dz
        );
        return this;
    }


    public BlockPosDelta delta(EnumFacing side){
        editRoot(
                sx + side.getXOffset(),
                sy + side.getYOffset(),
                sz + side.getZOffset()
        );
        return this;
    }


    public BlockPosDelta dX(int dx){
        editRoot(
                sx+dx,
                sy,
                sz
        );
        return this;
    }


    public BlockPosDelta dY(int dy){
        editRoot(
                sx, sy+dy, sz
        );
        return this;
    }


    public BlockPosDelta dZ(int dz){
        editRoot(
                sx, sy, sz+dz
        );
        return this;
    }


    public BlockPosDelta useDelta(int x, int y, int z){
        editRoot(x, y, z);
        return this;
    }


    public BlockPosDelta shiftY(int y){
        editRootY(getY() + y);
        return this;
    }


    @Override
    public BlockPos toImmutable() {
        return new BlockPos(getX(), getY(), getZ());
    }


    public BlockPosDelta reset() {
        editRoot(sx, sy, sz);
        return this;
    }


    public BlockPosDelta setPos(int x, int y, int z) {
        editRoot(sx = x, sy = y, sz = z);
        return this;
    }
}