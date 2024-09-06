package plus.fserver.utls.collections;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import java.util.Iterator;
import java.util.function.Consumer;


public class EntityChunkIter implements Iterable<Entity>, Iterator<Entity> {
    private final ClassInheritanceMultiMap<Entity>[] lists;
    private EntityList<Entity> list;
    private int cursor1 = 0, cursor2 = 0;
    private Entity next;

    public EntityChunkIter(Chunk chunk) {
        list = (lists = chunk.getEntityLists())[0].rawValues();
        calcNext();
    }

    private void calcNext(){
        if(cursor2 < list.sizeC()){
            next = list.getC(cursor2++);
            return;
        }
        while (cursor1 < 16) {
            EntityList<Entity> res = lists[cursor1++].rawValues();
            if(!res.isEmptyC()){
                next = res.getC(0);
                cursor2 = 1;
                list = res;
                return;
            }
        }
        next = null;
    }


    @Override
    public Iterator<Entity> iterator() {
        return this;
    }


    @Override
    public void forEach(Consumer<? super Entity> action) {
        ClassInheritanceMultiMap<Entity>[] lists = this.lists;
        for (int i = 1; i < lists.length; ++i) {
            EntityList<Entity> vals = lists[i].rawValues();
            if(!vals.isEmptyC()) vals.forEach(action);
        }
    }


    @Override
    public boolean hasNext() {
        return next != null;
    }


    @Override
    public Entity next() {
        Entity prev = next;
        calcNext();
        return prev;
    }
}