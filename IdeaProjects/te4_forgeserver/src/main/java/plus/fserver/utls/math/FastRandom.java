package plus.fserver.utls.math;

import net.minecraft.util.math.BlockPos;

import java.util.Random;


public class FastRandom extends Random {
    public static final FastRandom INST = new FastRandom();

    public FastRandom(){}


    public FastRandom(int i) {
        super(i);
    }


    public static int range(int f1, int f2){
        if(f1>0){
            if(f2>0) return f1>f2 ? -(f1-f2) : f2-f1;
            else return f2-f1;
        } else {
            if(f2>0) return f2-f1;
            else return f1<f2 ? -(f1-f2) : f2-f1;
        }
    }


    public static double range(double f1, double f2){
        if(f1>0){
            if(f2>0) return f1>f2 ? -(f1-f2) : f2-f1;
            else return f2-f1;
        } else {
            if(f2>0) return f2-f1;
            else return f1<f2 ? -(f1-f2) : f2-f1;
        }
    }


    public void shuffle(Object[] array){
        shuffle(array, array.length);
    }


    public void shuffle(Object[] array, int to){
        if(to==0)return;
        int i=0, e;
        Object swap;
        do{
            swap = array[e=nextInt(to)];
            array[e] = array[i];
            array[i] = swap;
            i+=2;
        } while (to>i);
        if(to!=i)return;
        swap = array[i = to-1];
        array[i] = array[e=nextInt(to)];
        array[e] = swap;
    }


    public void setRandom8x8x4(BlockPos.MutableBlockPos pos, BlockPos parent){
        int num = nextInt();
        pos.setPos(
                parent.getX() + (num & 7) - ((num >> 4) & 7),
                parent.getY() + ((num >> 8) & 3) - ((num >> 10) & 3),
                parent.getZ() + ((num >> 12) & 7) - ((num >> 16) & 7)
        );
    }
}