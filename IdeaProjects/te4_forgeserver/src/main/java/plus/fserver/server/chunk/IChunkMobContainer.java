package plus.fserver.server.chunk;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;


@Deprecated
public interface IChunkMobContainer {
    void addEntity(Entity entity);
    void removeEntity(Entity entity);
    void updateEntitySection(WorldServer world, Entity entity);
    void getEntitiesWithinAABBLimited(@Nullable Entity exclude, AxisAlignedBB aabb, List<Entity> listToFill, @Nullable Predicate<? super Entity > filter, int max);
    void getEntitiesWithinAABB(@Nullable final Entity exclude, final AxisAlignedBB aabb, final List<Entity> listToFill, @Nullable final Predicate <? super Entity > filter);
    <T extends Entity> void getEntitiesOfTypeWithinAABB(final Class <? extends T > entityClass, final AxisAlignedBB aabb, final List<T> listToFill, @Nullable final Predicate <? super T > filter);
    Iterator<ClassInheritanceMultiMap<Entity>> getEntityLists();
}