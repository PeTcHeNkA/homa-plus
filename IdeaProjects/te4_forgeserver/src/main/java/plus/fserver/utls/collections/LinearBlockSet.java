package plus.fserver.utls.collections;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;


public class LinearBlockSet implements Iterable<BlockPos>{
    private final Chunk root;

    private static class Chunk implements Iterable<Chunk>{
        private final VChunk[][] chunks = new VChunk[8][];
        private Chunk xLeft, xRight, zLeft, zRight;
        private final int x, z;

        private Chunk(int x, int z) {
            this.x = x;
            this.z = z;
        }

        // 16 x 16 x 8
        private static final class VChunk {
            private final byte[] data = new byte[64];

            private void set(int x, int y, int z, boolean b) {
                if(b)
                    data[x & 15 | ((z & 15) << 4)] |= ((byte) (y & 7));
                else
                    data[x & 15 | ((z & 15) << 4)] &= ~((byte) (y & 7));
            }

            private boolean get(int x, int y, int z) {
                y &= 7;
                return ((data[x & 15 | ((z & 15) << 4)] & y) == y);
            }

            public void clear(){
                byte[] data = this.data;
                for(int i = 0; i < 32; i++) data[i] = 0;
            }
        }


        public void set(int x, int y, int z, boolean b) {
            VChunk[] cur;
            if ((cur = chunks[y >> 5]) == null)
                chunks[y >> 5] = cur = new VChunk[8];

            VChunk chunk;
            if ((chunk = cur[(y & 31) >> 2]) == null)
                cur[(y & 31) >> 2] = chunk = new VChunk();

            chunk.set(x, y, z, b);
        }


        public boolean get(int x, int y, int z) {
            VChunk[] cur = chunks[y >> 5];
            if (cur == null) return false;
            VChunk chunk = cur[(y & 31) >> 2];
            if (chunk == null) return false;
            return chunk.get(x, y, z);
        }


        public Chunk curChunk(int cX, int cZ){
            if(x == cX && z == cZ)return this;
            if(cX < x){
                if(xLeft == null)xLeft = new Chunk(x - 1, z);
                return xLeft;
            }else if(cX > x){
                if(xRight == null)xRight = new Chunk(x + 1, z);
                return xRight;
            }
            if(cZ < z){
                if(zLeft == null)zLeft = new Chunk(x, z - 1);
                return zLeft;
            } else {
                if(zRight == null)zRight = new Chunk(x, z + 1);
                return zRight;
            }
        }

        private Chunk byIndex(int idx){
            switch (idx){
                case 0: return xLeft;
                case 1: return xRight;
                case 2: return zLeft;
                case 3: return zRight;
                default: return this;
            }
        }


        public void clear(){
            for (VChunk[] vChunk : chunks)
                if (vChunk != null)
                    for (VChunk vChunk1 : vChunk)
                        if (vChunk1 != null) vChunk1.clear();
        }


        @NotNull
        @Override
        public ChunkIter iterator() {
            return new ChunkIter(this);
        }
    }


    public LinearBlockSet(int x, int z) {
        root = new Chunk(x >> 4, z >> 4);
    }


    public boolean get(int x, int y, int z) {
        return root.curChunk(x >> 4, z >> 4).get(x, y, z);
    }


    public void set(int x, int y, int z, boolean b) {
        root.curChunk(x >> 4, z >> 4).set(x, y, z, b);
    }


    @NotNull
    @Override
    public Iterator<BlockPos> iterator() {
        return null;
    }



    private static class SVIter extends BlockPos implements Iterator<BlockPos>{
        private int dx, dy, dz, mask, cursor;

        public SVIter() {
            super(0,0,0);
        }

        public SVIter init(int dx, int dy, int dz, int mask){
            if(mask == 0){
                this.cursor = 32;
                return this;
            } else {
                this.cursor = 0;
            }
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.mask = mask;
            return this;
        }

        @Override
        public boolean hasNext() {
            return cursor < 32;
        }

        @Override
        public BlockPos next() {
            if(cursor < 32){
                throw new RuntimeException("TODO");
            } else {
                return null;
            }
        }
    }



    private static class ChunkIter implements Iterator<Chunk>{
        private final Chunk start;
        private ChunkIter current;
        private int flag = 0;
        private Chunk next;

        public ChunkIter(Chunk start) {
            this.start = start;
            nextItr();
            calcNext();
        }

        private void calcNext(){
            if(current.hasNext()){
                next = current.next();
            } else {
                nextItr();
                next = current == null? null : current.next();
            }
        }


        private void nextItr(){
            Chunk ch = start.byIndex(flag++);
            while (ch == null & flag < 4){
                ch = start.byIndex(flag++);
            }
            current = ch == null ? null : ch.iterator();
        }


        @Override
        public boolean hasNext() {
            return next != null;
        }


        @Override
        public Chunk next() {
            Chunk ret = next;
            calcNext();
            return ret;
        }
    }





    public static class Itr implements Iterator<BlockPos> {


        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public BlockPos next() {
            return null;
        }
    }
}


















