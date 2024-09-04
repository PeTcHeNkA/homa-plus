//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.entity.ai;

import net.minecraft.entity.passive.EntityVillager;
import plus.fserver.entity.ai.EntityAIWatchClosestPlayer;


public class EntityAILookAtTradePlayer extends EntityAIWatchClosestPlayer {
    private final EntityVillager villager;

    public EntityAILookAtTradePlayer(EntityVillager villager) {
        super(villager, 8.0F);
        this.villager = villager;
    }


    @Override
    public boolean shouldExecute() {
        if (this.villager.isTrading()) {
            this.closestEntity = this.villager.getCustomer();
            return true;
        } else {
            return false;
        }
    }
}
