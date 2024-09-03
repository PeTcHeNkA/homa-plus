package plus.fserver.server;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.collections.ArrayRemSwapListE6;
import plus.fserver.world.CollisionPredicate;


public class EntityCollider {
    private final CollisionPredicate entityCP = new CollisionPredicate();

    private static final Entity[] EMPTY = new Entity[0];
    private final WorldServer world;
    private Entity[] temp = EMPTY;
    private AsyncCollideTask task = new AsyncCollideTask();
    private int limit;

    public EntityCollider(WorldServer world) {
        this.world = world;
    }


    public void begin(int limit){
        task.init(this.limit = limit);
    }


    public void complete(){
        temp = EMPTY;
        if(!task.isEmptyC()) {
            world.addAsyncTask(task);
            task = new AsyncCollideTask();
        }
        entityCP.reset();
    }


    public void collideAll(Chunk chunk){
        collideAll(chunk.getEntityLists());
    }


    public void collideAll(ClassInheritanceMultiMap<Entity>[] entityLists){
        ArrayRemSwapListE6<Entity> list;

        for (ClassInheritanceMultiMap<Entity> part:entityLists){
            int size;
            if((size = (list = part.rawValues()).sizeC()) < 2)continue;
            if(size > 12){
                Entity[] entities = export(list, size);
                temp = EMPTY;
                if(size > limit){
                    task.add((cp, limit1, temp) -> collideAllB(cp, entities, size, limit1, temp));
                } else {
                    task.add((cp, limit12, temp) -> collideAll(cp, entities, size));
                }
            } else {
                collideAll(entityCP, export(list, size), size);
            }
        }
    }


    private Entity[] export(ArrayRemSwapListE6<Entity> src, int size){
        Object[] raw = src.raw();
        Entity[] result = temp;
        if(result.length < size){
            result = temp = new Entity[size];
        }
        for (int i = 0; i < size; i++){
            result[i] = (Entity) raw[i];
        }
        return result;
    }


    private static class AsyncCollideTask extends ArrayListE6<CollideTask> implements Runnable{
        private final CollisionPredicate cp = new CollisionPredicate();
        private final ArrayListE6<Entity> temp = new ArrayListE6<>();
        private int limit;

        private void init(int limit) {
            this.limit = limit;
        }

        @Override
        public void run() {
            final ArrayListE6<Entity> temp = this.temp;
            final CollisionPredicate cp = this.cp;
            final int limit = this.limit, size = this.size;
            final Object[] data = this.data;
            for (int i = 0; i < size; i++)
                ((CollideTask) data[i]).collide(cp, limit, temp);
        }
    }


    private interface CollideTask{
        void collide(CollisionPredicate cp, int limit, ArrayListE6<Entity> temp);
    }


    private static void collideAll(CollisionPredicate cp, Entity[] entities, int size){
        Predicate<Entity> e1;
        Entity[] parts;
        for (int i = 0; i < size; i++){
            Entity entity;
            if((entity = entities[i]).useSelfCollisions())continue;

            e1 = cp.forEntity(entity);
            AxisAlignedBB aabb = entity.getEntityBoundingBox();

            for (int j = i + 1; j < size; j++){
                Entity check;
                if((check = entities[j]).useSelfCollisions())continue;

                if(check.getEntityBoundingBox().intersects(aabb)){
                    if(e1.apply(check))
                        check.collideWith(entity);
                } else if((parts = entity.getParts()) != null){
                    for (Entity entity1:parts){
                        if(e1.apply(check))
                            check.collideWith(entity1);
                    }
                }
            }
        }
    }


    private static void collideAllB(CollisionPredicate cp, Entity[] entities, int size, int limit, ArrayListE6<Entity> temp){
        Predicate<Entity> e1;
        Entity[] parts;
        temp.fclear();

        for (int i = 0; i < size; i++){
            Entity entity;
            if((entity = entities[i]).useSelfCollisions())continue;

            e1 = cp.forEntity(entity);
            AxisAlignedBB aabb = entity.getEntityBoundingBox();

            for (int j = i + 1; j < size; j++){
                Entity check;
                if((check = entities[j]).useSelfCollisions())continue;

                if(check.getEntityBoundingBox().intersects(aabb)){
                    temp.addF(entity);
                    if(e1.apply(check))
                        check.collideWith(entity);
                } else if((parts = entity.getParts()) != null){
                    for (Entity entity1:parts){
                        if(e1.apply(check))
                            check.collideWith(entity1);
                    }
                }
            }
            int s;
            if((s = temp.sizeC()) > limit){
                Object[] raw = temp.raw();
                for (int j = 0; j < s; j++){
                    ((Entity)raw[j]).attackEntityFrom(DamageSource.CRAMMING, 1);
                }
            }
            temp.fclear();
        }
    }
}