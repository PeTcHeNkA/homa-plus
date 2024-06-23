package plus.fserver.utls.math;


public class MathUtls {
    public static final float F_180_PI = 180F / (float)Math.PI;
    public static final float F_256_T_360 = 0.7111111111111111F; //= 256 / 360

    public static boolean isInvalidDouble(double d){
        return d != d || d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY;
    }


    public static int clamp16c(double value){
        int i = (int)value;
        return (i = (value < (double)i ? i - 1 : i)>>4) < 0 ? 0 : (i > 15 ? 15 : i);
    }


    public static boolean checkAbs0_003D(double motion){
        return motion < -0.003D || motion > 0.003D;
        //Math.abs(this.motionX) < 0.003D)
    }
}