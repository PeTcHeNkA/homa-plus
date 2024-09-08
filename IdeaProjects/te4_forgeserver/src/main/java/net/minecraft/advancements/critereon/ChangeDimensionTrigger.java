package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import plus.tson.utl.Te4HashSet;


public class ChangeDimensionTrigger extends ConcurrentHashMap<PlayerAdvancements, ChangeDimensionTrigger.Listeners> implements ICriterionTrigger<ChangeDimensionTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("changed_dimension");

    public ChangeDimensionTrigger() {}


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
    public Instance deserializeInstance(JsonObject p_192166_1_, JsonDeserializationContext p_192166_2_) {
        DimensionType lvt_3_1_ = p_192166_1_.has("from") ? DimensionType.byName(JsonUtils.getString(p_192166_1_, "from")) : null;
        DimensionType lvt_4_1_ = p_192166_1_.has("to") ? DimensionType.byName(JsonUtils.getString(p_192166_1_, "to")) : null;
        return new Instance(lvt_3_1_, lvt_4_1_);
    }


    public void trigger(EntityPlayerMP player, DimensionType from, DimensionType to) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, from, to);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(4);
        }

        public void trigger(PlayerAdvancements adv, DimensionType from, DimensionType to) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var4 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var4.hasNext()) {
                listener = (ICriterionTrigger.Listener)var4.next();
                if (((Instance)listener.getCriterionInstance()).test(from, to)) {
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
        @Nullable
        private final DimensionType from;
        @Nullable
        private final DimensionType to;

        public Instance(@Nullable DimensionType from, @Nullable DimensionType to) {
            super(ChangeDimensionTrigger.ID);
            this.from = from;
            this.to = to;
        }

        public boolean test(DimensionType from, DimensionType to) {
            if (this.from != null && this.from != from) {
                return false;
            } else {
                return this.to == null || this.to == to;
            }
        }
    }
}
