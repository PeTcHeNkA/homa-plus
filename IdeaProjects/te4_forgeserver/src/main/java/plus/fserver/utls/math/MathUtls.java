package plus.fserver.utls.math;


public class MathUtls {
    public static final float F_180_PI = 180F / (float)Math.PI;
    public static final float F_256_T_360 = 0.7111111111111111F; //= 256 / 360
    public static final Integer INT_300 = new Integer(300);
    public static final Integer INT_0   = new Integer(0);
    public static final float D1_F24 = 1f / (float)(1 << 24);
    public static final int   FR24_MASK = 0xFFFFF;

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

    public static int range(int f1, int f2){
        if(f1>0){
            if(f2>0) return f1>f2 ? -(f1-f2) : f2-f1;
            else return f2-f1;
        } else {
            if(f2>0) return f2-f1;
            else return f1<f2 ? -(f1-f2) : f2-f1;
        }
    }

    public static double range(double f1, double f2){
        if(f1>0){
            if(f2>0) return f1>f2 ? -(f1-f2) : f2-f1;
            else return f2-f1;
        } else {
            if(f2>0) return f2-f1;
            else return f1<f2 ? -(f1-f2) : f2-f1;
        }
    }


    public static double squareDistanceBetween(
            double x1, double y1, double z1,
            double x2, double y2, double z2
    ) {
        x2 -= x1;
        y2 -= y1;
        z2 -= z1;
        return x2 * x2 + y2 * y2 + z2 * z2;
    }
}