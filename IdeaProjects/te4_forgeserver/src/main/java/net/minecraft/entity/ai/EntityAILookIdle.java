package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;


public class EntityAILookIdle extends EntityAIBase {
    private final EntityLiving idleEntity;
    private float lookX, lookZ;
    private byte idleTime;

    public EntityAILookIdle(EntityLiving entity) {
        this.idleEntity = entity;
        this.setMutexBits(3);
    }


    @Override
    public boolean shouldExecute() {
        return this.idleEntity.getRNG().nextFloat() < 0.02F;
    }


    @Override
    public boolean shouldContinueExecuting() {
        return this.idleTime >= 0;
    }


    @Override
    public void startExecuting() {
        double rand = 6.283185307179586 * this.idleEntity.getRNG().nextDouble();
        this.lookX = (float) Math.cos(rand);
        this.lookZ = (float) Math.sin(rand);
        this.idleTime = (byte) (20 + this.idleEntity.getRNG().nextInt(20));
    }


    @Override
    public void updateTask() {
        --this.idleTime;
        this.idleEntity.getLookHelper().setLookPosition(this.idleEntity.posX + this.lookX, this.idleEntity.posY + (double)this.idleEntity.getEyeHeight(), this.idleEntity.posZ + this.lookZ, (float)this.idleEntity.getHorizontalFaceSpeed(), (float)this.idleEntity.getVerticalFaceSpeed());
    }
}