package plus.fserver.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;


public class EntityAIWatchClosestPlayer extends EntityAIBase {
    protected EntityLiving entity;
    protected Entity closestEntity;
    protected float maxDistance;
    private byte lookTime;
    private final float chance;

    public EntityAIWatchClosestPlayer(EntityLiving entity, float distance) {
        this.entity = entity;
        this.maxDistance = distance;
        this.chance = 0.02F;
        this.setMutexBits(2);
    }

    public EntityAIWatchClosestPlayer(EntityLiving entity, float distance, float chance) {
        this.entity = entity;
        this.maxDistance = distance;
        this.chance = chance;
        this.setMutexBits(2);
    }


    @Override
    public boolean shouldExecute() {
        if (this.entity.getRNG().nextFloat() >= this.chance) {
            return false;
        } else {
            EntityLiving entity = this.entity;
            if (entity.getAttackTarget() != null) {
                closestEntity = entity.getAttackTarget();
            }
            if(closestEntity == null) {
                this.closestEntity = entity.world.getClosestPlayer(entity.posX, entity.posY, entity.posZ, this.maxDistance, this::canWath);
            }
            return closestEntity != null;
        }
    }


    private boolean canWath(Entity entity){
        if(entity instanceof EntityPlayer){
            if(((EntityPlayer)entity).isSpectator())return false;
        }
        final Entity src = this.entity;
        while (true) {
            if (entity.isRiding()) {
                entity = entity.getRidingEntity();
                if (entity != src) {
                    continue;
                }
                return false;
            }
            return true;
        }
    }


    @Override
    public boolean shouldContinueExecuting() {
        if (!this.closestEntity.isEntityAlive()) {
            return false;
        } else if (this.entity.getDistanceSq(this.closestEntity) > (double)(this.maxDistance * this.maxDistance)) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }


    @Override
    public void startExecuting() {
        this.lookTime = (byte) (40 + this.entity.getRNG().nextInt(40));
    }


    @Override
    public void resetTask() {
        this.closestEntity = null;
    }


    @Override
    public void updateTask() {
        this.entity.getLookHelper().setLookPosition(this.closestEntity.posX, this.closestEntity.posY + (double)this.closestEntity.getEyeHeight(), this.closestEntity.posZ, (float)this.entity.getHorizontalFaceSpeed(), (float)this.entity.getVerticalFaceSpeed());
        --this.lookTime;
    }
}