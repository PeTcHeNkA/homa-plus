package plus.mobs.ai.part;

import net.minecraft.entity.ai.EntityAIAttackMelee;
import plus.mobs.frame.AbcMob;


public class EntityAIAbcAttack extends EntityAIAttackMelee {
    private final AbcMob mob;
    private int ticks;

    public EntityAIAbcAttack(AbcMob mob, double speedIn, boolean useLongMemory) {
        super(mob, speedIn, useLongMemory);
        this.mob = mob;
    }

    public void startExecuting() {
        super.startExecuting();
        this.ticks = 0;
    }


    public void resetTask() {
        super.resetTask();
        clearState();
    }


    private void clearState(){
        this.mob.setState((byte) -1);
    }


    public void updateTask() {
        super.updateTask();
        ++this.ticks;

        if (this.ticks >= 5 && this.attackTick < 10)
        {
            this.mob.setState((byte) 2);
        }
        else {
            clearState();
        }
    }
}