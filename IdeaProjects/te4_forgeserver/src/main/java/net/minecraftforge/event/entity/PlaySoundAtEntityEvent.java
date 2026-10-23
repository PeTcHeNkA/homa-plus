package net.minecraftforge.event.entity;

import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraft.util.*;
import net.minecraft.entity.*;


@Cancelable
public class PlaySoundAtEntityEvent extends EntityEvent {
    private SoundEvent name;
    private SoundCategory category;
    private float volume;
    private float pitch;
    private float newVolume;
    private float newPitch;

    public PlaySoundAtEntityEvent(){super();}

    public PlaySoundAtEntityEvent(final Entity entity, final SoundEvent name, final SoundCategory category, final float volume, final float pitch) {
        super(entity);
        this.name = name;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.newVolume = volume;
        this.newPitch = pitch;
    }


    public void initPlaySound(final Entity entity, final SoundEvent name, final SoundCategory category, final float volume, final float pitch){
        super.setEntity(entity);
        this.name = name;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.newVolume = volume;
        this.newPitch = pitch;
        setCanceled(false);
    }


    @Override
    public boolean isCancelable() {
        return true;
    }


    public SoundEvent getSound() {
        return this.name;
    }


    public SoundCategory getCategory() {
        return this.category;
    }


    public float getDefaultVolume() {
        return this.volume;
    }


    public float getDefaultPitch() {
        return this.pitch;
    }


    public float getVolume() {
        return this.newVolume;
    }


    public float getPitch() {
        return this.newPitch;
    }


    public void setSound(final SoundEvent value) {
        this.name = value;
    }


    public void setCategory(final SoundCategory category) {
        this.category = category;
    }


    public void setVolume(final float value) {
        this.newVolume = value;
    }


    public void setPitch(final float value) {
        this.newPitch = value;
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