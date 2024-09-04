package plus.fserver.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.Iterator;
import java.util.List;


public class EntityAIHurtByTargetSlim extends EntityAITarget {
    private int revengeTimerOld;

    public static EntityAIHurtByTargetSlim create(EntityCreature creature, boolean callsHelp){
        if(callsHelp)return new CallHelps(creature);
        return new EntityAIHurtByTargetSlim(creature);
    }

    public EntityAIHurtByTargetSlim(EntityCreature creature) {
        super(creature, true);
        this.setMutexBits(1);
    }

    public static class CallHelps extends EntityAIHurtByTargetSlim{
        public CallHelps(EntityCreature creature) {
            super(creature);
        }


        @Override
        protected void alertOthers() {
            double distance = this.getTargetDistance();
            List<EntityCreature> list = this.taskOwner.world.getEntitiesWithinAABB(this.taskOwner.getClass(), (new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ, this.taskOwner.posX + 1.0, this.taskOwner.posY + 1.0, this.taskOwner.posZ + 1.0)).grow(distance, 10.0, distance));
            Iterator<EntityCreature> var4 = list.iterator();

            while(true) {
                EntityCreature creature;
                do {
                    do {
                        do {
                            do {
                                if (!var4.hasNext()) {
                                    return;
                                }

                                creature = var4.next();
                            } while(this.taskOwner == creature);
                        } while(creature.getAttackTarget() != null);
                    } while(this.taskOwner instanceof EntityTameable && ((EntityTameable)this.taskOwner).getOwner() != ((EntityTameable)creature).getOwner());
                } while(creature.isOnSameTeam(this.taskOwner.getRevengeTarget()));

                this.setEntityAttackTarget(creature, this.taskOwner.getRevengeTarget());
            }
        }
    }


    @Override
    public boolean shouldExecute() {
        int timer = this.taskOwner.getRevengeTimer();
        EntityLivingBase target = this.taskOwner.getRevengeTarget();
        return timer != this.revengeTimerOld && target != null && this.isSuitableTarget(target, false);
    }


    @Override
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.taskOwner.getRevengeTarget());
        this.target = this.taskOwner.getAttackTarget();
        this.revengeTimerOld = this.taskOwner.getRevengeTimer();
        this.unseenMemoryTicks = 300;
        alertOthers();
        super.startExecuting();
    }


    protected void alertOthers() {}


    protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase target) {
        creature.setAttackTarget(target);
    }
}
