package net.minecraftforge.event.entity.living;

import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraft.entity.*;


@Event.HasResult
public class LivingPackSizeEvent extends LivingEvent {
    private int maxPackSize;

    public LivingPackSizeEvent(final EntityLiving entity) {
        super(entity);
    }

    public LivingPackSizeEvent(){}


    public int getMaxPackSize() {
        return this.maxPackSize;
    }


    @Override
    public boolean hasResult() {
        return true;
    }


    public EventProxy forBus(EventBus bus){
        return new EventProxy(getListenerList().getListeners(bus.id()), this);
    }


    public void setMaxPackSize(final int maxPackSize) {
        this.maxPackSize = maxPackSize;
    }


    public static class EventProxy {
        private final IEventListener[] listeners;
        private final LivingPackSizeEvent event;

        public EventProxy(IEventListener[] listeners, LivingPackSizeEvent event) {
            this.listeners = listeners;
            this.event = event;
        }


        public int post(final EntityLiving entity){
            final LivingPackSizeEvent event = this.event;
            event.initLiving(entity).setResult(Event.Result.DEFAULT);
            event.maxPackSize = 0;

            for (IEventListener listener:listeners)listener.invoke(event);

            if(event.getResult() == Event.Result.ALLOW)return event.getMaxPackSize();
            return entity.getMaxSpawnedInChunk();
        }
    }


    //Elca-Forge event system
    //Start generated code
    private static ListenerList LISTENER_LIST;
    protected void setup() {
        super.setup();
        if (LISTENER_LIST == null)
            LISTENER_LIST = new ListenerList(super.getListenerList());
    }
    public ListenerList getListenerList() {
        return LISTENER_LIST;
    }
    //End generated code
}