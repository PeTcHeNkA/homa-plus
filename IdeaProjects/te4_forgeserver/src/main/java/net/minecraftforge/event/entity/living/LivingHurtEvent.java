package net.minecraftforge.event.entity.living;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.ListenerList;


@Cancelable
public class LivingHurtEvent extends LivingEvent {
    private DamageSource source;
    private float amount;

    public LivingHurtEvent(){super();}


    public LivingHurtEvent(EntityLivingBase entity, DamageSource source, float amount) {
        super(entity);
        this.source = source;
        this.amount = amount;
    }

    public LivingHurtEvent initHurt(EntityLivingBase entity, DamageSource source, float amount){
        super.initLivingCancelable(entity);
        this.source = source;
        this.amount = amount;
        return this;
    }


    public DamageSource getSource() {
        return source;
    }


    public float getAmount() {
        return amount;
    }


    public void setAmount(float amount) {
        this.amount = amount;
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
