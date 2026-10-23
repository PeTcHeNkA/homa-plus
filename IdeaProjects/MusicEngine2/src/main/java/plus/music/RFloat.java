package plus.music;

import java.util.Random;


public interface RFloat {

    static RFloat build(String rawData){
        float Min;
        if(rawData!=null) {
            if (rawData.contains("-")) {
                double Max;
                String[] cs = rawData.split("-");
                return new RandomPith(
                        cs[0].equals("*") ? 0.0f : Float.parseFloat(cs[0]),
                        cs[1].equals("*") ? 2.0f : Float.parseFloat(cs[1])
                );
            } else {
                Min = Float.parseFloat(rawData);
            }
        } else {
            Min = 1.0f;
        }
        return () -> Min;
    }


    float Float();
}

class RandomPith implements RFloat {
    private static final Random r = new Random();
    private final float shift;
    private final float lim;

    public RandomPith(float min, float max){
        shift = min;
        lim = max>min?max-min:min-max;
    }


    @Override
    public float Float() {
        return shift+((float) r.nextDouble())*lim;
    }
}