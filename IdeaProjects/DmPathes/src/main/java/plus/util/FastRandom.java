package plus.util;

import java.util.Random;

public class FastRandom extends Random {
    private int seed = 1;
    private short count = 0;
    public static FastRandom r = new FastRandom();

    @Override
    public int nextInt(int maxIndex){
        count++;
        seed = (seed * (11119+count) + 31);
        if(seed<0)seed = -seed;
        return seed % maxIndex;
    }
    public char nextChar(){
        count++;
        seed = (seed * 1119+count + 31);
        return (char) seed;
    }
}
