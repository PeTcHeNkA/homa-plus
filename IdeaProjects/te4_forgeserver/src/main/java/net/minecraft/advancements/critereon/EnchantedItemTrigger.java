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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class EnchantedItemTrigger extends ConcurrentHashMap<PlayerAdvancements, EnchantedItemTrigger.Listeners> implements ICriterionTrigger<EnchantedItemTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("enchanted_item");

    public EnchantedItemTrigger() {
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
        ItemPredicate item = ItemPredicate.deserialize(jObj.get("item"));
        MinMaxBounds levels = MinMaxBounds.deserialize(jObj.get("levels"));
        return new Instance(item, levels);
    }


    public void trigger(EntityPlayerMP player, ItemStack stack, int lvl) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, stack, lvl);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(4);
        }

        public void trigger(PlayerAdvancements adv, ItemStack stack, int lvl) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var4 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var4.hasNext()) {
                listener = (ICriterionTrigger.Listener)var4.next();
                if (((Instance)listener.getCriterionInstance()).test(stack, lvl)) {
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
        private final ItemPredicate item;
        private final MinMaxBounds levels;

        public Instance(ItemPredicate item, MinMaxBounds levels) {
            super(EnchantedItemTrigger.ID);
            this.item = item;
            this.levels = levels;
        }

        public boolean test(ItemStack stack, int lvl) {
            if (!this.item.test(stack)) {
                return false;
            } else {
                return this.levels.test((float)lvl);
            }
        }
    }
}