package plus.utls;

import net.minecraft.entity.Entity;
import plus.client.mem.Memory;
import java.util.Arrays;
import static java.lang.Math.*;


public class MathUtils {
    //public static final Random RANDOM = new Random();

    public static <T> T[] fillArray(T[] arr, T obj, int count){
        Arrays.fill(arr, obj);
        return arr;
    }


    public static boolean isNear(double d1, double d2){
        double range = MathUtils.range(d1, d2);
        return !(range>0.01 || range<-0.01);
    }


    public static boolean isNear(double d1, double d2, double p){
        double range = MathUtils.range(d1, d2);
        return !(range>p || range<-p);
    }


    public static float range(double f1, double f2){
        return range((float) f1, (float) f2);
    }
    public static float range(float f1, float f2){
        if(f1>0){
            if(f2>0){
                return f1>f2 ? -(f1-f2) : f2-f1;
            } else {
                return f2-f1;
            }
        } else {
            if(f2>0){
                return f2-f1;
            } else {
                return f1<f2 ? -(f1-f2) : f2-f1;
            }
        }
    }

    public static boolean isBetween(int val, int max, int min){
        return val<max && val>min || val>max && val<min;
    }

    public static boolean isBetween(float val, float max, float min){
        return val<max && val>min || val>max && val<min;
    }


    public static double calcXZrot(Entity player, float partialTicks, Memory mem){
        if(isNear(player.posX, player.prevPosX) && isNear(player.posZ,player.prevPosZ)){
            return (float) mem.prevXZrot;
        }
        mem.setPrevXZ(MathUtils.interpolateRotation(mem.prevXZrot, MathUtils.calcXZrot(player), partialTicks));
        return mem.prevXZrot;
    }



    public static double calcXZrot(Entity entity){
        return Math.toDegrees(
                Math.atan2(
                        MathUtils.range(entity.posX, entity.prevPosX),
                        MathUtils.range(entity.posZ, entity.prevPosZ)
                )
        );
    }


    public static double calcEntitySpeed(Entity e){
        return abs(
                rangeSqrt(e.posX, e.prevPosX) +
                rangeSqrt(e.posY, e.prevPosY) +
                rangeSqrt(e.posZ, e.prevPosZ)
        );
    }


    private static double rangeSqrt(double d1, double d2){
        if(d1>d2){
            return sqrt(d1-d2);
        } else {
            return sqrt(d2-d1);
        }
    }


    public static double interpolateRotation(double prev, double val, float partialTicks) {
        double f;
        for (f = val - prev; f < -180.0F; f += 360.0F) {}
        while (f >= 180.0F) {
            f -= 360.0F;
        }
        return prev + partialTicks * f;
    }


    public static float wrapDegrees180(float f){
        if(f>360){
            return f%180;
        }
        if(f<0){
            return 180 + f%180;
        }
        return f;
    }


    public static float wrapDegrees360(float f){
        if(f>360){
            return f%360;
        }
        if(f<0){
            return 360 + f%360;
        }
        return f;
    }


    public static float calcYrot(Entity player, float partialTicks, Memory mem){
        mem.setPrevY((float) MathUtils.interpolateRotation(mem.prevYrot, MathUtils.calcYrot(player), partialTicks));
        return mem.prevYrot;
    }


    public static double calcYrot(Entity entity){
        double delta = entity.posX - entity.prevPosX;
        double result = entity.posZ - entity.prevPosZ;
        result = delta*delta + result*result;
        delta = entity.prevPosY - entity.posY;
        if(result == 0){
            if(delta==0)return 0;
            result = 0.001;
        }

        result = result / sqrt(result * (delta * delta + result));

        if (result > 1.0) {
            return 0;
        } else if(result < -1.0){
            return 180;
        } else {
            result = Math.toDegrees(Math.acos(result));
            if(Double.isNaN(result))return 0;
            if (delta>0) {
                return -result;
            }
            return result;
        }
    }


    public static double calcBigStructCoef(int x){
        return 1 + 15 * (  2d - 4d/(Math.sqrt(x)/100d + 2d)  );
    }


    public static float calcPowerComponent(float src){
        return (float) (1-sqrt(2*src)/2f);
    }
}