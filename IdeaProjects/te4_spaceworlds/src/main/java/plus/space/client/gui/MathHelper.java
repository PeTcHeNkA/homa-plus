package plus.space.client.gui;

import static java.lang.Math.cos;
import static java.lang.Math.sin;


public class MathHelper {
    public static void print(double[]d){
        System.out.printf("X:%s Y:%s Z:%s\n", round(d[0]), round(d[1]), round(d[2]));
    }


    private static double round(double d){
        return ((int)(d*100))/100d;
    }


    public static double[] calc(double[] p1, double[] cen, double rX, double rY, double rZ){
        return calc(p1[0], p1[1], p1[2], cen[0], cen[1], cen[2], rX, rY, rZ);
    }


    public static double[] calc(double x, double y, double z, double a, double b, double c, double aX, double aY, double aZ){
        double xs, ys, zs, theta;
        if(aX != 0) {
            theta = Math.toRadians(aX);

            xs = a + (x - a);
            ys = b + (y - b) * cos(theta) - (z - c) * sin(theta);
            zs = c + (y - b) * sin(theta) + (z - c) * cos(theta);

            x = xs;
            y = ys;
            z = zs;
        }
        if(aY != 0) {
            theta = Math.toRadians(aY);

            xs = a + (x - a) * cos(theta) + (z - c) * sin(theta);
            ys = b + (y - b);
            zs = c - (x - a) * sin(theta) + (z - c) * cos(theta);

            x = xs;
            y = ys;
            z = zs;
        }
        if(aZ != 0) {
            theta = Math.toRadians(aZ);

            xs = a + (x - a) * cos(theta) - (y - b) * sin(theta);
            ys = b + (x - a) * sin(theta) + (y - b) * cos(theta);
            zs = c + (z - c);

            x = xs;
            y = ys;
            z = zs;
        }
        return new double[]{x, y, z};
    }
}
