package col;

import net.minecraft.util.math.BlockPos;
import plus.fserver.utls.debug.HashCollisionChecker;


public class BPCollisions {
    public static void main(String[] args) {
        int count = 50;
        doTest(count, new Algo() {
            @Override
            public int hash(BlockPos obj) {
                return obj.getY()<<4 | obj.getX() | obj.getZ()<<16;
                //return obj.getY()*31 + (obj.getX()<<16 | obj.getZ());
            }
        });
        doTest(count, new Algo() {
            @Override
            public int hash(BlockPos obj) {
                return obj.hashCode();
            }
        });
    }

    interface Algo{
        int hash(BlockPos obj);
    }

    private static void doTest(int range, Algo algo){
        doTest(range, range, algo);
    }
    private static void doTest(int rangeX, int rangeZ, Algo algo){
        HashCollisionChecker checker = new HashCollisionChecker();
        for (int x=-rangeX;x<rangeX;x++){
            for (int z=-rangeZ;z<rangeZ;z++){
                for (int y=0;y<255;y++){
                    checker.feed(algo.hash(new BlockPos(x, y, z)));
                }
            }
        }
        System.out.println(checker);
    }


    //31 * obj.getY() + (obj.getX()<<16 | obj.getZ());
}
