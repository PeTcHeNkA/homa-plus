package plus.fserver.utls.math;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;


@Deprecated
public class BlockPosMutDelta extends BlockPos {
    private int x, y, z, sx, sy, sz;
    public BlockPosMutDelta(int x, int y, int z) {
        super(0, 0, 0);
        this.x = sx = x;
        this.y = sy = y;
        this.z = sz = z;
    }


    public BlockPosMutDelta setPos(int x, int y, int z){
        this.x = sx = x;
        this.y = sy = y;
        this.z = sz = z;
        return this;
    }


    public BlockPosMutDelta(BlockPos pos) {
        super(0,0,0);
        this.x = sx = pos.getX();
        this.y = sy = pos.getY();
        this.z = sz = pos.getZ();
    }


    public BlockPosMutDelta delta(int dx, int dy, int dz){
        x = sx + dx;
        y = sy + dy;
        z = sz + dz;
        return this;
    }


    public BlockPosMutDelta delta(EnumFacing side){
        x = sx + side.getXOffset();
        y = sy + side.getYOffset();
        z = sz + side.getZOffset();
        return this;
    }


    public BlockPosMutDelta dX(int dx){
        x = sx + dx;
        y = sy;
        z = sz;
        return this;
    }


    public BlockPosMutDelta dY(int dy){
        x = sx;
        y = sy + dy;
        z = sz;
        return this;
    }


    public BlockPosMutDelta dZ(int dz){
        x = sx;
        y = sy;
        z = sz + dz;
        return this;
    }


    public BlockPosMutDelta useDelta(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }


    public BlockPosMutDelta shiftY(int y){
        this.y += y;
        return this;
    }
//
//
//    @Override
//    public int getX() {
//        return x;
//    }
//
//
//    @Override
//    public int getY() {
//        return y;
//    }
//
//
//    @Override
//    public int getZ() {
//        return z;
//    }


    @Override
    public BlockPos toImmutable() {
        return new BlockPos(x, y, z);
    }


    public BlockPosMutDelta reset() {
        x = super.getX();
        y = super.getY();
        z = super.getZ();
        return this;
    }
}