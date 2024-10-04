package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.Vec3d;


public class EntityAIAvoidEntity<T extends Entity> extends EntityAIBase {
    protected EntityCreature entity;
    private final double farSpeed;
    private final double nearSpeed;
    protected T closestLivingEntity;
    private final float avoidDistance;
    private Path path;
    private final PathNavigate navigation;
    private final Class<T> classToAvoid;
    private final Predicate<? super T> predicate;

    public EntityAIAvoidEntity(EntityCreature creature, Class<T> clazz, float distance, double farSpeed, double nearSpeed) {
        this(creature, clazz, Predicates.alwaysTrue(), distance, farSpeed, nearSpeed);
    }


    public EntityAIAvoidEntity(EntityCreature creature, Class<T> avoid, Predicate<? super T> avoidSelector, float distance, double farSpeed, double nearSpeed) {
        predicate = new AvoidSelector<>(creature, avoidSelector);

        this.entity = creature;
        this.classToAvoid = avoid;;
        this.avoidDistance = distance;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.navigation = creature.getNavigator();
        this.setMutexBits(1);
    }


    private static class AvoidSelector<T extends Entity> implements Predicate<T>{
        private final EntityCreature entity;
        private final Predicate<? super T> parent;

        private AvoidSelector(EntityCreature entity, Predicate<? super T> parent) {
            this.entity = entity;
            this.parent = parent;
        }

        @Override
        public boolean apply(@Nullable T entity) {
            if(!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()
                    && !((EntityPlayer)entity).isCreative()){

                if(entity.isEntityAlive() && this.entity.getEntitySenses().canSee(entity)
                        && !this.entity.isOnSameTeam(entity)){
                    return parent.apply(entity);
                }
            }
            return false;
        }
    }


    @Override
    public boolean shouldExecute() {
        EntityCreature entity;
        List<T> list = (entity = this.entity).world.getEntitiesWithinAABB(this.classToAvoid,
                entity.getEntityBoundingBox().grow(this.avoidDistance, 3.0, this.avoidDistance),
                predicate
        );

        if (list.isEmpty()) {
            return false;
        } else {
            Entity closest = this.closestLivingEntity = list.get(0);
            Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, 16, 7, new Vec3d(closest.posX, closest.posY, closest.posZ));
            if (vec == null) {
                return false;
            } else if (closest.getDistanceSq(vec.x, vec.y, vec.z) < closest.getDistanceSq(entity)) {
                return false;
            } else {
                return (this.path = this.navigation.getPathToXYZ(vec.x, vec.y, vec.z)) != null;
            }
        }
    }


    @Override
    public boolean shouldContinueExecuting() {
        return !this.navigation.noPath();
    }


    @Override
    public void startExecuting() {
        this.navigation.setPath(this.path, this.farSpeed);
    }


    @Override
    public void resetTask() {
        this.closestLivingEntity = null;
    }


    @Override
    public void updateTask() {
        if (this.entity.getDistanceSq(this.closestLivingEntity) < 49.0) {
            this.entity.getNavigator().setSpeed(this.nearSpeed);
        } else {
            this.entity.getNavigator().setSpeed(this.farSpeed);
        }
    }
}
