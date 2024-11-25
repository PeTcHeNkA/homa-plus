package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class EntityHurtPlayerTrigger extends ConcurrentHashMap<PlayerAdvancements, EntityHurtPlayerTrigger.Listeners> implements ICriterionTrigger<EntityHurtPlayerTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("entity_hurt_player");

    public EntityHurtPlayerTrigger() {
        super(7);
    }


    @Override //fix no-search field
    public boolean isEmpty(){return super.isEmpty();}


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
        DamagePredicate predicate = DamagePredicate.deserialize(jObj.get("damage"));
        return new Instance(predicate);
    }


    public void trigger(EntityPlayerMP player, DamageSource src, float f1, float f2, boolean p_192200_5_) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player, src, f1, f2, p_192200_5_);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(7);
        }

        public void remove(ICriterionTrigger.Listener<Instance> listener) {
            super.remove(listener);
        }


        public void trigger(PlayerAdvancements adv, EntityPlayerMP player, DamageSource src, float f1, float f2, boolean flag) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var7 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var7.hasNext()) {
                listener = (ICriterionTrigger.Listener)var7.next();
                if (((Instance)listener.getCriterionInstance()).test(player, src, f1, f2, flag)) {
                    if (listeners == null) {
                        listeners = Lists.newArrayList();
                    }

                    listeners.add(listener);
                }
            }

            if (listeners != null) {
                var7 = listeners.iterator();

                while(var7.hasNext()) {
                    listener = (ICriterionTrigger.Listener)var7.next();
                    listener.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final DamagePredicate damage;

        public Instance(DamagePredicate predicate) {
            super(EntityHurtPlayerTrigger.ID);
            this.damage = predicate;
        }

        public boolean test(EntityPlayerMP player, DamageSource src, float f1, float f2, boolean flag) {
            return this.damage.test(player, src, f1, f2, flag);
        }
    }
}