package ot;

import java.util.ArrayList;
import java.util.List;

public class m2 {

    private static List<short[]> motions = new ArrayList<>();
    private static List<byte[]> cameraYawPith = new ArrayList<>();
    private static double[] startPos;

    public static void main(String[] args){
        try {
            main();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(){
        List<String> i = new ArrayList<>();
        //        - 90;70;100
        //        - 100;70;110
        //        - 105;75;100
        //        - 105;70;90
        i.add("90;70;100");
        i.add("100;70;110");
        i.add("105;75;100");
        i.add("105;70;90");

        double[] lt = new double[]{100, 70, 100};
        double[] sp = new double[]{90, 70, 100};

        startPos = sp;

        initLinear(i, lt, 4);

//        for(byte[] b:cameraYawPith){
//            System.out.println(String.format("| %s, %s", b[0], b[1]));
//        }
//        for(short[] b:motions){
//            System.out.println(String.format("| %s, %s, %s", b[0], b[1], b[2]));
//        }

        try {
            System.out.println(cameraYawPith.size());
            for (int i2 = 1; i2 < cameraYawPith.size(); i2++) {
                System.out.println(String.format("| Moved! %s", i2));
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    private static void initLinear(List<String> data, double[] lookTarget, int time){
        //"10.1;0;0"
        List<double[]> way = new ArrayList<>();
        int index = 0;
        String[] next = getNextPart(data, index);
        do{
            double[] step = getStep(
                    parseCords(next[0]),
                    parseCords(next[1]), time
            );
            for(int i=0;i<time;i++){
                way.add(step);
            }

            index+=1;
            next = getNextPart(data, index);
        } while (next != null);

        for(double[] value:way){
            motions.add(
                    new short[]{
                            (short) (value[0]*4096),
                            (short) (value[1]*4096),
                            (short) (value[2]*4096)
                    }
            );
        }
        parseCamera(way, lookTarget);
    }

    private static void parseCamera(List<double[]> way, double[] targt){
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
    }

    public static byte[] look(double x1, double y1, double z1, double x2, double y2, double z2){
        return new byte[]{
                (byte) ((Math.toDegrees(-Math.atan2(Math.abs(Math.abs(x1)-Math.abs(x2)), -(y1-y2)))+90 ) * 0.7111111f),
                (byte) ( Math.toDegrees(Math.atan2(  x1-x2, z2-z1  ))  * 0.7111111f)
        };
    }

    protected static double[] parseCords(String data){
        if(data==null)return null;
        String[] h = data.split(";");
        return new double[]{
                Double.parseDouble(h[0]),
                Double.parseDouble(h[1]),
                Double.parseDouble(h[2])
        };
    }
    private static String[] getNextPart(List<String> data, int index){
        try {
            return new String[]{
                    data.get(index),
                    data.get(index + 1)
            };
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    private static double[] Step(double[] value, double[] step){
        return new double[]{
                value[0]+step[0],
                value[1]+step[1],
                value[2]+step[2]
        };
    }

    private static double[] getStep(double[] v0, double[] v1, int count){
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






}
