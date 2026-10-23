package plus.mobs.ai.part;

import net.minecraft.entity.ai.EntityAIBase;
import plus.mobs.frame.AbcMob;
import plus.mobs.utl.LogicalExpression;


public class EntityAIEditModel extends EntityAIBase{
    private final AbcMob mob;
    private final LogicalExpression<AbcMob> expression;
    private final AbcMob.IEntityApplicator model;

    public EntityAIEditModel(
            AbcMob mob, LogicalExpression<AbcMob> expression, AbcMob.IEntityApplicator model) {
        this.mob = mob;
        this.expression = expression;
        this.model = model;
    }


    @Override
    public boolean shouldExecute() {
        if(expression.check(mob)){
            mob.tasks.removeTask(this);
            model.apply(mob);
        }
        return false;
    }
}