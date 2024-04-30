package plus.fserver.utls.math;

public class MathUtls {
    public static final float F_180_PI = 180F / (float)Math.PI;
    public static final float F_256_T_360 = 0.7111111111111111F; //= 256 / 360

    public static boolean isInvalidDouble(double d){
        return d != d || d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY;
    }
}