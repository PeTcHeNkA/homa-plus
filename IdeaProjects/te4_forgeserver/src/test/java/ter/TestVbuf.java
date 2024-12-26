package ter;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import plus.fserver.utls.math.FastRandom;
import plus.fserver.world.save.VarIntBuf;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;


public class TestVbuf {
    public static void main(String[] args) {
//        VarIntBuf buf = new VarIntBuf();
//
//        int start = 70_000;
//
//        for (int i = start, end = start + 50; i < end; i++){
//            buf.add(i);
//            //if(!check(buf, i)){
////                System.out.println(i);
////                return;
////            }
//        }
//        ecoTest();
//        test2();
    }


    public static void ecoTest(){
        VarIntBuf buf = new VarIntBuf();
        for (int i = 0; i < 2000; i+=5){
            buf.add(i);
        }
        System.out.println(buf);
    }


    public static boolean check(VarIntBuf buf, int check){
        buf.add(check);
        int res = buf.remMax();
        buf.add(res);
        //System.out.println(check+" -> "+res);
        return res == check;
    }


    public static void test2(){
        Random rand = new Random(2);
        VarIntBuf buf = new VarIntBuf();

        int[] nums = new int[30];
        for (int i = 0; i < 10; i++){
            nums[i] = rand.nextInt(127);
        }

        for (int i = 10; i < 15; i++){
            nums[i] = 300+rand.nextInt(500);
        }

        for (int i = 15; i < 30; i++){
            nums[i] = (1<<16) + rand.nextInt(1<<15);
        }

        IntArrayList list = new IntArrayList(nums);
        Collections.shuffle(list, rand);
        nums = list.elements();

        for (int i = 0; i < nums.length; i++){
            buf.add(nums[i]);
        }

        int[] nNUms = new int[nums.length];
        int cursor = 0;

        System.out.println(buf);

        int pre = buf.remMax();
        while (pre != -1){
            nNUms[cursor++] = pre;
            pre = buf.remMax();
        }

        Arrays.sort(nums);
        Arrays.sort(nNUms);

        System.out.println(Arrays.toString(nums));
        System.out.println(Arrays.toString(nNUms));
    }
}
