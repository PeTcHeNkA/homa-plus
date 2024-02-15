package plus.fserver.utls.collections;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import net.minecraft.util.math.BlockPos;


//is really long
public class BlockMap<T> extends Object2ObjectLinkedOpenCustomHashMap<BlockPos,T> {
    public static final Strategy<BlockPos> strategy = new BlockHashStrategy();

    public BlockMap() {
        super(strategy);
    }


    private static final class BlockHashStrategy implements Strategy<BlockPos>{
        @Override
        public int hashCode(BlockPos obj) {
            return obj.getY()<<4 | obj.getX() | obj.getZ()<<16;
        }

        @Override
        public boolean equals(BlockPos a, BlockPos b) {
            return a.equals(b);
        }
    }
}