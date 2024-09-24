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
        ClassInheritanceMultiMap<Entity>[] lists = this.lists = chunk.getEntityLists();

        while (cursor1 < 16) {
            EntityList<Entity> res = lists[cursor1++];
            if(res != null && !res.isEmptyC()){
                next = res.getC(0);
                cursor2 = 1;
                list = res;
                return;
            }
        }
    }


    private void calcNext(){
        EntityList<Entity> list;
        if(cursor2 < (list = this.list).sizeC()){
            next = list.getC(cursor2++);
            return;
        }
        ClassInheritanceMultiMap<Entity>[] lists = this.lists;
        while (cursor1 < 16) {
            EntityList<Entity> res = lists[cursor1++];
            if(res != null && !res.isEmptyC()){
                next = res.getC(0);
                cursor2 = 1;
                this.list = res;
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
        EntityList<Entity> vals;
        for (ClassInheritanceMultiMap<Entity> entities : this.lists)
            if ((vals = entities) != null && !vals.isEmptyC()) vals.forEach(action);
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