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
        predicate = new AvoidSelector(avoidSelector);

        this.entity = creature;
        this.classToAvoid = avoid;;
        this.avoidDistance = distance;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.navigation = creature.getNavigator();
        this.setMutexBits(1);
    }


    private class AvoidSelector implements Predicate<T>{
        private final Predicate<? super T> parent;

        private AvoidSelector(Predicate<? super T> parent) {
            this.parent = parent;
        }

        @Override
        public boolean apply(@Nullable T entity) {
            if(!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()
                    && !((EntityPlayer)entity).isCreative()){

                if(entity.isEntityAlive() && EntityAIAvoidEntity.this.entity.getEntitySenses().canSee(entity)
                        && !EntityAIAvoidEntity.this.entity.isOnSameTeam(entity)){
                    return parent.apply(entity);
                }
            }
            return false;
        }
    }


    @Override
    public boolean shouldExecute() {
        List<T> list = this.entity.world.getEntitiesWithinAABB(this.classToAvoid,
                this.entity.getEntityBoundingBox().grow(this.avoidDistance, 3.0, this.avoidDistance),
                predicate
        );

        if (list.isEmpty()) {
            return false;
        } else {
            this.closestLivingEntity = list.get(0);
            Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 16, 7, new Vec3d(this.closestLivingEntity.posX, this.closestLivingEntity.posY, this.closestLivingEntity.posZ));
            if (vec == null) {
                return false;
            } else if (this.closestLivingEntity.getDistanceSq(vec.x, vec.y, vec.z) < this.closestLivingEntity.getDistanceSq(this.entity)) {
                return false;
            } else {
                this.path = this.navigation.getPathToXYZ(vec.x, vec.y, vec.z);
                return this.path != null;
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
