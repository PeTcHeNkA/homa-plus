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


public class ConsumeItemTrigger extends ConcurrentHashMap<PlayerAdvancements, ConsumeItemTrigger.Listeners> implements ICriterionTrigger<ConsumeItemTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("consume_item");

    public ConsumeItemTrigger() {}

    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public void addListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> p_192165_2_) {
        Listeners listeners = super.get(advancements);
        if (listeners == null) {
            listeners = new Listeners();
            super.put(advancements, listeners);
        }
        listeners.add(p_192165_2_);
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
        ItemPredicate predicate = ItemPredicate.deserialize(jObj.get("item"));
        return new Instance(predicate);
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
            super(7);
        }


        @Override //fix no-search field
        public boolean isEmpty(){return super.isEmpty();}


        public void trigger(PlayerAdvancements adv, ItemStack stack) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var3 = super.iterator();

            ICriterionTrigger.Listener lvt_4_2_;
            while(var3.hasNext()) {
                lvt_4_2_ = (ICriterionTrigger.Listener)var3.next();
                if (((Instance)lvt_4_2_.getCriterionInstance()).test(stack)) {
                    if (listeners == null) {
                        listeners = Lists.newArrayList();
                    }

                    listeners.add(lvt_4_2_);
                }
            }

            if (listeners != null) {
                var3 = listeners.iterator();

                while(var3.hasNext()) {
                    lvt_4_2_ = (ICriterionTrigger.Listener)var3.next();
                    lvt_4_2_.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final ItemPredicate item;

        public Instance(ItemPredicate predicate) {
            super(ConsumeItemTrigger.ID);
            this.item = predicate;
        }

        public boolean test(ItemStack stack) {
            return this.item.test(stack);
        }
    }
}
