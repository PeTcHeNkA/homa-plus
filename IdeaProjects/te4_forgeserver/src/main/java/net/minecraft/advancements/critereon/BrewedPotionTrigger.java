package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionType;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class BrewedPotionTrigger extends ConcurrentHashMap<PlayerAdvancements, BrewedPotionTrigger.Listeners> implements ICriterionTrigger<BrewedPotionTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("brewed_potion");

    public BrewedPotionTrigger() {
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
    public Instance deserializeInstance(JsonObject p_192166_1_, JsonDeserializationContext p_192166_2_) {
        PotionType lvt_3_1_ = null;
        if (p_192166_1_.has("potion")) {
            ResourceLocation lvt_4_1_ = new ResourceLocation(JsonUtils.getString(p_192166_1_, "potion"));
            if (!PotionType.REGISTRY.containsKey(lvt_4_1_)) {
                throw new JsonSyntaxException("Unknown potion '" + lvt_4_1_ + "'");
            }

            lvt_3_1_ = (PotionType)PotionType.REGISTRY.getObject(lvt_4_1_);
        }

        return new Instance(lvt_3_1_);
    }


    public void trigger(EntityPlayerMP player, PotionType type) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, type);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(4);
        }

        public void trigger(PlayerAdvancements adv, PotionType type) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var3 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var3.hasNext()) {
                listener = (ICriterionTrigger.Listener)var3.next();
                if (((Instance)listener.getCriterionInstance()).test(type)) {
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
        private final PotionType potion;

        public Instance(@Nullable PotionType type) {
            super(BrewedPotionTrigger.ID);
            this.potion = type;
        }

        public boolean test(PotionType type) {
            return this.potion == null || this.potion == type;
        }
    }
}