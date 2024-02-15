package plus.fserver.server.chunk;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.apache.logging.log4j.core.util.ObjectArrayIterator;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;


@Deprecated
public class ChunkDefMobContainer implements IChunkMobContainer{
    private final ClassInheritanceMultiMap<Entity>[] entityLists = new ClassInheritanceMultiMap[16];
    public ChunkDefMobContainer(){
        for (int i = 0; i < this.entityLists.length; ++i) {
            this.entityLists[i] = new ClassInheritanceMultiMap<>(Entity.class);
        }
    }


    //MinecraftForge.EVENT_BUS.post(new EntityEvent.EnteringChunk(entityIn, x, z, entityIn.chunkCoordX, entityIn.chunkCoordZ));
    @Override
    public void addEntity(Entity entityIn) {
        int x = MathHelper.floor(entityIn.posX / 16.0D);
        int z = MathHelper.floor(entityIn.posZ / 16.0D);

        int part = MathHelper.floor(entityIn.posY / 16.0D);

        if (part < 0) {
            part = 0;
        }

        if (part >= this.entityLists.length) {
            part = this.entityLists.length - 1;
        }

        entityIn.addedToChunk = true;
        entityIn.chunkCoordX = x;
        entityIn.chunkCoordY = part;
        entityIn.chunkCoordZ = z;
        this.entityLists[part].add(entityIn);
    }


    @Override
    public void removeEntity(Entity entityIn) {
        int index = entityIn.chunkCoordY;
        if (index < 0) {
            index = 0;
        }
        if (index >= this.entityLists.length) {
            index = this.entityLists.length - 1;
        }
        this.entityLists[index].remove(entityIn);
    }


    @Override
    public void updateEntitySection(WorldServer world, Entity entityIn) {
        int cXpos = MathHelper.floor(entityIn.posX / 16.0D);
        int cYpos = MathHelper.floor(entityIn.posY / 16.0D);
        int cZpos = MathHelper.floor(entityIn.posZ / 16.0D);

        if (!entityIn.addedToChunk || entityIn.chunkCoordX != cXpos || entityIn.chunkCoordY != cYpos || entityIn.chunkCoordZ != cZpos) {
            ChunkProviderServer cps = world.getChunkProvider();
            Chunk chunk;
            if (entityIn.addedToChunk) {
                chunk = cps.getLoadedChunk(entityIn.chunkCoordX, entityIn.chunkCoordZ);
                if(chunk != null) chunk.removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
            }
            chunk = cps.getLoadedChunk(cXpos, cZpos);

            if (!entityIn.setPositionNonDirty() && chunk == null) {
                entityIn.addedToChunk = false;
            } else {
                if(chunk == null)chunk = cps.provideChunk(cXpos, cZpos);
                chunk.addEntity(entityIn);
            }
        }
    }


    @Override
    public void getEntitiesWithinAABBLimited(@Nullable Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, @Nullable Predicate<? super Entity > filter, int max) {
        int i = MathHelper.clamp(
                MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D),
                0, 15
        ),
                j = MathHelper.clamp(
                        MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D),
                        0, 15
                );
        Entity[] parts;
        Entity entity;

        if(filter == null) {
            for (;i <= j; ++i)
                if (!this.entityLists[i].isEmpty()) {
                    for (ClassInheritanceMultiMap<Entity>.FastIter iter = this.entityLists[i].fastIter();
                         iter.hasNext();
                    )
                        if ((entity = iter.readAndNext()) != entityIn && entity.getEntityBoundingBox().intersects(aabb)) {
                            listToFill.add(entity);
                            if ((parts = entity.getParts()) != null)
                                for (Entity entity1 : parts)
                                    if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb))
                                        listToFill.add(entity1);
                            if(--max<0)return;
                        }
                }
        } else {
            for (;i <= j; ++i)
                if (!this.entityLists[i].isEmpty()) {
                    for (ClassInheritanceMultiMap<Entity>.FastIter iter = this.entityLists[i].fastIter();
                         iter.hasNext();
                    )
                        if ((entity = iter.readAndNext()) != entityIn && entity.getEntityBoundingBox().intersects(aabb)) {
                            if (filter.apply(entity)) listToFill.add(entity);

                            if ((parts = entity.getParts()) != null)
                                for (Entity entity1 : parts)
                                    if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb)
                                            && (filter.apply(entity1)))
                                        listToFill.add(entity1);
                            if(--max<0)return;
                        }
                }
        }
    }


    @Override
    public void getEntitiesWithinAABB(@Nullable final Entity entityIn, final AxisAlignedBB aabb, final List<Entity> listToFill, @Nullable final Predicate <? super Entity > filter) {
        int i = MathHelper.clamp(
                MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D),
                0, 15
        ),
                j = MathHelper.clamp(
                        MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D),
                        0, 15
                );
        if(filter == null) {
            for (Entity entity;i <= j; ++i) if (!this.entityLists[i].isEmpty()) {
                for (ClassInheritanceMultiMap<Entity>.FastIter iter = this.entityLists[i].fastIter();
                     iter.hasNext();
                )
                    if ((entity = iter.readAndNext()).getEntityBoundingBox().intersects(aabb) && entity != entityIn) {
                        listToFill.add(entity);
                        if (entity.getParts() != null)
                            for (Entity entity1 : entity.getParts())
                                if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb))
                                    listToFill.add(entity1);
                    }
            }
        } else {
            for (Entity entity;i <= j; ++i) if (!this.entityLists[i].isEmpty()) {
                for (ClassInheritanceMultiMap<Entity>.FastIter iter = this.entityLists[i].fastIter();
                     iter.hasNext();
                )
                    if ((entity = iter.readAndNext()).getEntityBoundingBox().intersects(aabb) && entity != entityIn) {
                        if (filter.apply(entity)) listToFill.add(entity);
                        if (entity.getParts() != null)
                            for (Entity entity1 : entity.getParts())
                                if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb)
                                        && (filter.apply(entity1)))
                                    listToFill.add(entity1);
                    }
            }
        }
    }


    @Override
    public <T extends Entity> void getEntitiesOfTypeWithinAABB(final Class <? extends T > entityClass, final AxisAlignedBB aabb, final List<T> listToFill, @Nullable final Predicate <? super T > filter) {
        final int i = MathHelper.clamp(
                MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D),
                0, this.entityLists.length - 1
        ),
                j = MathHelper.clamp(
                        MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D),
                        0, this.entityLists.length - 1
                );
        for (int k = i; k <= j; ++k) {
            for (T t : this.entityLists[k].getByClass(entityClass)) {
                if (t.getEntityBoundingBox().intersects(aabb) && (filter == null || filter.apply(t)))
                    listToFill.add(t);
            }
        }
    }


    @Override
    public Iterator<ClassInheritanceMultiMap<Entity>> getEntityLists() {
        return new ObjectArrayIterator<>(entityLists);
    }
}