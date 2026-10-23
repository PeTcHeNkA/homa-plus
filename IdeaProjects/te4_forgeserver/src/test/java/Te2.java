import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import plus.fserver.api.IBlockEAccess;


public class Te2 {

    private boolean rtxForExplosion(IBlockEAccess selector, Vec3d vec31, Vec3d vec32) {
        //null -> false
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int sX   = MathHelper.floor(vec32.x);
                int sY   = MathHelper.floor(vec32.y);
                int sZ   = MathHelper.floor(vec32.z);
                int xPos = MathHelper.floor(vec31.x);
                int yPos = MathHelper.floor(vec31.y);
                int zPos = MathHelper.floor(vec31.z);

                final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(xPos, yPos, zPos);

                IBlockState state = selector.getBlockState(blockPos);

                if (state.getBlock().hasCollisionRayTrace(state, selector, blockPos, vec31, vec32)) return true;


                EnumFacing enumfacing;


                int k1 = 200;

                double num1 = 0, num2 = 0, num3 = 0, num4, num5, num6, num7, num8, num9;
                double vecX = vec31.x, vecY = vec31.y, vecZ = vec31.z;

                while (k1-- >= 0) {
                    if (Double.isNaN(vecX) || Double.isNaN(vecY) || Double.isNaN(vecZ)) {
                        return false;
                    }
                    if (xPos == sX && yPos == sY && zPos == sZ) {
                        return false;
                    }

                    num7 = vec32.x - vecX;
                    num8 = vec32.y - vecY;
                    num9 = vec32.z - vecZ;

                    if (sX > xPos) {
                        num1 = xPos + 1d;
                        num4 = (num1 - vecX) / num7;
                    } else if (sX < xPos) {
                        num1 = xPos;
                        num4 = (num1 - vecX) / num7;
                    } else {
                        num4 = 999d;
                    }

                    if (sY > yPos) {
                        num2 = yPos + 1;
                        num5 = (num2 - vecY) / num8;
                    } else if (sY < yPos) {
                        num2 = yPos;
                        num5 = (num2 - vecY) / num8;
                    } else {
                        num5 = 999d;
                    }

                    if (sZ > zPos) {
                        num3 = zPos + 1;
                        num6 = (num3 - vecZ) / num9;
                    } else if (sZ < zPos) {
                        num3 = zPos;
                        num6 = (num3 - vecZ) / num9;
                    } else {
                        num6 = 999d;
                    }

                    if (num4 == -0.0D) num4 = -1.0E-4D;
                    if (num5 == -0.0D) num5 = -1.0E-4D;
                    if (num6 == -0.0D) num6 = -1.0E-4D;

                    if (num4 < num5 && num4 < num6) {
                        enumfacing = sX > xPos ? EnumFacing.WEST : EnumFacing.EAST;
                        vecX = num1; vecY = vecY + num8 * num4; vecZ = vecZ + num9 * num4;
                    } else if (num5 < num6) {
                        enumfacing = sY > yPos ? EnumFacing.DOWN : EnumFacing.UP;
                        vecX = vecX + num7 * num5; vecY = num2; vecZ = vecZ + num9 * num5;
                    } else {
                        enumfacing = sZ > zPos ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vecX = vecX + num7 * num6; vecY = vecY + num8 * num6; vecZ = num3;
                    }

                    state = selector.getBlockState(
                            xPos = MathHelper.floor(vecX) - (enumfacing == EnumFacing.EAST ? 1 : 0),
                            yPos = MathHelper.floor(vecY) - (enumfacing == EnumFacing.UP ? 1 : 0),
                            zPos = MathHelper.floor(vecZ) - (enumfacing == EnumFacing.SOUTH ? 1 : 0)
                    );

                    blockPos.setPosB(xPos, yPos, zPos);
                    if (state.getBlock().canCollideCheck(state, false)) {
                        if (state.getBlock().hasCollisionRayTrace(state, selector, blockPos, new Vec3d(vecX, vecY, vecZ), vec32)) return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}