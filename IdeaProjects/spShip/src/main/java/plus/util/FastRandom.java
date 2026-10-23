package plus.util;

import java.util.Random;


public class FastRandom extends Random {
    private int seed = 1;
    private short count = 0;
    public static FastRandom r = new FastRandom();


    @Override
    public int nextInt(int maxIndex){
        seed = (seed * (11119+count++) + 31);
        if(seed<0)seed = -seed;
        return seed % (maxIndex);
    }


    public int nextInt(int minIndex, int maxIndex){
        seed = (seed * (11119+count++) + 31);
        if(seed<0)seed = -seed;
        return seed%(maxIndex-minIndex+1)+minIndex;
    }


    public char nextChar(){
        count++;
        seed = (seed * 1119+count + 31);
        if(seed<1000)seed=(1000+count);
        return (char) seed;
    }
}
