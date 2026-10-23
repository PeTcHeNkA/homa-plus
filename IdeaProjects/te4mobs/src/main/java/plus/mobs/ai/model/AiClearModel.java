package plus.mobs.ai.model;

import plus.mobs.frame.AbcMob;


public class AiClearModel implements AbcMob.IEntityApplicator {
    @Override
    public void apply(AbcMob mob) {
        mob.tasks.taskEntries.clear();
    }
}