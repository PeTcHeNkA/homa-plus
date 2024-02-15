package net.minecraftforge.event.entity.living;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.EntityEvent;


public class LivingEvent extends EntityEvent {
    private EntityLivingBase entityLiving;

    public LivingEvent(EntityLivingBase entity) {
        super(entity);
        entityLiving = entity;
    }


    public EntityLivingBase getEntityLiving() {
        return entityLiving;
    }


    public LivingEvent initLiving(EntityLivingBase entity){
        setEntity(this.entityLiving = entity);
        return this;
    }


    public LivingEvent initLivingCancelable(EntityLivingBase entity){
        setEntity(this.entityLiving = entity);
        setCanceled(false);
        return this;
    }


    @Cancelable
    public static class LivingUpdateEvent extends LivingEvent {
        public LivingUpdateEvent(EntityLivingBase e){ super(e); }
    }


    public static class LivingJumpEvent extends LivingEvent {
        public LivingJumpEvent(EntityLivingBase e){ super(e); }
    }
}
