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
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class InventoryChangeTrigger extends ConcurrentHashMap<PlayerAdvancements, InventoryChangeTrigger.Listeners> implements ICriterionTrigger<InventoryChangeTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("inventory_changed");

    public InventoryChangeTrigger() {
        super(7);
    }


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
            listeners = new Listeners();
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
        JsonObject lvt_3_1_ = JsonUtils.getJsonObject(jObj, "slots", new JsonObject());
        MinMaxBounds lvt_4_1_ = MinMaxBounds.deserialize(lvt_3_1_.get("occupied"));
        MinMaxBounds lvt_5_1_ = MinMaxBounds.deserialize(lvt_3_1_.get("full"));
        MinMaxBounds lvt_6_1_ = MinMaxBounds.deserialize(lvt_3_1_.get("empty"));
        ItemPredicate[] lvt_7_1_ = ItemPredicate.deserializeArray(jObj.get("items"));
        return new Instance(lvt_4_1_, lvt_5_1_, lvt_6_1_, lvt_7_1_);
    }


    public void trigger(EntityPlayerMP player, InventoryPlayer inv) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, inv);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(7);
        }

        public void trigger(PlayerAdvancements adv, InventoryPlayer inv) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var3 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var3.hasNext()) {
                listener = (ICriterionTrigger.Listener)var3.next();
                if (((Instance)listener.getCriterionInstance()).test(inv)) {
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
        private final MinMaxBounds occupied;
        private final MinMaxBounds full;
        private final MinMaxBounds empty;
        private final ItemPredicate[] items;

        public Instance(MinMaxBounds p_i47390_1_, MinMaxBounds p_i47390_2_, MinMaxBounds p_i47390_3_, ItemPredicate[] p_i47390_4_) {
            super(InventoryChangeTrigger.ID);
            this.occupied = p_i47390_1_;
            this.full = p_i47390_2_;
            this.empty = p_i47390_3_;
            this.items = p_i47390_4_;
        }

        public boolean test(InventoryPlayer inv) {
            int lvt_2_1_ = 0;
            int lvt_3_1_ = 0;
            int lvt_4_1_ = 0;
            List<ItemPredicate> predicates = Lists.newArrayList(this.items);

            for(int i = 0; i < inv.getSizeInventory(); ++i) {
                ItemStack lvt_7_1_ = inv.getStackInSlot(i);
                if (lvt_7_1_.isEmpty()) {
                    ++lvt_3_1_;
                } else {
                    ++lvt_4_1_;
                    if (lvt_7_1_.getCount() >= lvt_7_1_.getMaxStackSize()) {
                        ++lvt_2_1_;
                    }

                    predicates.removeIf(lvt_9_1_ -> lvt_9_1_.test(lvt_7_1_));
                }
            }

            if (!this.full.test((float)lvt_2_1_)) {
                return false;
            } else if (!this.empty.test((float)lvt_3_1_)) {
                return false;
            } else if (!this.occupied.test((float)lvt_4_1_)) {
                return false;
            } else return predicates.isEmpty();
        }
    }
}