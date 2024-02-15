package plus.fserver.utls.math;

import net.minecraft.util.math.BlockPos;

import java.util.Random;


public class FastRandom extends Random {
    public static final FastRandom INST = new FastRandom();

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
    //TODO

//    int size = list.size();
//        if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
//        for (int i=size; i>1; i--)
//            swap(list, i-1, rnd.nextInt(i));
//    } else {
//        Object[] arr = list.toArray();
//
//        // Shuffle array
//        for (int i=size; i>1; i--)
//            swap(arr, i-1, rnd.nextInt(i));
//
//        // Dump array back into list
//        // instead of using a raw type here, it's possible to capture
//        // the wildcard but it will require a call to a supplementary
//        // private method
//        ListIterator it = list.listIterator();
//        for (int i=0; i<arr.length; i++) {
//            it.next();
//            it.set(arr[i]);
//        }
//    }
    public void shuffle(Object[] array){
        if(array.length==0)return;
        int i=0, e;
        Object swap;
        do{
            swap = array[e=nextInt(array.length)];
            array[e] = array[i];
            array[i] = swap;
            i+=2;
        } while (array.length>i);
        if(array.length!=i)return;
        swap = array[i = array.length-1];
        array[i] = array[e=nextInt(array.length)];
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