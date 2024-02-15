package plus.fserver.server.chunk;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;


@Deprecated
public class ChunkSlimMobContainer implements IChunkMobContainer{
    private static final int LVL_MASK = (1<<6)-1;
    private static final int LVL_CHECK_MASK = 255 & ~7;

    private final Part[] parts = new Part[4];

    private static class Part{
        private final Level[] levels = new Level[8];
    }
    private static class Level{
        private final ClassInheritanceMultiMap<Entity>[] data = new ClassInheritanceMultiMap[4];
    }


    @Override
    public void addEntity(Entity entity){
        addEntityTo(
                entity,
                MathHelper.floor(entity.posX),
                MathHelper.floor(entity.posY),
                MathHelper.floor(entity.posZ)
        );
    }


    @Override
    public void removeEntity(Entity entity){
        removeEntityAt(
                entity,
                MathHelper.floor(entity.posX),
                MathHelper.floor(entity.posY),
                MathHelper.floor(entity.posZ)
        );
    }


    private void addEntityTo(Entity entity, int xPos, int yPos, int zPos){
        yPos = yPos & 255;
        Part part = parts[yPos>>6];
        if(part==null)parts[yPos>>6] = part = new Part();
        int idx = (yPos & LVL_MASK) >> 3;

        Level level = part.levels[idx];
        if(level == null)part.levels[idx] = level = new Level();

        idx = ((xPos >> 2) & 2) | ((zPos >> 3) & 1);
        ClassInheritanceMultiMap<Entity> data = level.data[idx];
        if(data == null)level.data[idx] = data = new ClassInheritanceMultiMap<>(Entity.class);
        data.add(entity);

        entity.chunkCoordX = xPos >> 4;
        entity.chunkCoordZ = zPos >> 4;
        entity.chunkCoordY = yPos >> 4;
    }


    private void removeEntityAt(Entity entity, int xPos, int yPos, int zPos){
        Part part = parts[yPos>>6];
        if(part==null)parts[yPos>>6] = part = new Part();
        int idx = (yPos & LVL_MASK) >> 3;

        Level level = part.levels[idx];
        if(level == null)part.levels[idx] = level = new Level();

        idx = ((xPos >> 2) & 2) | ((zPos >> 3) & 1);
        ClassInheritanceMultiMap<Entity> data = level.data[idx];
        if(data == null)level.data[idx] = data = new ClassInheritanceMultiMap<>(Entity.class);
        data.remove(entity);
    }


    @Override
    public void updateEntitySection(WorldServer world, Entity entity){
        int pxPos = MathHelper.floor(entity.prevPosX);
        int pyPos = MathHelper.floor(entity.prevPosY);
        int pzPos = MathHelper.floor(entity.prevPosZ);

        int xPos = MathHelper.floor(entity.posX);
        int yPos = MathHelper.floor(entity.posY);
        int zPos = MathHelper.floor(entity.posZ);

        if(xPos >> 4 != entity.chunkCoordX || zPos>> 4 != entity.chunkCoordZ){
            moveToOtherChunk(world, entity, pxPos, pyPos, pzPos, xPos, zPos);
            return;
        }

        if((xPos & 8) != (pxPos & 8) || (zPos & 8) != (pzPos & 8)
                || (yPos & LVL_CHECK_MASK) != (pyPos & LVL_CHECK_MASK)){

            if(entity.addedToChunk) removeEntityAt(entity, pxPos, pyPos, pzPos);
            addEntityTo(entity, xPos, yPos, zPos);
            entity.addedToChunk = true;
        }
    }


    private void moveToOtherChunk(WorldServer world, Entity entityIn,
                                 int pxPos, int pyPos, int pzPos, int xPos, int zPos
    ){
        Chunk chunk;
        ChunkProviderServer cps = world.getChunkProvider();

        if (entityIn.addedToChunk) {
            removeEntityAt(entityIn, pxPos, pyPos, pzPos);
        }
        chunk = cps.getLoadedChunk(xPos>>4, zPos>>4);

        if (!entityIn.setPositionNonDirty() && chunk == null) {
            entityIn.addedToChunk = false;
        } else {
            if(chunk == null)chunk = cps.provideChunk(xPos>>4, zPos>>4);
            chunk.addEntity(entityIn);
        }
    }


    @Override
    public void getEntitiesWithinAABBLimited(@Nullable Entity exclude, AxisAlignedBB aabb, List<Entity> listToFill, @Nullable Predicate<? super Entity > filter, int max) {
        int yMin = MathHelper.floor(aabb.minY)>>3;
        if(yMin<0)yMin = 0;
        int yMax = MathHelper.floor(aabb.maxY)>>3;
        if(yMax>3)yMax = 3;

        int minX = (MathHelper.floor(aabb.minX)>>3) & 1;
        int maxX = (MathHelper.floor(aabb.maxX)>>3) & 1;

        int minZ = (MathHelper.floor(aabb.minZ)>>3) & 1;
        int maxZ = (MathHelper.floor(aabb.maxZ)>>3) & 1;

        for (int cy = yMin; cy <= yMax; ++cy){
            Part part = parts[cy>>3];
            if(part==null)continue;
            Level level = part.levels[cy & 7];
            if(level == null)continue;

            for (int cx = minX; cx <= maxX; ++cx){
                for (int cz = minZ; cz <= maxZ; ++cz){

                    ClassInheritanceMultiMap<Entity> map = level.data[(cx << 1) | cz];
                    if(map==null || map.isEmpty())continue;

                    for (ClassInheritanceMultiMap<Entity>.FastIter iter = map.fastIter();
                         iter.hasNext();){

                        Entity entity = iter.readAndNext();
                        if(entity != exclude){

                            if(entity.getEntityBoundingBox().intersects(aabb)){
                                if(filter == null || filter.apply(entity)) listToFill.add(entity);
                            }
                            Entity[] parts = entity.getParts();
                            if(parts != null){
                                for (Entity entity1:parts){
                                    if(entity1.getEntityBoundingBox().intersects(aabb)){
                                        if(filter == null || filter.apply(entity1)) listToFill.add(entity1);
                                    }
                                }
                            }
                            if(--max<1)return;
                        }
                    }
                }
            }
        }
    }


