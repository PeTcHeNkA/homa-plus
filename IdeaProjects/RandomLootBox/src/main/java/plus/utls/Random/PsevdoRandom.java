package plus.utls.Random;

import java.util.Random;

public class PsevdoRandom extends Random {

    private int seed = 1;
    @Override
    public int nextInt(int maxIndex){
        seed = (seed * 111119 + 31);
        if(seed<0)seed = -seed;
        return seed % maxIndex;
    }

}
