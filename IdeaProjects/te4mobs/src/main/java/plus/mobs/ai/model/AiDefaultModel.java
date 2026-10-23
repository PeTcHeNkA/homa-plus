package plus.mobs.ai.model;

import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import plus.mobs.ai.part.EntityAIAbcAttack;
import plus.mobs.frame.AbcMob;


public class AiDefaultModel implements AbcMob.IEntityApplicator {
    private final boolean longMemory;



    public AiDefaultModel(boolean longMemory) {
        this.longMemory = longMemory;
    }

    @Override
    public void apply(AbcMob mob) {
        mob.tasks.addTask(0, new EntityAISwimming(mob));
        mob.tasks.addTask(3, new EntityAIAbcAttack(mob, 1.0D, false));
        mob.tasks.addTask(5, new EntityAIMoveTowardsRestriction(mob, 1.0D));
        mob.tasks.addTask(8, new EntityAIWanderAvoidWater(mob, 1.0D));
        mob.tasks.addTask(9, new EntityAIWatchClosest(mob, EntityPlayer.class, 8.0F));
        mob.tasks.addTask(9, new EntityAILookIdle(mob));

        mob.tasks.addTask(7, new EntityAIMoveThroughVillage(
                mob, 1.0D, false));

        mob.targetTasks.addTask(2, new EntityAIHurtByTarget(
                mob, true, EntityPlayer.class));
    }
}