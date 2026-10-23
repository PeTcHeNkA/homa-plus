package plus.Motions.parser;

import plus.util.CordMath;
import java.util.ArrayList;
import java.util.List;


public class LinearParser extends FrameParser{

    public LinearParser(){}

    public LinearParser(List<String> data, double[] startPos, double[] lookTarget, int time){
        super(data, startPos, lookTarget, time);
    }

    @Override
    public MotionContainer parse(List<String> data, double[] startPos, double[] lookTarget, int time) {
        startPos = CordMath.parseCords(data.get(0));

        MotionContainer c = new MotionContainer();

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
        c.Cameras = CordMath.parseCamera(way, startPos, lookTarget, invertLook);
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
