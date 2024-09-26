package plus.fserver.entity.ai;

import java.util.*;
import net.minecraft.entity.ai.*;
import plus.fserver.entity.ai.goals.CustomPriotizedSlowedGoal;
import plus.fserver.ElcaConfig;
import java.util.function.*;


public class CustomGoalTypeData {
    private final IdentityHashMap<Class<? extends EntityAIBase>, CustomGoalTypeParameter> GOAL_TYPES = new IdentityHashMap<>();
    
    public CustomGoalTypeData() {
        this.GOAL_TYPES.put(EntityAISwimming.class, new CustomGoalTypeParameter(g -> g, 5, 1 , ElcaConfig.SLOW_SWIM_AI   ));
        this.GOAL_TYPES.put(EntityAITempt.class   , new CustomGoalTypeParameter(g -> g, 4, 10, ElcaConfig.SLOW_TEMP_CHECK));
        this.GOAL_TYPES.put(EntityAIWander.class  , new CustomGoalTypeParameter(g -> g, 4, 1 , ElcaConfig.SLOW_WANDER    ));
    }


    public CustomPriotizedSlowedGoal getPriotizedGoalFor(final int priority, final EntityAIBase goal) {
        final CustomGoalTypeParameter params = this.GOAL_TYPES.get(goal.getClass());
        if (params != null && params.isEnabled) {
            return new CustomPriotizedSlowedGoal(priority, params.goalConverter.apply(goal), params.shouldExecuteRate, params.tickRate);
        }
        return new CustomPriotizedSlowedGoal(priority, goal, 1, 1);
    }


    public static class CustomGoalTypeParameter {
        public final Function<EntityAIBase, EntityAIBase> goalConverter;
        public final int shouldExecuteRate;
        public final int tickRate;
        public final boolean isEnabled;
        
        private CustomGoalTypeParameter(final Function<EntityAIBase, EntityAIBase> goalConverter, final int shouldExecuteRate, final int tickRate, final boolean isEnabled) {
            this.goalConverter = goalConverter;
            this.shouldExecuteRate = shouldExecuteRate;
            this.tickRate = tickRate;
            this.isEnabled = isEnabled;
        }
    }
}
