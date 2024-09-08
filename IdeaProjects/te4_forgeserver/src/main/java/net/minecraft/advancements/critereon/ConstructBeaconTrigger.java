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
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class ConstructBeaconTrigger extends ConcurrentHashMap<PlayerAdvancements, ConstructBeaconTrigger.Listeners> implements ICriterionTrigger<ConstructBeaconTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("construct_beacon");

    public ConstructBeaconTrigger() {}

    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public void addListener(PlayerAdvancements p_192165_1_, ICriterionTrigger.Listener<Instance> listener) {
        Listeners listeners = super.get(p_192165_1_);
        if (listeners == null) {
            listeners = new Listeners(p_192165_1_);
            super.put(p_192165_1_, listeners);
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
        MinMaxBounds level = MinMaxBounds.deserialize(jObj.get("level"));
        return new Instance(level);
    }


    public void trigger(EntityPlayerMP player, TileEntityBeacon beacon) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, beacon);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners(PlayerAdvancements advancements) {
            super(4);
        }


        public void trigger(PlayerAdvancements adv, TileEntityBeacon beacon) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var3 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var3.hasNext()) {
                listener = (ICriterionTrigger.Listener)var3.next();
                if (((Instance)listener.getCriterionInstance()).test(beacon)) {
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
        private final MinMaxBounds level;

        public Instance(MinMaxBounds level) {
            super(ConstructBeaconTrigger.ID);
            this.level = level;
        }

        public boolean test(TileEntityBeacon beacon) {
            return this.level.test((float)beacon.getLevels());
        }
    }
}