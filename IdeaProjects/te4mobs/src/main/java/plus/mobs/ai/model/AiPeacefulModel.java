package plus.mobs.ai.model;

import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import plus.mobs.frame.AbcMob;


public class AiPeacefulModel implements AbcMob.IEntityApplicator {
    @Override
    public void apply(AbcMob mob) {
        mob.tasks.addTask(0, new EntityAISwimming(mob));
        mob.tasks.addTask(1, new EntityAIPanic(mob, 2.0D));


        //mob.tasks.addTask(3, new EntityAITempt(mob, 1.25D, Items.WHEAT, false));


        mob.tasks.addTask(5, new EntityAIWanderAvoidWater(mob, 1.0D));
        mob.tasks.addTask(6, new EntityAIWatchClosest(mob, EntityPlayer.class, 6.0F));
        mob.tasks.addTask(7, new EntityAILookIdle(mob));
    }
}
