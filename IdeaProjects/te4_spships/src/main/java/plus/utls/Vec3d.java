package plus.utls;

public class Vec3d extends Vec2d{
    public double z;

    public Vec3d(double x, double y, double z) {
        super(x, y);
        this.z = z;
    }
    public Vec3d(){
        this(0,0,0);
    }

    public boolean isNear(Vec3d vec3d, float f){
        return super.isNear(vec3d, f) && MathUtils.isNear(z, vec3d.z, f);
    }

    public boolean isNear(Vec3d vec3d){
        return super.isNear(vec3d) && MathUtils.isNear(z, vec3d.z);
    }
}