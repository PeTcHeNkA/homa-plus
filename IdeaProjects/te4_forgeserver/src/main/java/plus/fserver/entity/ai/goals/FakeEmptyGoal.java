package plus.fserver.entity.ai.goals;

import net.minecraft.entity.ai.*;


public class FakeEmptyGoal extends EntityAIBase {
    public boolean shouldExecute() {
        return false;
    }
}