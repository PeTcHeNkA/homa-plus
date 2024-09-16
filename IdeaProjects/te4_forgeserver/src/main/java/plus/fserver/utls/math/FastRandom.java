package plus.fserver.utls.math;

import net.minecraft.util.math.BlockPos;

import java.util.Random;


public class FastRandom extends Random {
    public static final FastRandom INST = new FastRandom();

    public FastRandom(){}


    public FastRandom(int i) {
        super(i);
    }


    public void shuffle(Object[] array){
        shuffle(array, array.length);
    }


    public void shuffle(Object[] array, int to){
        if(to == 0)return;
        int i = 0, e;
        Object swap;
        int seed = nextInt();
        do{
            swap = array[e = (seed & 0x7FFFFFFF) % to];
            array[e] = array[i];
            array[i] = swap;
            i+=2;
            seed = seed * 363 + 21 + i;
        } while (to>i);
        if(to!=i)return;
        swap = array[i = to-1];
        array[i] = array[e = (seed & 0x7FFFFFFF) % to];
        array[e] = swap;
    }


    public void setRandom8x8x4(BlockPos.MutableBlockPos pos, BlockPos parent){
        setRandom8x8x4(this, pos, parent);
    }


    public static void setRandom8x8x4(Random rand, BlockPos.MutableBlockPos pos, BlockPos parent){
        int num = rand.nextInt();
        pos.setPos(
                parent.getX() + (num & 7) - ((num >> 4) & 7),
                parent.getY() + ((num >> 8) & 3) - ((num >> 10) & 3),
                parent.getZ() + ((num >> 12) & 7) - ((num >> 16) & 7)
        );
    }
}