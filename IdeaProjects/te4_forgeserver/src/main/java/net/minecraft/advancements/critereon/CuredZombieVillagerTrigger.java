package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class CuredZombieVillagerTrigger extends ConcurrentHashMap<PlayerAdvancements, CuredZombieVillagerTrigger.Listeners> implements ICriterionTrigger<CuredZombieVillagerTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

    public CuredZombieVillagerTrigger() {
        super(3);
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
        EntityPredicate zombie = EntityPredicate.deserialize(jObj.get("zombie"));
        EntityPredicate villager = EntityPredicate.deserialize(jObj.get("villager"));
        return new Instance(zombie, villager);
    }


    public void trigger(EntityPlayerMP player, EntityZombie zombie, EntityVillager villager) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player, zombie, villager);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(4);
        }

        public void trigger(PlayerAdvancements adv, EntityPlayerMP player, EntityZombie zombie, EntityVillager villager) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var5 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var5.hasNext()) {
                listener = (ICriterionTrigger.Listener)var5.next();
                if (((Instance)listener.getCriterionInstance()).test(player, zombie, villager)) {
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
        private final EntityPredicate zombie;
        private final EntityPredicate villager;

        public Instance(EntityPredicate zombie, EntityPredicate villager) {
            super(CuredZombieVillagerTrigger.ID);
            this.zombie = zombie;
            this.villager = villager;
        }


        public boolean test(EntityPlayerMP player, EntityZombie zombie, EntityVillager villager) {
            if (!this.zombie.test(player, zombie)) {
                return false;
            } else {
                return this.villager.test(player, villager);
            }
        }
    }
}
