package plus.mobs.ai.model;

import plus.mobs.ai.part.EntityAIEditModel;
import plus.mobs.frame.AbcMob;
import plus.mobs.utl.LogicalExpression;


public class AiLowHealthModel implements AbcMob.IEntityApplicator {
    private final AbcMob.IEntityApplicator model;
    private final float minHpPercent;
    public AiLowHealthModel(AbcMob.IEntityApplicator model, float minHpPercent) {
        this.model = model;
        this.minHpPercent = minHpPercent;
    }

    @Override
    public void apply(AbcMob mob) {
        mob.tasks.addTask(0,
                new EntityAIEditModel(mob, new LogicalExpression<AbcMob>() {
                    final float lowHp = (float) (mob.getMaxHealth()*minHpPercent);
                    @Override
                    public boolean check(AbcMob pbj) {
                        return pbj.getHealth()<lowHp;
                    }
                }, model)
        );
    }
}