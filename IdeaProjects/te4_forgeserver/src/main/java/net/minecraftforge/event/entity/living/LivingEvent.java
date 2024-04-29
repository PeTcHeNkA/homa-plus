package net.minecraftforge.event.entity.living;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;


public class LivingEvent extends EntityEvent {
    EntityLivingBase entityLiving;

    public LivingEvent(EntityLivingBase entity) {
        super(entity);
        entityLiving = entity;
    }


    public LivingEvent(){
        super();
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
    public static final class LivingUpdateEvent extends LivingEvent {
        IEventListener[] listeners;
        public LivingUpdateEvent forBus(EventBus bus){
            listeners = getListenerList().getListeners(bus.id());
            return this;
        }


        public boolean post(EntityLivingBase entity){
            setEntity(this.entityLiving = entity);
            setCanceled(false);
            for (IEventListener listener:listeners)listener.invoke(this);
            return isCanceled();
        }


        @Override
        public boolean isCancelable() {
            return true;
        }


        public LivingUpdateEvent(){ super(); }


        @Deprecated
        public LivingUpdateEvent(EntityLivingBase e){ super(e); }
    }


    public static class LivingJumpEvent extends LivingEvent {
        public LivingJumpEvent(EntityLivingBase e){ super(e); }


        public LivingJumpEvent(){}
    }
}
