package net.minecraft.entity.ai;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.AxisAlignedBB;


public class EntityAIFollowParent extends EntityAIBase {
    EntityAnimal childAnimal;
    EntityAnimal parentAnimal;
    double moveSpeed;
    private byte delayCounter;

    public EntityAIFollowParent(EntityAnimal child, double speed) {
        this.childAnimal = child;
        this.moveSpeed = speed;
    }


    @Override
    public boolean shouldExecute() {
        if (this.childAnimal.getGrowingAge() >= 0) {
            return false;
        } else {
            List<EntityAnimal> animals = this.childAnimal.world.getEntitiesWithinAABB(this.childAnimal.getClass(), getAABB(childAnimal));
            EntityAnimal lvt_2_1_ = null;
            double lvt_3_1_ = Double.MAX_VALUE;
            Iterator var5 = animals.iterator();

            while(var5.hasNext()) {
                EntityAnimal lvt_6_1_ = (EntityAnimal)var5.next();
                if (lvt_6_1_.getGrowingAge() >= 0) {
                    double lvt_7_1_ = this.childAnimal.getDistanceSq(lvt_6_1_);
                    if (!(lvt_7_1_ > lvt_3_1_)) {
                        lvt_3_1_ = lvt_7_1_;
                        lvt_2_1_ = lvt_6_1_;
                    }
                }
            }

            if (lvt_2_1_ == null) {
                return false;
            } else if (lvt_3_1_ < 9.0) {
                return false;
            } else {
                this.parentAnimal = lvt_2_1_;
                return true;
            }
        }
    }


    private static double round(double d){
        return ((long)d) & 0xFFFFFFFFFFFFFFFEL;
    }
    private static AxisAlignedBB getAABB(Entity entity){
        AxisAlignedBB aabb = entity.getEntityBoundingBox();
        return new AxisAlignedBB(
                round(aabb.minX - 8),
                round(aabb.minY - 8),
                aabb.minZ - 4,
                aabb.maxX + 4,
                round(aabb.maxY + 8),
                round(aabb.maxZ + 8)
        );
    }


    @Override
    public boolean shouldContinueExecuting() {
        if (this.childAnimal.getGrowingAge() >= 0) {
            return false;
        } else if (!this.parentAnimal.isEntityAlive()) {
            return false;
        } else {
            double distanceSq = this.childAnimal.getDistanceSq(this.parentAnimal);
            return !(distanceSq < 9.0) && !(distanceSq > 256.0);
        }
    }


    @Override
    public void startExecuting() {
        this.delayCounter = 0;
    }


    @Override
    public void resetTask() {
        this.parentAnimal = null;
    }


    @Override
    public void updateTask() {
        if (--this.delayCounter <= 0) {
            this.delayCounter = 10;
            this.childAnimal.getNavigator().tryMoveToEntityLiving(this.parentAnimal, this.moveSpeed);
        }
    }
}