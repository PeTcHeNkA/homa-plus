package plus.fserver.utls.math;

import javax.annotation.Nullable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public class Vec3dMut {
    public static final Vec3dMut ZERO = new Vec3dMut(0.0D, 0.0D, 0.0D);
    public double x;
    public double y;
    public double z;

    public Vec3dMut(double xIn, double yIn, double zIn) {
        if (xIn == -0.0D) {
            xIn = 0.0D;
        }
        if (yIn == -0.0D) {
            yIn = 0.0D;
        }
        if (zIn == -0.0D) {
            zIn = 0.0D;
        }
        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
    }


    public void initV(double xIn, double yIn, double zIn){
        if (xIn == -0.0D) {
            xIn = 0.0D;
        }
        if (yIn == -0.0D) {
            yIn = 0.0D;
        }
        if (zIn == -0.0D) {
            zIn = 0.0D;
        }
        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
    }


    public Vec3dMut init(double xIn, double yIn, double zIn){
        initV(xIn, yIn, zIn);
        return this;
    }


    public Vec3dMut subtractReverse(double x, double y, double z) {
        initV(x - this.x, y - this.y, z - this.z);
        return this;
    }


    public Vec3dMut normalize() {
        double d0 = MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return d0 < 1.0E-4D ? ZERO : init(this.x / d0, this.y / d0, this.z / d0);
    }


    public double dotProduct(double x, double y, double z) {
        return this.x * x + this.y * y + this.z * z;
    }


    public Vec3dMut crossProduct(double x, double y, double z) {
        return init(this.y * z - this.z * y, this.z * x - this.x * z, this.x * y - this.y * x);
    }


    public Vec3dMut subtract(double x, double y, double z) {
        return this.add(-x, -y, -z);
    }


    public Vec3dMut add(double x, double y, double z) {
        return init(this.x + x, this.y + y, this.z + z);
    }


    public void addY(double y) {
        this.y += y;
    }


    public double distanceTo(double x, double y, double z) {
        double d0 = x - this.x;
        double d1 = y - this.y;
        double d2 = z - this.z;
        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }


    public double squareDistanceTo(double x, double y, double z) {
        double d0 = x - this.x;
        double d1 = y - this.y;
        double d2 = z - this.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }


    public Vec3dMut scale(double factor) {
        return init(this.x * factor, this.y * factor, this.z * factor);
    }


    public double length() {
        return MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }


    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }


    @Nullable
    public Vec3dMut getIntermediateWithXValue(double x, double y, double z, double factor) {
        double d0 = x - this.x;
        double d1 = y - this.y;
        double d2 = z - this.z;

        if (d0 * d0 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (factor - this.x) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? init(this.x + d0 * d3, this.y + d1 * d3, this.z + d2 * d3) : null;
        }
    }


    @Nullable
    public Vec3dMut getIntermediateWithYValue(double x, double y, double z, double factor) {
        double d0 = x - this.x;
        double d1 = y - this.y;
        double d2 = z - this.z;

        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (factor - this.y) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? init(this.x + d0 * d3, this.y + d1 * d3, this.z + d2 * d3) : null;
        }
    }


    @Nullable
    public Vec3dMut getIntermediateWithZValue(double x, double y, double z, double factor) {
        double d0 = x - this.x;
        double d1 = y - this.y;
        double d2 = z - this.z;

        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else
        {double d3 = (factor - this.z) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? init(this.x + d0 * d3, this.y + d1 * d3, this.z + d2 * d3) : null;
        }
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof Vec3dMut)) {
            return false;
        } else {
            Vec3dMut vec3d = (Vec3dMut) other;

            if (Double.compare(vec3d.x, this.x) != 0) {
                return false;
            } else if (Double.compare(vec3d.y, this.y) != 0) {
                return false;
            } else {
                return Double.compare(vec3d.z, this.z) == 0;
            }
        }
    }


    public Vec3d vec(){
        return new Vec3d(x, y, z);
    }


    @Override
    public int hashCode() {
        long j = Double.doubleToLongBits(this.x);
        int i = (int)(j ^ j >>> 32);
        j = Double.doubleToLongBits(this.y);
        i = 31 * i + (int)(j ^ j >>> 32);
        j = Double.doubleToLongBits(this.z);
        i = 31 * i + (int)(j ^ j >>> 32);
        return i;
    }


    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }


    public Vec3dMut rotatePitch(float pitch) {
        float f = MathHelper.cos(pitch);
        float f1 = MathHelper.sin(pitch);
        double d0 = this.x;
        double d1 = this.y * (double)f + this.z * (double)f1;
        double d2 = this.z * (double)f - this.y * (double)f1;
        return init(d0, d1, d2);
    }


    public Vec3dMut rotateYaw(float yaw) {
        float f = MathHelper.cos(yaw);
        float f1 = MathHelper.sin(yaw);
        double d0 = this.x * (double)f + this.z * (double)f1;
        double d1 = this.y;
        double d2 = this.z * (double)f - this.x * (double)f1;
        return init(d0, d1, d2);
    }
}
