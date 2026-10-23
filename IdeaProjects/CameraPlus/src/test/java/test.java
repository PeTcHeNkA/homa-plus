import plus.util.CordMath;
import java.util.ArrayList;
import java.util.List;


public class test {

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


    public static List<byte[]> parseCamera(List<double[]> way, double[] targt, boolean invert){
        if(invert){
            List<byte[]> cameraYawPith = new ArrayList<>();

            double[] value = new double[]{0,0,0};
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
        } else return parseCamera(way, targt);
    }


    public static double[] Step(double[] value, double[] step){
        return new double[]{
                value[0]+step[0],
                value[1]+step[1],
                value[2]+step[2]
        };
    }


    public static List<byte[]> parseCamera(List<double[]> way, double[] targt){
        List<byte[]> cameraYawPith = new ArrayList<>();

        double[] value = new double[]{0,0,0};
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


    public static void main(String[] args){
        List<String> l = new ArrayList<>();
        l.add("0;0;0");
        l.add("10;10;0");
        l.add("10;20;0");
        l.add("10;20;10");

        fm a = new p().parse1(l, new double[]{0, 0, 0}, 3, false).toFm();

        //System.out.println(a.c.length);
        //System.out.println(a.c[0].motion.length);
        //System.out.println(a.c[0].camera.length);
        for(int i=0;i<9;i++){
            System.out.println(a.c[i].motion[0]);
        }

        System.out.println(((double)Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(8*1024*1024));


    }
}

class p{

    public mc parse1(List<String> data, double[] lookTarget, int time, boolean invertLook) {
        mc c = new mc();

        List<double[]> way = new ArrayList<>();
        int index = 0;
        String[] next = getNextPart(data, index);
        do{
            double[] step = CordMath.getStep(
                    CordMath.parseCords(next[0]),
                    CordMath.parseCords(next[1]), time
            );
            for(int i=0;i<time;i++){
                way.add(step);
            }

            index+=1;
            next = getNextPart(data, index);
        } while (next != null);

        for(double[] value:way){
            c.Motions.add(
                    new short[]{
                            (short) (value[0]*4096),
                            (short) (value[1]*4096),
                            (short) (value[2]*4096)
                    }
            );
        }
        c.Cameras.addAll(test.parseCamera(way, lookTarget, invertLook));
        return c;
    }

    private String[] getNextPart(List<String> data, int index){
        try {
            return new String[]{
                    data.get(index),
                    data.get(index + 1)
            };
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }
}
class mc{
    public final List<short[]> Motions = new ArrayList<>();
    public final List<byte[]> Cameras = new ArrayList<>();
    public fm toFm(){
        return new fm(this);
    }
}


class ct{
    public final short[] motion;
    public final byte[] camera;

    ct(short[] motion, byte[] camera) {
        this.motion = motion;
        this.camera = camera;
    }
}


class fm{
    public final ct[] c;

    public fm(mc mc) {
        int size = mc.Motions.size();
        c = new ct[size];
        for(int i = 0;i<size;i++){
            c[i] = new ct(
                    mc.Motions.get(i),
                    mc.Cameras.get(i)
            );
        }
    }
}