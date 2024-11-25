package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class BredAnimalsTrigger extends ConcurrentHashMap<PlayerAdvancements, BredAnimalsTrigger.Listeners> implements ICriterionTrigger<BredAnimalsTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("bred_animals");

    public BredAnimalsTrigger() {
        super(7);
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public void addListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> listener) {
        Listeners listeners = super.get(advancements);
        if (listeners == null) {
            listeners = new Listeners();
            super.put(advancements, listeners);
        }
        listeners.add(listener);
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
    public Instance deserializeInstance(JsonObject jObj, JsonDeserializationContext ctx) {
        EntityPredicate parent = EntityPredicate.deserialize(jObj.get("parent"));
        EntityPredicate partner = EntityPredicate.deserialize(jObj.get("partner"));
        EntityPredicate child = EntityPredicate.deserialize(jObj.get("child"));
        return new Instance(parent, partner, child);
    }


    public void trigger(EntityPlayerMP player, EntityAnimal parent, EntityAnimal partner, EntityAgeable child) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player, parent, partner, child);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(3);
        }


        @Override //fix no-search field
        public boolean isEmpty(){return super.isEmpty();}


        public void trigger(PlayerAdvancements adv, EntityPlayerMP player, EntityAnimal parent, EntityAnimal partner, EntityAgeable child) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var6 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var6.hasNext()) {
                listener = (ICriterionTrigger.Listener)var6.next();
                if (((Instance)listener.getCriterionInstance()).test(player, parent, partner, child)) {
                    if (listeners == null) {
                        listeners = Lists.newArrayList();
                    }

                    listeners.add(listener);
                }
            }

            if (listeners != null) {
                var6 = listeners.iterator();

                while(var6.hasNext()) {
                    listener = (ICriterionTrigger.Listener)var6.next();
                    listener.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final EntityPredicate parent;
        private final EntityPredicate partner;
        private final EntityPredicate child;

        public Instance(EntityPredicate parent, EntityPredicate partner, EntityPredicate child) {
            super(BredAnimalsTrigger.ID);
            this.parent = parent;
            this.partner = partner;
            this.child = child;
        }


        public boolean test(EntityPlayerMP player, EntityAnimal parent, EntityAnimal partner, EntityAgeable child) {
            if (!this.child.test(player, child)) {
                return false;
            } else {
                return this.parent.test(player, parent) && this.partner.test(player, partner) || this.parent.test(player, partner) && this.partner.test(player, parent);
            }
        }
    }
}