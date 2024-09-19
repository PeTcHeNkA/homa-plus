package plus.fserver.entity.ai;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;


public class TeleportInfo extends BlockPos {
    public EnumFacing direction;
    public double dx, dy;
    public EnumFacing facing;

    public TeleportInfo(int x, int y, int z) {
        super(x, y, z);
    }


    public void setVec(double x, double y, EnumFacing facing){
        dx = x; dy = y;
    }


    public Vec3d getVec(){
        return new Vec3d(dx, dy, 0D);
    }
}