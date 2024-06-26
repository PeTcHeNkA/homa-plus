package col;

import net.minecraft.entity.Entity;
import plus.fserver.utls.collections.ChunkedEntityList;
import plus.fserver.utls.collections.EntityIDHashSplitedList;
import plus.fserver.utls.debug.DebugEntity;
import java.util.ArrayList;
import java.util.Iterator;


public class TestChunkList {
    public static void main(String[] args) {
        EntityIDHashSplitedList list = new EntityIDHashSplitedList();

        ArrayList<DebugEntity> entities = new ArrayList<>();

        for (int i=0;i<100;i++){
            DebugEntity entity = new DebugEntity(i*2, 1, i*2).track();
            list.addF(entity);
            entities.add(entity);
        }

//        for (Iterator<Entity> iterator = list.nearEntityIter(new DebugEntity(100, 1, 3)); iterator.hasNext();) {
//            Entity entity = iterator.next();
//            System.out.println(entity);
//        }


        //System.out.println(list);

//        for (DebugEntity entity : entities) {
//            list.removeEntity(entity);
//        }

        System.out.println(list.size());

        int s = 0;

        System.out.println("--------------");

        for (Entity entity: list) {
            System.out.println(entity);
            ++s;
        }

        System.out.println(s);
    }
}
