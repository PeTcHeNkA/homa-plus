package plus.utls;

public class Vec2d {
    public double y;
    public double x;

    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2d(){
        this(0,0);
    }

    public boolean isNear(Vec2d vec, float f){
        return MathUtils.isNear(x, vec.x, f) && MathUtils.isNear(y, vec.y, f);
    }

    public boolean isNear(Vec2d vec){
        return MathUtils.isNear(x, vec.x) && MathUtils.isNear(y, vec.y);
    }
}