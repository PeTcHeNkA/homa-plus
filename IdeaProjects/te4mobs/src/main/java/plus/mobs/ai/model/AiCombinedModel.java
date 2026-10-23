package plus.mobs.ai.model;

import plus.mobs.frame.AbcMob;


public class AiCombinedModel implements AbcMob.IEntityApplicator {
    private final AbcMob.IEntityApplicator model1, model2;

    public AiCombinedModel(AbcMob.IEntityApplicator model1, AbcMob.IEntityApplicator model2) {
        this.model1 = model1;
        this.model2 = model2;
    }


    @Override
    public void apply(AbcMob mob) {
        model1.apply(mob);
        model2.apply(mob);
    }
}