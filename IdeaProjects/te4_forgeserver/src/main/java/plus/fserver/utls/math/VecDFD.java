package plus.fserver.utls.math;


public class VecDFD {
    public double x;
    public float  y;
    public double z;

    public VecDFD(double xIn, float yIn, double zIn) {
        if (xIn == -0.0) {
            xIn = 0.0;
        }
        if (yIn == -0.0f) {
            yIn = 0.0f;
        }
        if (zIn == -0.0) {
            zIn = 0.0;
        }
        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
    }


    @Override
    public boolean equals(final Object vec) {
        if (this == vec) {
            return true;
        }
        if (!(vec instanceof VecDFD)) {
            return false;
        }
        final VecDFD vec3d = (VecDFD) vec;
        return Double.compare(vec3d.x, this.x) == 0 && Float.compare(vec3d.y, this.y) == 0 && Double.compare(vec3d.z, this.z) == 0;
    }


    @Override
    public int hashCode() {
        long j = Double.doubleToRawLongBits(this.x);
        int i = (int)(j ^ j >>> 32);
        j = Float.floatToRawIntBits(this.y);
        i = 31 * i + (int)(j ^ j >>> 32);
        j = Double.doubleToRawLongBits(this.z);
        i = 31 * i + (int)(j ^ j >>> 32);
        return i;
    }


    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}
