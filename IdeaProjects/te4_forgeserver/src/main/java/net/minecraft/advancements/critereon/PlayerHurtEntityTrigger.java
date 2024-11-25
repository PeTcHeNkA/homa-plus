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


public class PlayerHurtEntityTrigger extends ConcurrentHashMap<PlayerAdvancements, PlayerHurtEntityTrigger.Listeners> implements ICriterionTrigger<PlayerHurtEntityTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

    public PlayerHurtEntityTrigger() {}


    @Override //fix no-search field
    public boolean isEmpty(){return super.isEmpty();}


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
        DamagePredicate dPredicate = DamagePredicate.deserialize(jObj.get("damage"));
        EntityPredicate ePredicate = EntityPredicate.deserialize(jObj.get("entity"));
        return new Instance(dPredicate, ePredicate);
    }


    public void trigger(EntityPlayerMP player, Entity entity, DamageSource src, float p_192220_4_, float p_192220_5_, boolean p_192220_6_) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player, entity, src, p_192220_4_, p_192220_5_, p_192220_6_);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners(PlayerAdvancements advancements) {
            super(7);
        }


        public void trigger(PlayerAdvancements adv, EntityPlayerMP player, Entity entity, DamageSource src, float f1, float f2, boolean flag) {
            List<ICriterionTrigger.Listener<Instance>> list = null;
            Iterator var8 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var8.hasNext()) {
                listener = (ICriterionTrigger.Listener)var8.next();
                if (((Instance)listener.getCriterionInstance()).test(player, entity, src, f1, f2, flag)) {
                    if (list == null) {
                        list = Lists.newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                var8 = list.iterator();

                while(var8.hasNext()) {
                    listener = (ICriterionTrigger.Listener)var8.next();
                    listener.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final DamagePredicate damage;
        private final EntityPredicate entity;

        public Instance(DamagePredicate dPredicate, EntityPredicate ePredicate) {
            super(PlayerHurtEntityTrigger.ID);
            this.damage = dPredicate;
            this.entity = ePredicate;
        }

        public boolean test(EntityPlayerMP player, Entity entity, DamageSource src, float f1, float f2, boolean flag) {
            if (!this.damage.test(player, src, f1, f2, flag)) {
                return false;
            } else {
                return this.entity.test(player, entity);
            }
        }
    }
}