package net.minecraftforge.event.entity.living;

import net.minecraft.entity.*;
import net.minecraftforge.fml.common.eventhandler.ListenerList;


public class LivingSetAttackTargetEvent extends LivingEvent {
    private EntityLivingBase target;

    public LivingSetAttackTargetEvent(final EntityLivingBase entity, final EntityLivingBase target) {
        super(entity);
        this.target = target;
    }


    public void init(final EntityLivingBase entity, final EntityLivingBase target){
        super.setEntity(entity);
        this.target = target;
    }


    public EntityLivingBase getTarget() {
        return this.target;
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