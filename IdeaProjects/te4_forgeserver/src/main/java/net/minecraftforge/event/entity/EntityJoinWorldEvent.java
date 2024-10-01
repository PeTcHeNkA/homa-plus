package net.minecraftforge.event.entity;

import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraft.world.*;
import net.minecraft.entity.*;


@Cancelable
public class EntityJoinWorldEvent extends EntityEvent {
    private World world;

    public EntityJoinWorldEvent(){}

    public EntityJoinWorldEvent(final Entity entity, final World world) {
        super(entity);
        this.world = world;
    }


    public void initJoinEvent(final Entity entity, final World world){
        super.setEntity(entity);
        super.setCanceled(false);
        this.world = world;
    }


    public World getWorld() {
        return this.world;
    }


    @Override
    public boolean isCancelable() {
        return true;
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