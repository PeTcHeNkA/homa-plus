package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class TameAnimalTrigger extends ConcurrentHashMap<PlayerAdvancements, TameAnimalTrigger.Listeners> implements ICriterionTrigger<TameAnimalTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("tame_animal");

    public TameAnimalTrigger() {
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
            listeners = new Listeners(advancements);
            super.put(advancements, listeners);
        }

        listeners.add(listener);
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
        EntityPredicate predicate = EntityPredicate.deserialize(jObj.get("entity"));
        return new Instance(predicate);
    }


    public void trigger(EntityPlayerMP player, EntityAnimal animal) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player, animal);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners(PlayerAdvancements advancements) {
            super(7);
        }


        @Override //fix no-search field
        public boolean isEmpty(){return super.isEmpty();}


        public void trigger(PlayerAdvancements adv, EntityPlayerMP player, EntityAnimal animal) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var4 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var4.hasNext()) {
                listener = (ICriterionTrigger.Listener)var4.next();
                if (((Instance)listener.getCriterionInstance()).test(player, animal)) {
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
            super(TameAnimalTrigger.ID);
            this.entity = predicate;
        }

        public boolean test(EntityPlayerMP player, EntityAnimal animal) {
            return this.entity.test(player, animal);
        }
    }
}