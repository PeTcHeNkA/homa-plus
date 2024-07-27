
import plus.tson.TsonFloat;

import java.util.Arrays;

import static plus.space.client.gui.MathHelper.calc;
import static plus.space.client.gui.MathHelper.print;


public class Test {


    public static void main(String... args) throws InterruptedException {
        for (int i=-1;i<20;i++){
            System.out.println(calcWorldDay(i));
        }
    }

    private static int[] getDelta(int num){
        return new int[]{(num>>3)<<4, (num & 7)<<4};
    }



    private static int calcWorldDay(int range){
        return (int) ((0.3f + Math.pow(range/4f+0.18f, 5)) * 24000f);
    }


    public static double wrapDegrees360(double f){
        double d = f;
        if(f>360) f = f%360f;
        f = f<0?360 + f%360f:f;
        //System.out.println(d+"->"+f);
        return f;
    }
}