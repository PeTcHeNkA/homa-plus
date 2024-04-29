package net.minecraftforge.event.entity.living;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityLivingBase;


@Cancelable
public class LivingAttackEvent extends LivingEvent {
    private DamageSource source;
    private float amount;

    public LivingAttackEvent(EntityLivingBase entity, DamageSource source, float amount) {
        super(entity);
        this.source = source;
        this.amount = amount;
    }


    public void setAmount(float amount){
        this.amount = amount;
    }


    public LivingAttackEvent initAttack(EntityLivingBase entity, DamageSource source, float amount){
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


    @Override
    public boolean isCancelable() {
        return true;
    }
}