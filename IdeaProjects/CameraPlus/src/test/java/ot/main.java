package ot;

import plus.util.CordMath;

import java.util.ArrayList;
import java.util.List;




public class main {
    public static void main(String[] args){
        try {
            main();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static List<String> tr = new ArrayList<>();
    public static void main(){
        tr.add("__XXXX__");
        tr.add("_X____X_");
        tr.add("__X___X_");
        tr.add("__X__X__");
        tr.add("_S___X__");
        tr.add("____X___");
        tr.add("___X____");
        tr.add("____XXXE");

        //String cs = getTraectoryCashe(tr);
        //System.out.println(cs);
//        double[][] dd = getTraectory(10,0,0,4, tr);
//        for(double[]d:dd){
//            System.out.println(String.format("| %s, %s, %s", d[0], d[1], d[2]));
//        }
        List<double[]> db = new ArrayList<>();
        db.add(new double[]{10, 10, 10});
        db.add(new double[]{20, 20, 20});
        db.add(new double[]{0, 10, 0});

        List<double[]> dd = smoothMotions(db, 3);
        for(double[]d:dd){
            System.out.println(String.format("| %s, %s, %s", d[0], d[1], d[2]));
        }
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




    public static int checkBoost(double x1, double z1, double x2, double z2){
        int b;
        if(x2>x1){
            if(z2>z1){
                b = 0;
            } else {
                b = 270;
            }
        } else {
            if(z2>z1){
                b = 180;
            } else {
                b = 90;
            }
        }


        return b;
    }


    public static double[] getLineStep(double x0, double y0, double z0, double x1, double y1, double z1){
        return getLineStep(x0, y0, z0, x1, y1, z1, 2);
    }

    private static double[] getLineStep(double x0, double y0, double z0, double x1, double y1, double z1, int count){
        try {
            double xStep = getStep(x0, x1, count);
            double yStep = getStep(y0, y1, count);
            double zStep = getStep(z0, z1, count);
            return new double[]{xStep, yStep, zStep};
        } catch (Exception e){
            return getLineStep(x0, y0, z0, x1, y1, z1, count+1);
        }
    }
    private static double getStep(double v0, double v1, int count){
        double result = -(v0-v1)/count;
        if(result>8||result<-8)throw new NullPointerException();
        return result;

    }









    public static double[][] getTraectory(double x0, double y0, double z0, double step, String cashe){
        String[] s = cashe.split("-");

        double[][] map = new double[s.length][3];

        map[0] = new double[]{x0, y0, z0};
        for(int i = 1;i<s.length;i++){
            byte[] c2 = s[i].getBytes();
            map[i]= new double[]{c2[0] * step, y0, c2[1] * step};
        }
        return map;
    }

    public static String getTraectoryCashe(List<String> t){
        if(t.size()==0)throw new NullPointerException();
        if(t.size() != t.get(0).length()){
            System.out.println("Error! Len vertical != len horizontal!");
            throw new NullPointerException();
        }
        if(t.size()>127){
            System.out.println("Error! Max size is 127!");
            throw new NullPointerException();
        }

        List<byte[]> cds = getWayCords(t);
        List<byte[]> c2 = new ArrayList<>();
        c2.add(cds.get(0));
        for(int i = 1;i<cds.size();i++){
            byte[] prew = cds.get(i-1);
            byte[] now = cds.get(i);
            c2.add(
                    new byte[]{
                            (byte) -(prew[0]-now[0]),
                            (byte) -(prew[1]-now[1])
                    }
            );
        }
        List<String> ch = new ArrayList<>();
        for(byte[] b:c2){
            ch.add(new String(b));
        }
        return String.join("-", ch);
    }

    public static double[][] getTraectory(double x0, double y0, double z0, double step, List<String> t){
        if(step>8)throw new NullPointerException("Error! Step > 8!");
        if(t.size()==0)throw new NullPointerException();
        //if(t.size() != t.get(0).length()){
        //    System.out.println("Error! Len vertical != len horizontal!");
        //    throw new NullPointerException();
        //}
        if(t.size()>127){
            System.out.println("Error! Max size is 127!");
            throw new NullPointerException();
        }

        List<byte[]> cds = getWayCords(t);
        List<byte[]> c2 = new ArrayList<>();
        c2.add(cds.get(0));
        for(int i = 1;i<cds.size();i++){
            byte[] prew = cds.get(i-1);
            byte[] now = cds.get(i);
            c2.add(
                    new byte[]{
                            (byte) -(prew[0]-now[0]),
                            (byte) -(prew[1]-now[1])
                    }
            );
        }
        double[][] map = new double[c2.size()][3];

        map[0] = new double[]{x0, y0, z0};
        for(int i = 1;i<c2.size();i++){
            map[i]= new double[]{c2.get(i)[0] * step, y0, c2.get(i)[1] * step};
        }
        return map;
    }

    private static List<byte[]> getWayCords(List<String> map){
        List<byte[]> way = new ArrayList<>();
        way.add(getPosOf(map, 'S'));

        byte[] next = getNext(map, way.get(0), getPosOf(map, 'E'));

        do{
            way.add(next);
            next = getNext(map, way.get(way.size()-1), way.get(way.size()-2));
        } while (next != null);
        return way;
    }

    private static byte[] getPosOf(List<String> map, char c){//only start and end
        byte[] startPos = new byte[]{-1, -1};
        for (byte i = 0; i < map.size(); i++) {
            String s = map.get(i);
            byte index = (byte) s.indexOf(c);
            if(index>-1){
                startPos = new byte[]{i, index};
                break;
            }
        }
        if(startPos[0]== -1){
            throw new NullPointerException("Error! position not founded.");
        } else return startPos;
    }
    private static byte[] getNext(List<String> t, final byte[] startPos, byte [] prewPos){

        if(prewPos==null)throw new NullPointerException("Error! prewPos is not exist!");
        int[][] forCheck = new int[][]{
                new int[]{startPos[0]+1, startPos[1]+1},
                new int[]{startPos[0]+1, startPos[1]  },
                new int[]{startPos[0]+1, startPos[1]-1},
                new int[]{startPos[0]  , startPos[1]+1},
                new int[]{startPos[0]  , startPos[1]-1},
                new int[]{startPos[0]-1, startPos[1]-1},
                new int[]{startPos[0]-1, startPos[1]  },
                new int[]{startPos[0]-1, startPos[1]+1}
        };
        byte[] nextPos = null;

        for(int[] marker:forCheck){
            int indexX = marker[0];
            int indexY = marker[1];
            if(indexX != -1 && indexY != -1 && indexX < t.size() && indexY < t.size()) {
                char sel = t.get(indexX).toCharArray()[indexY];

                if (sel != '_') {
                    if ((marker[0] != prewPos[0] || marker[1] != prewPos[1]))
                        if (nextPos == null) {
                            if(sel != 'S')
                            nextPos = new byte[]{(byte) marker[0], (byte) marker[1]};
                        }
                    else if(nextPos != null)throw new NullPointerException("Error! path must be unambiguous!");
                }
            }
        }
        return nextPos;
    }
}
