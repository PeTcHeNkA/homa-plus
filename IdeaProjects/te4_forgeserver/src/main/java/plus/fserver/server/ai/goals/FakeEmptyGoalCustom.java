package plus.fserver.server.ai.goals;

import net.minecraft.entity.ai.*;


public class FakeEmptyGoalCustom extends EntityAIBase {
    public boolean shouldExecute() {
        return false;
    }
}