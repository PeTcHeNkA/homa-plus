package net.minecraftforge.event.entity.living;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.ListenerList;


@Cancelable
public class LivingDeathEvent extends LivingEvent {
    private DamageSource source;

    public LivingDeathEvent(){super();}

    public LivingDeathEvent(EntityLivingBase entity, DamageSource source) {
        super(entity);
        this.source = source;
    }

    public void init(EntityLivingBase entity, DamageSource source){
        super.initLivingCancelable(entity);
        this.source = source;
    }


    public DamageSource getSource() {
        return this.source;
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