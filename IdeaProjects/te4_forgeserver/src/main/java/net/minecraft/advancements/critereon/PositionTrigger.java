//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import plus.tson.utl.Te4HashSet;


public class PositionTrigger extends ConcurrentHashMap<PlayerAdvancements, PositionTrigger.Listeners> implements ICriterionTrigger<PositionTrigger.Instance> {
    private final ResourceLocation id;

    public PositionTrigger(ResourceLocation location) {
        this.id = location;
    }


    @Override
    public ResourceLocation getId() {
        return this.id;
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
        LocationPredicate predicate = LocationPredicate.deserialize(jObj);
        return new Instance(this.id, predicate);
    }


    public void trigger(EntityPlayerMP player) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player.getServerWorld(), player.posX, player.posY, player.posZ);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(8);
        }

        public void trigger(PlayerAdvancements adv, WorldServer world, double dx, double dy, double dz) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var9 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var9.hasNext()) {
                listener = (ICriterionTrigger.Listener)var9.next();
                if (((Instance)listener.getCriterionInstance()).test(world, dx, dy, dz)) {
                    if (listeners == null) {
                        listeners = Lists.newArrayList();
                    }

                    listeners.add(listener);
                }
            }

            if (listeners != null) {
                var9 = listeners.iterator();

                while(var9.hasNext()) {
                    listener = (ICriterionTrigger.Listener)var9.next();
                    listener.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final LocationPredicate location;

        public Instance(ResourceLocation id, LocationPredicate loc) {
            super(id);
            this.location = loc;
        }

        public boolean test(WorldServer world, double dx, double dy, double dz) {
            return this.location.test(world, dx, dy, dz);
        }
    }
}
