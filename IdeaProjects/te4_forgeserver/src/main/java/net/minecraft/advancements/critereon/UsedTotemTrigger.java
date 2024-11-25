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


public class UsedTotemTrigger extends ConcurrentHashMap<PlayerAdvancements, UsedTotemTrigger.Listeners> implements ICriterionTrigger<UsedTotemTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("used_totem");

    public UsedTotemTrigger() {}


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
        ItemPredicate lvt_3_1_ = ItemPredicate.deserialize(jObj.get("item"));
        return new Instance(lvt_3_1_);
    }


    public void trigger(EntityPlayerMP player, ItemStack stack) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, stack);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(3);
        }


        @Override //fix no-search field
        public boolean isEmpty(){return super.isEmpty();}


        public void trigger(PlayerAdvancements adv, ItemStack stack) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var3 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var3.hasNext()) {
                listener = (ICriterionTrigger.Listener)var3.next();
                if (((Instance)listener.getCriterionInstance()).test(stack)) {
                    if (listeners == null) {
                        listeners = Lists.newArrayList();
                    }

                    listeners.add(listener);
                }
            }

            if (listeners != null) {
                var3 = listeners.iterator();

                while(var3.hasNext()) {
                    listener = (ICriterionTrigger.Listener)var3.next();
                    listener.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final ItemPredicate item;

        public Instance(ItemPredicate item) {
            super(UsedTotemTrigger.ID);
            this.item = item;
        }

        public boolean test(ItemStack stack) {
            return this.item.test(stack);
        }
    }
}
