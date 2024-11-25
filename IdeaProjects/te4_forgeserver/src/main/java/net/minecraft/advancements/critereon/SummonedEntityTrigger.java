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
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class SummonedEntityTrigger extends ConcurrentHashMap<PlayerAdvancements, SummonedEntityTrigger.Listeners> implements ICriterionTrigger<SummonedEntityTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("summoned_entity");

    public SummonedEntityTrigger() {}

    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public void addListener(PlayerAdvancements p_192165_1_, ICriterionTrigger.Listener<Instance> p_192165_2_) {
        Listeners listeners = super.get(p_192165_1_);
        if (listeners == null) {
            listeners = new Listeners();
            super.put(p_192165_1_, listeners);
        }

        listeners.add(p_192165_2_);
    }


    @Override
    public void removeListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> listener) {
        Listeners listeners = super.get(advancements);
        if (listeners != null) {
            listeners.remove(listener);
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
    public Instance deserializeInstance(JsonObject jObj, JsonDeserializationContext p_192166_2_) {
        EntityPredicate predicate = EntityPredicate.deserialize(jObj.get("entity"));
        return new Instance(predicate);
    }


    public void trigger(EntityPlayerMP player, Entity entity) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player, entity);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(7);
        }


        @Override //fix no-search field
        public boolean isEmpty(){return super.isEmpty();}


        public void trigger(PlayerAdvancements adv, EntityPlayerMP player, Entity entity) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var4 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var4.hasNext()) {
                listener = (ICriterionTrigger.Listener)var4.next();
                if (((Instance)listener.getCriterionInstance()).test(player, entity)) {
                    if (listeners == null) {
                        listeners = Lists.newArrayList();
                    }

                    listeners.add(listener);
                }
            }

            if (listeners != null) {
                var4 = listeners.iterator();

                while(var4.hasNext()) {
                    listener = (ICriterionTrigger.Listener)var4.next();
                    listener.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final EntityPredicate entity;

        public Instance(EntityPredicate predicate) {
            super(SummonedEntityTrigger.ID);
            this.entity = predicate;
        }

        public boolean test(EntityPlayerMP player, Entity entity) {
            return this.entity.test(player, entity);
        }
    }
}
