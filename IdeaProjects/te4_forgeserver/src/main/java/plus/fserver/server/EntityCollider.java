package plus.fserver.server;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.collections.EntityList;
import plus.fserver.world.CollisionPredicate;


public class EntityCollider {
    private final CollisionPredicate entityCP = new CollisionPredicate();
    private final AsyWorldTickerV2 ticker;
    private AsyncCollideTask task = new AsyncCollideTask();
    private int limit;

    public EntityCollider(WorldServer world) {
        ticker = world.getMinecraftServer().ticker();
    }


    public void begin(int limit){
        task.init(this.limit = limit);
    }


    public void complete(){
        AsyncCollideTask curTask;
        if(!(curTask = task).isEmptyC()) {
            ticker.runAsync(curTask);
            task = new AsyncCollideTask();
        }
        entityCP.reset();
    }


    public void collideAll(Chunk chunk){
        collideAll(chunk.getEntityLists());
    }


    public void collideAll(ClassInheritanceMultiMap<Entity>[] entityLists){
        for (ClassInheritanceMultiMap<Entity> list:entityLists) {
            if (list != null) {
                int size;
                if ((size = list.sizeC()) < 2) continue;
                if (size > 12) {
                    Entity[] entities = list.toArray();
                    if (size > limit) {
                        task.add((cp, limit1, temp) -> collideAllB(cp, entities, size, limit1, temp));
                    } else {
                        task.add((cp, limit12, temp) -> collideAll(cp, entities, size));
                    }
                } else {
                    collideAll(entityCP, list.raw(), size);
                }
            }
        }
    }


    private static class AsyncCollideTask extends ArrayListE6<CollideTask> implements Runnable{
        private final CollisionPredicate cp = new CollisionPredicate();
        private final EntityList<Entity> temp = new EntityList<>();
        private int limit;

        private void init(int limit) {
            this.limit = limit;
        }

        @Override
        public void run() {
            final EntityList<Entity> temp = this.temp;
            final CollisionPredicate cp = this.cp;
            final int limit = this.limit, size = this.size;
            final Object[] data = this.data;
            for (int i = 0; i < size; i++)
                ((CollideTask) data[i]).collide(cp, limit, temp);
        }
    }


    interface CollideTask{
        void collide(CollisionPredicate cp, int limit, EntityList<Entity> temp);
    }


    static void collideAll(CollisionPredicate cp, Entity[] entities, int size){
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


    static void collideAllB(CollisionPredicate cp, Entity[] entities, int size, int limit, EntityList<Entity> temp){
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
                Entity[] raw = temp.toArray();
                ((WorldServer)raw[0].world).addSyncTask(() -> {
                    for (int j = 0; j < s; j++){
                        raw[j].attackEntityFrom(DamageSource.CRAMMING, 1);
                    }
                });
            }
            temp.fclear();
        }
    }
}