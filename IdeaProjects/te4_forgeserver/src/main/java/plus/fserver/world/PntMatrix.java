package plus.fserver.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.ChunkPos;
import plus.tson.utl.Te4HashMap;
import java.util.Arrays;
import static net.minecraft.pathfinding.PathNodeType.TYPES;


public class PntMatrix {
    private final Long2ObjectOpenHashMap<Chunked> data = new Long2ObjectOpenHashMap<>();
    //private final Te4HashMap<Long, Chunked> data = new Te4HashMap<>();
    private Chunked last = new Chunked(null, Integer.MIN_VALUE, Integer.MIN_VALUE);

    static class Group{
        private final Branch[] branches = new Branch[16];
        //             0-16,  0-16,  0-16
        Branch branch(int x, int y, int z){
            if(x<8) x = z <8?0:1;
            else    x = z <8?2:3;
            Branch result = branches[x = (x<<2 | y >> 2)];
            return result == null?branches[x] = new Branch():result;
        }
        void clean(){
            for (Branch br0:branches)
                if(br0 != null)br0.clean();
        }
    }


    static class Branch{
        private final int[] types = new int[64];
        //java link - 64bit, 256x64 more than 64x32
        //private final PathNodeType[] types = new PathNodeType[256];
        //                  8x,    4y,    8z
        PathNodeType get(int x, int y, int z){
            if(x>7) x-=8;
            if(z>7) z-=8;
            return TYPES[(types[x | z << 3] >> (y<<3)) & 0xFF];
        }
        void set(int x, int y, int z, PathNodeType type){
            if(x>7) x-=8;
            if(z>7) z-=8;
            x = x | z << 3;
            types[x] = types[x] & ~(0xff << (y<<3)) | type.id<<(y<<3);
        }
        void rem(int x, int y, int z){
            if(x>7) x-=8;
            if(z>7) z-=8;
            x = x | z << 3;
            types[x] = types[x] & ~(0xff << (y<<3));
        }

        void clean(){
            Arrays.fill(types, 0);
        }
    }


    public static class Chunked{
        private final Group[] groups = new Group[16];
        private final PntMatrix matrix;
        Chunked xAdd;
        Chunked zAdd;
        Chunked xMin;
        Chunked zMin;
        private final int x, z;

        Chunked(PntMatrix matrix, int x, int z) {
            this.matrix = matrix;
            this.x = x;
            this.z = z;
        }


        public void clean(){
            for (Group group:groups) if(group != null)group.clean();
        }


        public Chunked get(int x, int z){
            return executor(x>>4, z>>4);
        }


        private Chunked executor(int rx, int rz){
            if(rx == x){
                if(rz == z)return this;
                int delta = rz-z;
                if(delta>2 || delta<-2)return matrix.getChunked0(rx, rz);

                return rz > z ?
                        (zAdd == null?zAdd = matrix.getChunked0(x, z+1):zAdd).executor(rx, rz):
                        (zMin == null?zMin = matrix.getChunked0(x, z-1):zMin).executor(rx, rz);
            } else {
                int delta = rx-x;
                if(delta>2 || delta<-2)return matrix.getChunked0(rx, rz);
                delta = rz-z;
                if(delta>2 || delta<-2)return matrix.getChunked0(rx, rz);

                return rx > x ?
                            (xAdd == null?xAdd = matrix.getChunked0(x+1, z):xAdd).executor(rx, rz):
                            (xMin == null?xMin = matrix.getChunked0(x-1, z):xMin).executor(rx, rz);
            }
//            if(rx == x){
//                if(rz == z)return this;
//                return rz > z ?
//                        (zAdd == null?zAdd = matrix.getChunked0(x, z+1):zAdd).executor(rx, rz):
//                        (zMin == null?zMin = matrix.getChunked0(x, z-1):zMin).executor(rx, rz);
//            }
//            return rx > x ?
//                        (xAdd == null?xAdd = matrix.getChunked0(x+1, z):xAdd).executor(rx, rz):
//                        (xMin == null?xMin = matrix.getChunked0(x-1, z):xMin).executor(rx, rz);
        }


        public PathNodeType getType(int x, int y, int z){
            Group group = executor(x>>4, z>>4).groups[y >> 4];
            if(group==null)return null;
            Branch branch = group.branch(x&=15, y&15, z&=15);
            if(branch==null)return null;
            return branch.get(x, y&7, z);
        }


        public PathNodeType getCur(int x, int y, int z){
            Group group = groups[y >> 4];
            if(group==null)return null;
            Branch branch = group.branch(x&=15, y&15, z&=15);
            if(branch==null)return null;
            return branch.get(x, y&7, z);
        }


        public void setCur(int x, int y, int z, PathNodeType type){
            Group group = groups[y >> 4];
            if(group==null)group = groups[y >> 4] = new Group();
            Branch branch = group.branch(x&=15, y&15, z&=15);
            branch.set(x, y&7, z, type);
        }


        public void setType(int x, int y, int z, PathNodeType type){
            Chunked chunked = executor(x>>4, z>>4);
            Group group = chunked.groups[y >> 4];
            if(group==null)group = chunked.groups[y >> 4] = new Group();
            Branch branch = group.branch(x&=15, y&15, z&=15);
            branch.set(x, y&7, z, type);
        }


        public void rem(int x, int y, int z){
            Chunked chunked = executor(x>>4, z>>4);
            Group group = chunked.groups[y >> 4];
            if(group==null)group = chunked.groups[y >> 4] = new Group();
            Branch branch = group.branch(x&=15, y&15, z&=15);
            branch.rem(x, y&7, z);
        }
    }


    public void clean(){
        for (Chunked chunked:data.values()){
            chunked.clean();
        }
    }


    public Chunked getChunkedA(int x, int z){
        if(last.x == x && last.z == z)return last;
        return last = getChunked0(x, z);
    }


    public Chunked getChunked(int x, int z){
        if(last.x == (x>>=4) && last.z == (z>>=4))return last;
        return last = getChunked0(x, z);
    }


    private Chunked getChunked0(int x, int z){
        long key = ChunkPos.asLong(x, z);
        //todo use primitive long in collection
        Chunked res = data.get(key);
        if(res == null)data.put(key, res = new Chunked(this, x, z));
        return res;
    }
}