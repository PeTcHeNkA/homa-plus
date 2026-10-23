package plus.util;

import java.util.ArrayList;
import java.util.List;

public class CordMath {
    public static double[][] getCircleWay(double x0, double z0, double yConst, double radius, int count){
        if(radius<0)throw new NullPointerException();
        try {
            double degrees = 360.0 / count;
            double nowDeg = degrees;
            double[][] cordsNew = new double[count][3];

            double[] prew = new double[]{x0 + radius, z0, yConst};

            cordsNew[0] = prew;

            for (int i = 1; i < count; i++) {
                double angle = Math.toRadians(nowDeg);
                double[] nowc = new double[]{
                        x0 + radius * Math.cos(angle),
                        z0 + radius * Math.sin(angle)
                };
                cordsNew[i] = new double[]{
                        -reDist(prew[0], nowc[0]),
                        -reDist(prew[1], nowc[1]),
                        yConst
                };
                nowDeg += degrees;
                prew = nowc;
            }

            return cordsNew;
        } catch (Exception err){
            System.out.println(String.format("Error! number of points is too small! (%s), using (%s)", count, count+1));
            return getCircleWay(x0, z0, yConst, radius, count+1);
        }
    }

    private static double reDist(double n, double o){
        double result = n-o;
        if(result>8 || result < -8)throw new NullPointerException();
        return result;
    }

    public static double[][] getCirclePoints(double x0, double z0, double yConst, double radius, int count){
        double degrees = 360.0 / count;
        double nowDeg = degrees;
        double[][] cordsNew = new double[count][3];

        cordsNew[0] = new double[]{x0 + radius, z0};

        for (int i = 1; i < count; i++) {
            double angle = Math.toRadians(nowDeg);
            cordsNew[i] = new double[]{
                    x0 + radius * Math.cos(angle),
                    z0 + radius * Math.sin(angle),
                    yConst
            };
            nowDeg += degrees;
        }
        return cordsNew;
    }


    public static double[] parseCords(String data){
        if(data==null)return null;
        String[] h = data.split(";");
        return new double[]{
                Double.parseDouble(h[0]),
                Double.parseDouble(h[1]),
                Double.parseDouble(h[2])
        };
    }


    public static double[] parseCords(String data, double... basic){
        if(data==null)return null;
        String[] h = data.split(";");
        return new double[]{
                Double.parseDouble(h[0])+basic[0],
                Double.parseDouble(h[1])+basic[1],
                Double.parseDouble(h[2])+basic[2]
        };
    }


    public static double[] Step(double[] value, double[] step){
        return new double[]{
                value[0]+step[0],
                value[1]+step[1],
                value[2]+step[2]
        };
    }
    public static double[] getStep(double[] v0, double[] v1, int count){
        return new double[]{
                getStep(v0[0], v1[0], count),
                getStep(v0[1], v1[1], count),
                getStep(v0[2], v1[2], count)
        };
    }
    private static double getStep(double v0, double v1, int count){
        //System.out.println(String.format("| %s, %s [%s]", v0, v1, count));
        double result = -(v0-v1)/count;
        if(result>8||result<-8){
            System.out.println(String.format("Error -> %s, %s [%s]" , v0, v1, count));
            throw new NullPointerException();
        }
        return result;
    }

    public static List<double[]> smoothMotions(List<double[]> value, int coef){
        List<double[]> result = new ArrayList<>();
        result.add(new double[]{0,0,0});
        for(int i =1;i<value.size();i++){
            double[] prew = value.get(i-1);
            double[] now = value.get(i);

            double[] step = new double[]{
                    -(prew[0]-now[0])/coef,
                    -(prew[1]-now[1])/coef,
                    -(prew[2]-now[2])/coef
            };
            for(int ii=0;ii<coef;ii++){
                result.add(step);
            }
        }
        result.remove(0);
        return result;
    }

    public static List<double[]> smooth(List<double[]> value, int coef){
        List<double[]> result = new ArrayList<>();
        result.add(value.get(0));
        for(int i =1;i<value.size();i++){
            double[] prew = value.get(i-1);
            double[] now = value.get(i);

            double[] step = new double[]{
                    -(prew[0]-now[0])/coef,
                    -(prew[1]-now[1])/coef,
                    -(prew[2]-now[2])/coef
            };

            double[] hs = prew;
            for(int ii=0;ii<coef;ii++){
                hs = CordMath.Step(hs, step);
                result.add(hs);
            }
        }
        result.set(result.size()-1, value.get(value.size()-1));

        return result;
    }

    public static List<byte[]> parseCamera(List<double[]> way, double[] startPos, double[] targt, boolean invert){
        if(invert){
            List<byte[]> cameraYawPith = new ArrayList<>();
            cameraYawPith.add(
                    invertLook(
                            startPos[0],startPos[1],startPos[2],
                            targt[0], targt[1], targt[2]
                    )
            );

            double[] value = startPos.clone();
            for(double[] value1:way){
                value = Step(value, value1);
                cameraYawPith.add(
                        invertLook(
                                value[0], value[1], value[2],
                                targt[0], targt[1], targt[2]
                        )
                );
            }
            return cameraYawPith;
        } else return parseCamera(way, startPos, targt);
    }

    public static List<byte[]> parseCamera(List<double[]> way, double[] startPos, double[] targt){
        List<byte[]> cameraYawPith = new ArrayList<>();
        cameraYawPith.add(
                look(
                        startPos[0],startPos[1],startPos[2],
                        targt[0], targt[1], targt[2]
                )
        );

        double[] value = startPos.clone();
        for(double[] value1:way){
            value = Step(value, value1);
            cameraYawPith.add(
                    look(
                            value[0], value[1], value[2],
                            targt[0], targt[1], targt[2]
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


}
