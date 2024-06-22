package col;

import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.utls.collections.EntityChunkIter;


public class TestChunkItr {
    public static void test(Chunk chunk){
        try {
            EntityChunkIter iter = new EntityChunkIter(chunk);
            while (iter.hasNext()) {
                Entity entity = iter.next();
                System.out.println(entity.getName() + ":" + entity.getEntityId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}