package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class TickTrigger extends ConcurrentHashMap<PlayerAdvancements, TickTrigger.Listeners> implements ICriterionTrigger<TickTrigger.Instance> {
    public static final ResourceLocation ID = new ResourceLocation("tick");

    public TickTrigger() {
        super(7);
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public void addListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> p_192165_2_) {
        Listeners listeners = super.get(advancements);
        if (listeners == null) {
            listeners = new Listeners(advancements);
            super.put(advancements, listeners);
        }
        listeners.add(p_192165_2_);
    }


    @Override
    public void removeListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> p_192164_2_) {
        Listeners listeners = super.get(advancements);
        if (listeners != null) {
            listeners.remove(p_192164_2_);
            if (listeners.isEmpty()) {
                super.remove(advancements);
            }
        }
    }


    @Override
    public void removeAllListeners(PlayerAdvancements advancements) {
        super.remove(advancements);
    }


    private static Instance INSTANCE = new Instance();
    @Override
    public Instance deserializeInstance(JsonObject jsonObject, JsonDeserializationContext context) {
        return INSTANCE;
    }


    public void trigger(EntityPlayerMP player) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners(PlayerAdvancements advancements) {
            super(7);
        }


        public void trigger(PlayerAdvancements adv) {
            Iterator var1 = Lists.newArrayList(this).iterator();
            while(var1.hasNext()) {
                ICriterionTrigger.Listener<Instance> next = (ICriterionTrigger.Listener)var1.next();
                next.grantCriterion(adv);
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        public Instance() {
            super(TickTrigger.ID);
        }
    }
}