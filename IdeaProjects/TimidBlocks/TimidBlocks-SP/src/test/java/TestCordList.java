import plus.timidblocks.UserSoundManager;

import java.util.Collection;
import java.util.Random;


public class TestCordList {
    public static void main(String[] args) {
        //CordList cordList = new CordList();

        clearBlocksInChunkSub(66, 107, 20);

        //66 107 20
    }


    protected static void clearBlocksInRange(int CH_X, int CH_Z, int minX, int minZ, int maxX, int maxZ){
        int lim = 100; //todo maybe faster?
        //int lim = calcMaxH(chunk, snap, blocks);


        for (int x = 0; x < 16; x++){
            if(!isBetween(minX, maxX, (CH_X * 16) + x))continue;

            for (int z = 0; z < 16; z++){
                if(!isBetween(minZ, maxZ, (CH_Z * 16) + z))continue;

                System.out.printf("X: %s Z: %s \n", ((CH_X * 16) + x), ((CH_Z * 16) + z));
            }
        }
    }

    public static boolean isBetween(int min, int max, int val){
        return val >= min && val <= max;
    }


    public static void clearBlocksInChunkSub(int xa, int ya, int za){
        Random rand = new Random(1);
        CordList list = new CordList();

        for (int x = 0; x < 2; x++){
            for (int z = 0; z < 2; z++){
                for (int y = -10, lim = 10; y < lim; y+=5){
                    //if(rand.nextInt(8)>2){
                        list.add(x, y, z);
                    //}
                }
            }
        }

        list.forEach(new CordFunction() {
            @Override
            public void apply(int x, int y, int z) {
                System.out.println(x + " " + y + " " + z);
            }
        });
    }


    private static class CordList extends CordNode {
        CordNode current = this;

        void add(int x, int y, int z){
            int num = ((x & 15) << 24) | ((z & 15) << 16) | (((y & 0xFFFF) + 0x7FFF) & 0xFFFF);

            if(current.add0(num)){
                CordNode next0 = new CordNode();
                current.next = next0;
                current = next0;
            }
        }


        void forEach(CordFunction func){
            CordNode cur = this;
            while(cur != null){
                int[] data = cur.data;
                for(int i = 0, c = cur.cursor; i < c; i++){
                    int num = data[i];

                    func.apply((num >> 24) & 15, (num >> 16) & 15, (num & 0xFFFF) - 0x7FFF);
                }

                cur = cur.next;
            }
        }
    }


    private static class CordNode{
        int[] data = new int[64];
        int cursor;
        CordNode next;

        boolean add0(int num){
            data[cursor++] = num;
            return cursor == 64;
        }
    }


    @FunctionalInterface
    interface CordFunction {
        void apply(int x, int y, int z);
    }
}