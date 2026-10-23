package plus.mobs.ai.model;

import plus.mobs.ai.part.EntityAIEditModel;
import plus.mobs.frame.AbcMob;
import plus.mobs.utl.LogicalExpression;


public class AiLogicalModel implements AbcMob.IEntityApplicator {
    private final LogicalExpression<AbcMob> exp;
    private final AbcMob.IEntityApplicator model;

    public AiLogicalModel(AbcMob.IEntityApplicator model, LogicalExpression<AbcMob> exp) {
        this.model = model;
        this.exp = exp;
    }

    @Override
    public void apply(AbcMob mob) {
        mob.tasks.addTask(0, new EntityAIEditModel(mob, exp, model));
    }
}