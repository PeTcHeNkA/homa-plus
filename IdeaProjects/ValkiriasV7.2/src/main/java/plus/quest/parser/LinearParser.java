package plus.quest.parser;

import plus.quest.motion.BasicPos;
import plus.quest.motion.Motion;
import java.util.ArrayList;
import java.util.List;


public class LinearParser implements Parser{

    private static LinearParser I;

    public static LinearParser getInstance(){
        if(I==null)I = new LinearParser();
        return I;
    }


    @Override
    public Motion[] parse(List<String> data, BasicPos lookTarget, int time, boolean invertLook) {
        RawMotions c = new RawMotions();

        List<double[]> way = new ArrayList<>();
        int index = 0;
        String[] next = getNextPart(data, index);
        do{
            double[] step = CordMath.getStep(
                    new BasicPos(next[0]),
                    new BasicPos(next[1]), time
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
        c.Cameras.addAll(CordMath.parseCamera(way, lookTarget, invertLook));
        return c.toMotions();
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


class RawMotions{
    public final List<short[]> Motions = new ArrayList<>();
    public final List<byte[]> Cameras = new ArrayList<>();

    public Motion[] toMotions(){
        int size = Motions.size();
        Motion[] motions = new Motion[size];
        for(int i = 0;i<size;i++){
            motions[i] = new Motion(
                    Motions.get(i),
                    Cameras.get(i)
            );
        }
        return motions;
    }
}