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


public class ItemDurabilityTrigger extends ConcurrentHashMap<PlayerAdvancements, ItemDurabilityTrigger.Listeners> implements ICriterionTrigger<ItemDurabilityTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("item_durability_changed");

    public ItemDurabilityTrigger() {}

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
        MinMaxBounds durability = MinMaxBounds.deserialize(jObj.get("durability"));
        MinMaxBounds delta = MinMaxBounds.deserialize(jObj.get("delta"));
        return new Instance(item, durability, delta);
    }



    public void trigger(EntityPlayerMP player, ItemStack stack, int amount) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, stack, amount);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(7);
        }


        @Override //fix no-search field
        public boolean isEmpty(){return super.isEmpty();}


        public void trigger(PlayerAdvancements adv, ItemStack stack, int amount) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var4 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var4.hasNext()) {
                listener = (ICriterionTrigger.Listener)var4.next();
                if (((Instance)listener.getCriterionInstance()).test(stack, amount)) {
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
        private final MinMaxBounds durability;
        private final MinMaxBounds delta;

        public Instance(ItemPredicate item, MinMaxBounds durability, MinMaxBounds delta) {
            super(ItemDurabilityTrigger.ID);
            this.item = item;
            this.durability = durability;
            this.delta = delta;
        }

        public boolean test(ItemStack stack, int amount) {
            if (!this.item.test(stack)) {
                return false;
            } else if (!this.durability.test((float)(stack.getMaxDamage() - amount))) {
                return false;
            } else {
                return this.delta.test((float)(stack.getItemDamage() - amount));
            }
        }
    }
}