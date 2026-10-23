package plus.gen.utl;

import java.util.Random;


public class LazyRandom extends Random{
    private final float[] cache = new float[10000];
    private int i;
    public LazyRandom(long seed){
        super(seed);
        for(int i=0;i<10000;i++){
            cache[i] = super.nextFloat();
        }
    }

    @Override
    public float nextFloat() {
        if(i>=9999){
            i = 0;
            return cache[0];
        } else {
            return cache[++i];
        }
    }
}