    @Override
    public void getEntitiesWithinAABB(@Nullable final Entity exclude, final AxisAlignedBB aabb, final List<Entity> listToFill, @Nullable final Predicate <? super Entity > filter) {
        int yMin = MathHelper.floor(aabb.minY)>>3;
        if(yMin<0)yMin = 0;
        int yMax = MathHelper.floor(aabb.maxY)>>3;
        if(yMax>3)yMax = 3;

        int minX = (MathHelper.floor(aabb.minX)>>3) & 1;
        int maxX = (MathHelper.floor(aabb.maxX)>>3) & 1;

        int minZ = (MathHelper.floor(aabb.minZ)>>3) & 1;
        int maxZ = (MathHelper.floor(aabb.maxZ)>>3) & 1;

        for (int cy = yMin; cy <= yMax; ++cy){
            Part part = parts[cy>>3];
            if(part==null)continue;
            Level level = part.levels[cy & 7];
            if(level == null)continue;

            for (int cx = minX; cx <= maxX; ++cx){
                for (int cz = minZ; cz <= maxZ; ++cz){

                    ClassInheritanceMultiMap<Entity> map = level.data[(cx << 1) | cz];
                    if(map==null || map.isEmpty())continue;

                    for (ClassInheritanceMultiMap<Entity>.FastIter iter = map.fastIter();
                         iter.hasNext();){

                        Entity entity = iter.readAndNext();
                        if(entity != exclude){

                            if(entity.getEntityBoundingBox().intersects(aabb)){
                                if(filter == null || filter.apply(entity)) listToFill.add(entity);
                            }
                            Entity[] parts = entity.getParts();
                            if(parts != null){
                                for (Entity entity1:parts){
                                    if(entity1.getEntityBoundingBox().intersects(aabb)){
                                        if(filter == null || filter.apply(entity1)) listToFill.add(entity1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public <T extends Entity> void getEntitiesOfTypeWithinAABB(final Class <? extends T > entityClass, final AxisAlignedBB aabb, final List<T> listToFill, @Nullable final Predicate <? super T > filter) {
        int yMin = MathHelper.floor(aabb.minY)>>3;
        if(yMin<0)yMin = 0;
        int yMax = MathHelper.floor(aabb.maxY)>>3;
        if(yMax>3)yMax = 3;

        int minX = (MathHelper.floor(aabb.minX)>>3) & 1;
        int maxX = (MathHelper.floor(aabb.maxX)>>3) & 1;

        int minZ = (MathHelper.floor(aabb.minZ)>>3) & 1;
        int maxZ = (MathHelper.floor(aabb.maxZ)>>3) & 1;

        for (int cy = yMin; cy <= yMax; ++cy){
            Part part = parts[cy>>3];
            if(part==null)continue;
            Level level = part.levels[cy & 7];
            if(level == null)continue;

            for (int cx = minX; cx <= maxX; ++cx){
                for (int cz = minZ; cz <= maxZ; ++cz){

                    ClassInheritanceMultiMap<Entity> map = level.data[(cx << 1) | cz];
                    if(map==null || map.isEmpty())continue;

                    for (T entity:map.getByClass(entityClass)){
                        if(entity.getEntityBoundingBox().intersects(aabb)){
                            if(filter == null || filter.apply(entity)) listToFill.add(entity);
                        }
                    }
                }
            }
        }
    }


    @Override
    public Iterator<ClassInheritanceMultiMap<Entity>> getEntityLists() {
        return new SaveIter();
    }


    private final class SaveIter implements Iterator<ClassInheritanceMultiMap<Entity>>{
        private int partCursor;
        private int lvlCursor;
        private int cursor;
        ClassInheritanceMultiMap<Entity> next;
        private SaveIter(){
            next();
        }


        @Override
        public boolean hasNext() {
            return next != null;
        }


        @Override
        public ClassInheritanceMultiMap<Entity> next() {
            ClassInheritanceMultiMap<Entity> result = next;
            next = null;
            for (;partCursor<parts.length;++partCursor){
                Part part = parts[partCursor];
                if(part==null)continue;
                for (;lvlCursor<part.levels.length;++lvlCursor){
                    Level level = part.levels[lvlCursor];
                    if(level == null)continue;
                    for (;cursor<level.data.length;++cursor){
                        next = level.data[cursor];
                        if(next != null){
                            ++cursor;
                            return result;
                        }
                    }
                    cursor = 0;
                }
                lvlCursor = 0;
            }
            return result;
        }
    }
}
