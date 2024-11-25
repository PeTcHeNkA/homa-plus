package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class KilledTrigger extends ConcurrentHashMap<PlayerAdvancements, KilledTrigger.Listeners> implements ICriterionTrigger<KilledTrigger.Instance> {
    private final ResourceLocation id;

    public KilledTrigger(ResourceLocation id) {
        this.id = id;
    }


    @Override
    public ResourceLocation getId() {
        return this.id;
    }


    @Override
    public void addListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> p_192165_2_) {
        Listeners listeners = super.get(advancements);
        if (listeners == null) {
            listeners = new Listeners();
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


    @Override
    public Instance deserializeInstance(JsonObject jObj, JsonDeserializationContext ctx) {
        return new Instance(this.id, EntityPredicate.deserialize(jObj.get("entity")), DamageSourcePredicate.deserialize(jObj.get("killing_blow")));
    }


    public void trigger(EntityPlayerMP player, Entity entity, DamageSource src) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player, entity, src);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(7);
        }


        @Override //fix no-search field
        public boolean isEmpty(){return super.isEmpty();}


        public void trigger(PlayerAdvancements adv, EntityPlayerMP player, Entity entity, DamageSource src) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var5 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var5.hasNext()) {
                listener = (ICriterionTrigger.Listener)var5.next();
                if (((Instance)listener.getCriterionInstance()).test(player, entity, src)) {
                    if (listeners == null) {
                        listeners = Lists.newArrayList();
                    }

                    listeners.add(listener);
                }
            }

            if (listeners != null) {
                var5 = listeners.iterator();

                while(var5.hasNext()) {
                    listener = (ICriterionTrigger.Listener)var5.next();
                    listener.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final EntityPredicate entity;
        private final DamageSourcePredicate killingBlow;

        public Instance(ResourceLocation location, EntityPredicate entity, DamageSourcePredicate kb) {
            super(location);
            this.entity = entity;
            this.killingBlow = kb;
        }

        public boolean test(EntityPlayerMP player, Entity entity, DamageSource src) {
            return !this.killingBlow.test(player, src) ? false : this.entity.test(player, entity);
        }
    }
}
