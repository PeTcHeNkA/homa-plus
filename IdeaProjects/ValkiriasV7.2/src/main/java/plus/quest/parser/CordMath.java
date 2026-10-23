package plus.quest.parser;

import plus.quest.motion.BasicPos;
import java.util.ArrayList;
import java.util.List;


public class CordMath {

    public static byte[] invertLook(double x1, double y1, double z1, double x2, double y2, double z2){
        double dd1 = Math.abs(Math.abs(x1)-Math.abs(x2));
        dd1 = dd1*dd1;
        double dd2 = Math.abs(Math.abs(z1)-Math.abs(z2));
        dd2 = dd2*dd2;

        return new byte[]{
                (byte) ((Math.toDegrees(Math.atan2(  Math.sqrt(dd1+dd2), -(y1-y2)  ))-90) * 0.7111111f),
                (byte) -( Math.toDegrees(Math.atan2(  x1-x2, z2-z1  ))  * 0.7111111f)
        };
    }


    public static List<byte[]> parseCamera(List<double[]> way, BasicPos targt, boolean invert){
        if(invert){
            List<byte[]> cameraYawPith = new ArrayList<>();

            double[] value = new double[]{0,0,0};
            for(double[] value1:way){
                value = Step(value, value1);
                cameraYawPith.add(
                        invertLook(
                                value[0], value[1], value[2],
                                targt.X, targt.Y, targt.Z
                        )
                );
            }
            return cameraYawPith;
        } else return parseCamera(way, targt);
    }


    public static double[] Step(double[] value, double[] step){
        return new double[]{
                value[0]+step[0],
                value[1]+step[1],
                value[2]+step[2]
        };
    }


    public static List<byte[]> parseCamera(List<double[]> way, BasicPos targt){
        List<byte[]> cameraYawPith = new ArrayList<>();

        double[] value = new double[]{0,0,0};
        for(double[] value1:way){
            value = Step(value, value1);
            cameraYawPith.add(
                    look(
                            value[0], value[1], value[2],
                            targt.X, targt.Y, targt.Z
                    )
            );
        }
        return cameraYawPith;
    }


    public static byte[] look(double x1, double y1, double z1, double x2, double y2, double z2){
        double dd1 = Math.abs(Math.abs(x1)-Math.abs(x2));
        dd1 = dd1*dd1;
        double dd2 = Math.abs(Math.abs(z1)-Math.abs(z2));
        dd2 = dd2*dd2;

        return new byte[]{
                (byte) ((Math.toDegrees(Math.atan2(  Math.sqrt(dd1+dd2), -(y1-y2)  ))-90) * 0.7111111f),
                (byte) ( Math.toDegrees(Math.atan2(  x1-x2, z2-z1  ))  * 0.7111111f)
        };
    }


    public static double[] getStep(BasicPos v0, BasicPos v1, int count){
        return new double[]{
                getStep(v0.X, v1.X, count),
                getStep(v0.Y, v1.Y, count),
                getStep(v0.Z, v1.Z, count)
        };
    }


    private static double getStep(double v0, double v1, int count){
        double result = -(v0-v1)/count;
        if(result>8||result<-8){
            throw new IndexOutOfBoundsException(String.format("Error -> %s, %s [%s]" , v0, v1, count));
        }
        return result;
    }
}