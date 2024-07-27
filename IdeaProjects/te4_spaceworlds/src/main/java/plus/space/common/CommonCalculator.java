package plus.space.common;


public class CommonCalculator {
    private static final int BASE = 1280;
    public static int blocksOf(float scale){
        return (int) (BASE*scale*scale);
    }

    public static int calcWorldDay(int range){
        return (int) ((0.3f + Math.pow(range/4f+0.18f, 5)) * 24000f);
    }
}