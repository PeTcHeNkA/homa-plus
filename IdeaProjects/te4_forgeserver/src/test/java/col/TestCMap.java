package col;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.utls.collections.ChunkMap;


public class TestCMap {
    private static final int iters = 200;
    private static final int cSize = 400;
    private static final int count = 20;

    public static void main(String[] args) {
        Chunk[] chunks = new Chunk[cSize*cSize];
        for (int x = 0; x < cSize; x++) {
            for (int z = 0; z < cSize; z++) {
                chunks[x*cSize+z] = new Chunk(null, x, z);
            }
        }

        Long2ObjectOpenHashMap<Chunk> def = new Long2ObjectOpenHashMap<>();
        ChunkMap map = new ChunkMap();

        for (int i=0;i<10;i++){
            testTime("def", () -> testBasic(def, chunks, count));
            testTime("map", () ->  testSelf(map, chunks, count));
        }
    }

    private static void test(Chunk last){

    }


    private static void testBasic(Long2ObjectOpenHashMap<Chunk> map, Chunk[] chunks, int count) {
        for (int i = 0; i < count; i++) {
            for (Chunk c : chunks) {
                map.put(ChunkPos.asLong(c.x, c.z), c);
            }
            for (int j = 0; j < iters; j++) {
                Chunk chunk = chunks[j];
                test(map.get(ChunkPos.asLong(chunk.x, chunk.z)));
            }
            for (Chunk chunk : chunks) {
                map.remove(ChunkPos.asLong(chunk.x, chunk.z));
            }
        }
        if(map.size() != 0)throw new RuntimeException();
    }


    private static void testSelf(ChunkMap map, Chunk[] chunks, int count) {
        for (int i = 0; i < count; i++) {
            for (Chunk c : chunks) {
                map.put(c);
            }
            for (int j = 0; j < iters; j++) {
                Chunk chunk = chunks[j];
                test(map.get(chunk.x, chunk.z));
            }
            for (Chunk c : chunks) {
                map.remove(c.x, c.z);
            }
        }
        if(map.size() != 0)throw new RuntimeException();
    }


    private static void testTime(String h, Runnable r){
        long start = System.currentTimeMillis();
        r.run();
        System.out.println(h+":"+(System.currentTimeMillis()-start));
    }
}
