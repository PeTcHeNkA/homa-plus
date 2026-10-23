package plus.mobs.ai.model;

import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import plus.mobs.frame.AbcMob;


public class AIAgressiveModel implements AbcMob.IEntityApplicator {
    @Override
    public void apply(AbcMob mob) {
         mob.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(
            mob, EntityPlayer.class, true));
    }
}