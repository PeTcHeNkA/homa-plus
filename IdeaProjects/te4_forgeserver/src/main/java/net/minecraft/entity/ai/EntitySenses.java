package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import plus.fserver.utls.collections.EntityList;


public class EntitySenses extends EntityList<Entity> {
    private final EntityLiving entity;
    private final EntityList<Entity> unseenEntities = new EntityList<>();
    private boolean flag = true;

    public EntitySenses(EntityLiving entityIn) {
        this.entity = entityIn;
    }


    public void clearSensingCache() {
        if(flag) {
            unseenEntities.fclear();
            flag = false;
        } else {
            clear();
            unseenEntities.clearAll();
            flag = true;
        }
    }


    public boolean canSee(Entity entityIn) {
        if (containsRef(entityIn)) return true;
        if (unseenEntities.containsRef(entityIn)) return false;

        if (entity.canEntityBeSeen(entityIn)) {
            addF(entityIn);
            return true;
        } else {
            unseenEntities.addF(entityIn);
            return false;
        }
    }
}