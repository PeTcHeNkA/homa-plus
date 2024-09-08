package net.minecraft.util.math;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public final class AxisAlignedBB {
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public AxisAlignedBB(double x1, double y1, double z1, double x2, double y2, double z2) {
        if(x1 > x2){
            minX = x2;
            maxX = x1;
        } else {
            minX = x1;
            maxX = x2;
        }
        if(y1 > y2){
            minY = y2;
            maxY = y1;
        } else {
            minY = y1;
            maxY = y2;
        }
        if(z1 > z2){
            minZ = z2;
            maxZ = z1;
        } else {
            minZ = z1;
            maxZ = z2;
        }
    }


    public void init(double x1, double y1, double z1, double x2, double y2, double z2){
        if(x1 > x2){
            minX = x2;
            maxX = x1;
        } else {
            minX = x1;
            maxX = x2;
        }
        if(y1 > y2){
            minY = y2;
            maxY = y1;
        } else {
            minY = y1;
            maxY = y2;
        }
        if(z1 > z2){
            minZ = z2;
            maxZ = z1;
        } else {
            minZ = z1;
            maxZ = z2;
        }
    }


    public void initSafe(double x1, double y1, double z1, double x2, double y2, double z2){
        minX = x1;
        maxX = x2;
        minY = y1;
        maxY = y2;
        minZ = z1;
        maxZ = z2;
    }


    public AxisAlignedBB(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }


    public AxisAlignedBB(BlockPos pos1, BlockPos pos2) {
        this(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }


    @SideOnly(Side.CLIENT)
    public AxisAlignedBB(Vec3d min, Vec3d max) {
        this(min.x, min.y, min.z, max.x, max.y, max.z);
    }


    public AxisAlignedBB setMaxY(double y2) {
        return new AxisAlignedBB(this.minX, this.minY, this.minZ, this.maxX, y2, this.maxZ);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AxisAlignedBB that = (AxisAlignedBB) o;
        if (DoubleNEq(that.minX, minX)) return false;
        if (DoubleNEq(that.minY, minY)) return false;
        if (DoubleNEq(that.minZ, minZ)) return false;
        if (DoubleNEq(that.maxX, maxX)) return false;
        if (DoubleNEq(that.maxY, maxY)) return false;
        return DoubleNEq(that.maxZ, maxZ);
    }


    private static boolean DoubleNEq(double d1, double d2){
        return Double.doubleToLongBits(d1) != Double.doubleToLongBits(d2);
    }


    public int hashCode() {
        long i = Double.doubleToLongBits(this.minX);
        int j = (int)(i ^ i >>> 32);
        i = Double.doubleToLongBits(this.minY);
        j = 31 * j + (int)(i ^ i >>> 32);
        i = Double.doubleToLongBits(this.minZ);
        j = 31 * j + (int)(i ^ i >>> 32);
        i = Double.doubleToLongBits(this.maxX);
        j = 31 * j + (int)(i ^ i >>> 32);
        i = Double.doubleToLongBits(this.maxY);
        j = 31 * j + (int)(i ^ i >>> 32);
        i = Double.doubleToLongBits(this.maxZ);
        j = 31 * j + (int)(i ^ i >>> 32);
        return j;
    }


    public AxisAlignedBB contract(double x, double y, double z) {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;

        if (x < 0.0D) {
            d0 -= x;
        }
        else if (x > 0.0D) {
            d3 -= x;
        }

        if (y < 0.0D) {
            d1 -= y;
        }
        else if (y > 0.0D) {
            d4 -= y;
        }

        if (z < 0.0D) {
            d2 -= z;
        }
        else if (z > 0.0D)
        {
            d5 -= z;
        }
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }


    public AxisAlignedBB expand(double x, double y, double z) {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;

        if (x < 0.0D) {
            d0 += x;
        }
        else if (x > 0.0D) {
            d3 += x;
        }

        if (y < 0.0D) {
            d1 += y;
        }
        else if (y > 0.0D) {
            d4 += y;
        }
        if (z < 0.0D) {
            d2 += z;
        }
        else if (z > 0.0D) {
            d5 += z;
        }
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }


    public void expandTo(double x, double y, double z, AxisAlignedBB src) {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;

        if (x < 0.0D) {
            d0 += x;
        }
        else if (x > 0.0D) {
            d3 += x;
        }

        if (y < 0.0D) {
            d1 += y;
        }
        else if (y > 0.0D) {
            d4 += y;
        }
        if (z < 0.0D) {
            d2 += z;
        }
        else if (z > 0.0D) {
            d5 += z;
        }
        src.initSafe(d0, d1, d2, d3, d4, d5);
    }


    public AxisAlignedBB grow(double x, double y, double z) {
        return new AxisAlignedBB(
                this.minX - x,
                this.minY - y,
                this.minZ - z,
                this.maxX + x,
                this.maxY + y,
                this.maxZ + z
        );
    }


    public void growTo(double x, double y, double z, AxisAlignedBB src) {
        src.initSafe(
                this.minX - x,
                this.minY - y,
                this.minZ - z,
                this.maxX + x,
                this.maxY + y,
                this.maxZ + z
        );
    }


    public AxisAlignedBB grow(double value) {
        return this.grow(value, value, value);
    }


    public AxisAlignedBB intersect(AxisAlignedBB other) {
        double d0 = Math.max(this.minX, other.minX);
        double d1 = Math.max(this.minY, other.minY);
        double d2 = Math.max(this.minZ, other.minZ);
        double d3 = Math.min(this.maxX, other.maxX);
        double d4 = Math.min(this.maxY, other.maxY);
        double d5 = Math.min(this.maxZ, other.maxZ);
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }


    public AxisAlignedBB union(AxisAlignedBB other) {
        double d0 = Math.min(this.minX, other.minX);
        double d1 = Math.min(this.minY, other.minY);
        double d2 = Math.min(this.minZ, other.minZ);
        double d3 = Math.max(this.maxX, other.maxX);
        double d4 = Math.max(this.maxY, other.maxY);
        double d5 = Math.max(this.maxZ, other.maxZ);
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }


    public AxisAlignedBB offset(double x, double y, double z) {
        return new AxisAlignedBB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }


    public void offsetMut(double x, double y, double z) {
        initSafe(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }


    public AxisAlignedBB offset(BlockPos pos) {
        return new AxisAlignedBB(this.minX + (double)pos.getX(), this.minY + (double)pos.getY(), this.minZ + (double)pos.getZ(), this.maxX + (double)pos.getX(), this.maxY + (double)pos.getY(), this.maxZ + (double)pos.getZ());
    }


    public void offsetTo(BlockPos pos, AxisAlignedBB flush) {
        flush.initSafe(this.minX + (double)pos.getX(), this.minY + (double)pos.getY(), this.minZ + (double)pos.getZ(), this.maxX + (double)pos.getX(), this.maxY + (double)pos.getY(), this.maxZ + (double)pos.getZ());
    }


    public void offsetTo(double x, double y, double z, AxisAlignedBB flush) {
        flush.initSafe(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }


    public AxisAlignedBB offsetToF(double x, double y, double z, AxisAlignedBB flush) {
        flush.initSafe(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
        return flush;
    }


    public AxisAlignedBB offset(Vec3d vec) {
        return this.offset(vec.x, vec.y, vec.z);
    }


    public double calculateXOffset(AxisAlignedBB other, double offsetX) {
        if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetX > 0.0D && other.maxX <= this.minX) {
                double d1 = this.minX - other.maxX;
                if (d1 < offsetX) {
                    offsetX = d1;
                }
            } else if (offsetX < 0.0D && other.minX >= this.maxX) {
                double d0 = this.maxX - other.minX;

                if (d0 > offsetX) {
                    offsetX = d0;
                }
            }
        }
        return offsetX;
    }


    public double calculateYOffset(AxisAlignedBB other, double offsetY) {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetY > 0.0D && other.maxY <= this.minY) {
                double d1 = this.minY - other.maxY;
                if (d1 < offsetY) {
                    offsetY = d1;
                }
            } else if (offsetY < 0.0D && other.minY >= this.maxY) {
                double d0 = this.maxY - other.minY;

                if (d0 > offsetY) {
                    offsetY = d0;
                }
            }
        }
        return offsetY;
    }


    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY) {
            if (offsetZ > 0.0D && other.maxZ <= this.minZ) {
                double d1 = this.minZ - other.maxZ;

                if (d1 < offsetZ) {
                    offsetZ = d1;
                }
            }
            else if (offsetZ < 0.0D && other.minZ >= this.maxZ) {
                double d0 = this.maxZ - other.minZ;
                if (d0 > offsetZ) {
                    offsetZ = d0;
                }
            }
        }
        return offsetZ;
    }


    public boolean intersects(AxisAlignedBB other) {
        return this.minX < other.maxX && this.maxX > other.minX && this.minY < other.maxY
                && this.maxY > other.minY && this.minZ < other.maxZ && this.maxZ > other.minZ;
    }


    public boolean intersects(double x1, double y1, double z1, double x2, double y2, double z2) {
        return this.minX < x2 && this.maxX > x1 && this.minY < y2 && this.maxY > y1 && this.minZ < z2 && this.maxZ > z1;
    }


    public boolean intersectsXZY(double x1, double y1, double z1, double x2, double y2, double z2) {
        return this.minX < x2 && this.maxX > x1 && this.minZ < z2 && this.maxZ > z1 && this.minY < y2 && this.maxY > y1;
    }


    @SideOnly(Side.CLIENT)
    public boolean intersects(Vec3d min, Vec3d max) {
        return this.intersects(Math.min(min.x, max.x), Math.min(min.y, max.y), Math.min(min.z, max.z), Math.max(min.x, max.x), Math.max(min.y, max.y), Math.max(min.z, max.z));
    }


    public boolean contains(Vec3d vec) {
        if (vec.x > this.minX && vec.x < this.maxX) {
            if (vec.y > this.minY && vec.y < this.maxY) {
                return vec.z > this.minZ && vec.z < this.maxZ;
            }
            else {
                return false;
            }
        } else {
            return false;
        }
    }


    public double getAverageEdgeLength() {
        double d0 = this.maxX - this.minX;
        double d1 = this.maxY - this.minY;
        double d2 = this.maxZ - this.minZ;
        return (d0 + d1 + d2) / 3.0D;
    }


    public AxisAlignedBB shrink(double value) {
        return this.grow(-value);
    }


    @Nullable
    public RayTraceResult calculateIntercept(Vec3d vecA, Vec3d vecB) {
        Vec3d vec3d = this.collideWithXPlane(this.minX, vecA, vecB);
        EnumFacing enumfacing = EnumFacing.WEST;
        Vec3d vec3d1 = this.collideWithXPlane(this.maxX, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            enumfacing = EnumFacing.EAST;
        }

        vec3d1 = this.collideWithYPlane(this.minY, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            enumfacing = EnumFacing.DOWN;
        }

        vec3d1 = this.collideWithYPlane(this.maxY, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            enumfacing = EnumFacing.UP;
        }

        vec3d1 = this.collideWithZPlane(this.minZ, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            enumfacing = EnumFacing.NORTH;
        }
        vec3d1 = this.collideWithZPlane(this.maxZ, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            enumfacing = EnumFacing.SOUTH;
        }
        return vec3d == null ? null : new RayTraceResult(vec3d, enumfacing);
    }


    @Nullable
    public RayTraceResult calculateInterceptRT(Vec3d vecA, Vec3d vecB, BlockPos pos) {
        Vec3d vec3d = this.collideWithXPlane(this.minX, vecA, vecB);
        EnumFacing facing = EnumFacing.WEST;
        Vec3d vec3d1 = this.collideWithXPlane(this.maxX, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            facing = EnumFacing.EAST;
        }

        vec3d1 = this.collideWithYPlane(this.minY, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            facing = EnumFacing.DOWN;
        }

        vec3d1 = this.collideWithYPlane(this.maxY, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            facing = EnumFacing.UP;
        }

        vec3d1 = this.collideWithZPlane(this.minZ, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            facing = EnumFacing.NORTH;
        }
        vec3d1 = this.collideWithZPlane(this.maxZ, vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            facing = EnumFacing.SOUTH;
        }
        if(vec3d == null)return null;
        return new RayTraceResult(vec3d.add(pos.getX(), pos.getY(), pos.getY()), facing, pos);
    }


    public boolean checkInterceptRT(Vec3d vecA, Vec3d vecB) {
        Vec3d vec3d = this.collideWithXPlane(this.minX, vecA, vecB);
        if(vec3d != null)return true;
        Vec3d vec3d1 = this.collideWithXPlane(this.maxX, vecA, vecB);

        if (vec3d1 != null) return true;

        vec3d1 = this.collideWithYPlane(this.minY, vecA, vecB);

        if (vec3d1 != null) return true;

        vec3d1 = this.collideWithYPlane(this.maxY, vecA, vecB);

        if (vec3d1 != null) return true;

        vec3d1 = this.collideWithZPlane(this.minZ, vecA, vecB);

        if (vec3d1 != null) return true;

        vec3d1 = this.collideWithZPlane(this.maxZ, vecA, vecB);

        return vec3d1 != null;
    }


    @VisibleForTesting
    boolean isClosest(Vec3d p_186661_1_, @Nullable Vec3d p_186661_2_, Vec3d p_186661_3_) {
        return p_186661_2_ == null || p_186661_1_.squareDistanceTo(p_186661_3_) < p_186661_1_.squareDistanceTo(p_186661_2_);
    }


    @Nullable
    @VisibleForTesting
    Vec3d collideWithXPlane(double factor, Vec3d vec1, Vec3d vec2) {
        Vec3d vec3d = vec1.getIntermediateWithXValue(vec2, factor);
        return vec3d != null && this.intersectsWithYZ(vec3d) ? vec3d : null;
    }


    @Nullable
    @VisibleForTesting
    Vec3d collideWithYPlane(double factor, Vec3d vec1, Vec3d vec2) {
        Vec3d vec3d = vec1.getIntermediateWithYValue(vec2, factor);
        return vec3d != null && this.intersectsWithXZ(vec3d) ? vec3d : null;
    }


    @Nullable
    @VisibleForTesting
    Vec3d collideWithZPlane(double factor, Vec3d vec1, Vec3d vec2) {
        Vec3d vec3d = vec1.getIntermediateWithZValue(vec2, factor);
        return vec3d != null && this.intersectsWithXY(vec3d) ? vec3d : null;
    }


    @VisibleForTesting
    public boolean intersectsWithYZ(Vec3d vec) {
        return vec.y >= this.minY && vec.y <= this.maxY && vec.z >= this.minZ && vec.z <= this.maxZ;
    }


    @VisibleForTesting
    public boolean intersectsWithXZ(Vec3d vec) {
        return vec.x >= this.minX && vec.x <= this.maxX && vec.z >= this.minZ && vec.z <= this.maxZ;
    }


    @VisibleForTesting
    public boolean intersectsWithXY(Vec3d vec) {
        return vec.x >= this.minX && vec.x <= this.maxX && vec.y >= this.minY && vec.y <= this.maxY;
    }


    public String toString() {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }


    @SideOnly(Side.CLIENT)
    public boolean hasNaN() {
        return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getCenter() {
        return new Vec3d(this.minX + (this.maxX - this.minX) * 0.5D, this.minY + (this.maxY - this.minY) * 0.5D, this.minZ + (this.maxZ - this.minZ) * 0.5D);
    }


    public AxisAlignedBB copy(){
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}