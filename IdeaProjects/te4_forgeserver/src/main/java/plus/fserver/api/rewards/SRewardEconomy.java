package plus.fserver.api.rewards;

import net.minecraft.entity.player.EntityPlayerMP;
import plus.fserver.api.economy.EconomyEngine;
import plus.fserver.api.offline.ISafeTask;
import plus.fserver.api.offline.TaskType;


public class SRewardEconomy implements ISafeTask {
    private final EconomyEngine engine;
    private final long amount;

    public SRewardEconomy(String id, long amount){
        engine = EconomyEngine.byId(id);
        this.amount = amount;
    }


    @Override
    public TaskType type() {
        return TaskType.SMART;
    }


    @Override
    public void accept(EntityPlayerMP playerMP) {
        engine.addPlayerBalance(playerMP, amount);
    }
}
