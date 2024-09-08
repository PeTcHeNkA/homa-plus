package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class VillagerTradeTrigger extends ConcurrentHashMap<PlayerAdvancements, VillagerTradeTrigger.Listeners> implements ICriterionTrigger<VillagerTradeTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("villager_trade");

    public VillagerTradeTrigger() {}

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
    public Instance deserializeInstance(JsonObject p_192166_1_, JsonDeserializationContext p_192166_2_) {
        EntityPredicate lvt_3_1_ = EntityPredicate.deserialize(p_192166_1_.get("villager"));
        ItemPredicate lvt_4_1_ = ItemPredicate.deserialize(p_192166_1_.get("item"));
        return new Instance(lvt_3_1_, lvt_4_1_);
    }


    public void trigger(EntityPlayerMP player, EntityVillager villager, ItemStack stack) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player, villager, stack);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners(PlayerAdvancements advancements) {
            super(8);
        }

        public void trigger(PlayerAdvancements adv, EntityPlayerMP player, EntityVillager villager, ItemStack stack) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var5 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var5.hasNext()) {
                listener = (ICriterionTrigger.Listener)var5.next();
                if (((Instance)listener.getCriterionInstance()).test(player, villager, stack)) {
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
        private final EntityPredicate villager;
        private final ItemPredicate item;

        public Instance(EntityPredicate villager, ItemPredicate item) {
            super(VillagerTradeTrigger.ID);
            this.villager = villager;
            this.item = item;
        }

        public boolean test(EntityPlayerMP player, EntityVillager villager, ItemStack stack) {
            if (!this.villager.test(player, villager)) {
                return false;
            } else {
                return this.item.test(stack);
            }
        }
    }
}