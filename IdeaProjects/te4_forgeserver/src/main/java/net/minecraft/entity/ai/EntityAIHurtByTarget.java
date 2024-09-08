package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.math.AxisAlignedBB;
import plus.fserver.utls.collections.ListAddAgent;


public class EntityAIHurtByTarget extends EntityAITarget {
    private final boolean entityCallsForHelp;
    private int revengeTimerOld;
    private final Class<?>[] excludedReinforcementTypes;

    public EntityAIHurtByTarget(EntityCreature creature, boolean callsHelp, Class<?>... excludes) {
        super(creature, true);
        this.entityCallsForHelp = callsHelp;
        this.excludedReinforcementTypes = excludes;
        this.setMutexBits(1);
    }


    @Override
    public boolean shouldExecute() {
        int timer = this.taskOwner.getRevengeTimer();
        EntityLivingBase lvt_2_1_ = this.taskOwner.getRevengeTarget();
        return timer != this.revengeTimerOld && lvt_2_1_ != null && this.isSuitableTarget(lvt_2_1_, false);
    }


    @Override
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.taskOwner.getRevengeTarget());
        this.target = this.taskOwner.getAttackTarget();
        this.revengeTimerOld = this.taskOwner.getRevengeTimer();
        this.unseenMemoryTicks = 300;
        if (this.entityCallsForHelp) {
            this.alertOthers();
        }
        super.startExecuting();
    }


    protected void alertOthers() {
        double distance = this.getTargetDistance();

        Agent agent = new Agent();

        EntityCreature taskOwner = this.taskOwner;
        AxisAlignedBB box = taskOwner.world.commonWorldEntityAABB2;
        box.initSafe(
                taskOwner.posX - distance,
                taskOwner.posY - 10,
                taskOwner.posZ - distance,
                taskOwner.posX + 1.0 + distance,
                taskOwner.posY + 11.0,
                taskOwner.posZ + 1.0 + distance
        );

        taskOwner.world.getEntitiesWithinAABBNoCache(agent, taskOwner.getClass(), box, null);
    }


    private class Agent implements ListAddAgent<EntityCreature>{
        @Override
        public boolean add(EntityCreature creature) {
            EntityCreature tOwn;
            if((tOwn = EntityAIHurtByTarget.this.taskOwner) == creature || creature.getAttackTarget() != null)return false;
            if(tOwn instanceof EntityTameable && ((EntityTameable)tOwn).getOwner() != ((EntityTameable)creature).getOwner())return false;
            if(creature.isOnSameTeam(tOwn.getRevengeTarget()))return false;

            for (Class<?> clazz : EntityAIHurtByTarget.this.excludedReinforcementTypes) {
                if (creature.getClass() == clazz) return false;
            }

            EntityAIHurtByTarget.this.setEntityAttackTarget(creature, tOwn.getRevengeTarget());
            return true;
        }
    }


    protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase target) {
        creature.setAttackTarget(target);
    }
}