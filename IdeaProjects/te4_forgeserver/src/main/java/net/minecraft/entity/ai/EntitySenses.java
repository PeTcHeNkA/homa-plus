package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import plus.fserver.utls.collections.ArrayListE6;


public class EntitySenses {
    private final EntityLiving entity;
    private final ArrayListE6<Entity> seenEntities   = new ArrayListE6<>();
    private final ArrayListE6<Entity> unseenEntities = new ArrayListE6<>();
    private boolean flag = true;

    public EntitySenses(EntityLiving entityIn) {
        this.entity = entityIn;
    }

    public void clearSensingCache() {
        if(flag) {
            unseenEntities.fclear();
            flag = false;
        } else {
            seenEntities.clear();
            unseenEntities.clearAll();
            flag = true;
        }
    }


    public boolean canSee(Entity entityIn) {
        if (this.seenEntities.containsRef(entityIn)) {
            return true;
        }
        else if (this.unseenEntities.containsRef(entityIn)) {
            return false;
        } else {
            boolean flag = this.entity.canEntityBeSeen(entityIn);

            if (flag) {
                this.seenEntities.addF(entityIn);
            } else {
                this.unseenEntities.addF(entityIn);
            }
            return flag;
        }
    }
}