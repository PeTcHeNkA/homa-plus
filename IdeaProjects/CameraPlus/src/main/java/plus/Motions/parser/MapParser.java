package plus.Motions.parser;

import plus.util.CordMath;

import java.util.ArrayList;
import java.util.List;

public class MapParser extends FrameParser{

    public MapParser(){}

    public MapParser(List<String> data, double[] startPos, double[] lookTarget, int time){
        super(data, startPos, lookTarget, time);
    }

    @Override
    public MotionContainer parse(List<String> data, double[] startPos, double[] lookTarget, int smooth) {
        String[] settings = data.remove(0).split(";");//8;

        List<double[]> trajectory = getTrajectory(
                startPos[0], startPos[1], startPos[2],
                Integer.parseInt(settings[0]), data
        );
        trajectory = CordMath.smoothMotions(trajectory, smooth);

        MotionContainer container = new MotionContainer();
        for(double[] value:trajectory){
            container.Motions.add(
                    new short[]{
                            (short) (value[0]*4096),
                            (short) (value[1]*4096),
                            (short) (value[2]*4096)
                    }
            );
        }

        double[] position = new double[]{0.0f,0.0f,0.0f};

        List<double[]> cords = new ArrayList<>();
        for(double[] step:trajectory){
            position = CordMath.Step(position, step);
            cords.add(position);
        }
        container.Cameras = CordMath.parseCamera(cords, startPos, lookTarget);
        return container;
    }

    public List<double[]> getTrajectory(double x0, double y0, double z0, double step, List<String> t){

        if(step>8)throw new NullPointerException("Error! Step > 8!");
        if(t.size()==0)throw new NullPointerException();

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

        List<double[]> trajectory = new ArrayList<>();
        //trajectory.add(new double[]{x0, y0, z0});
        for(int i = 1;i<c2.size();i++){
            trajectory.add(new double[]{c2.get(i)[0] * step, y0, c2.get(i)[1] * step});
        }

        return trajectory;
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
