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
import net.minecraft.util.math.Vec3d;
import plus.tson.utl.Te4HashSet;


public class LevitationTrigger extends ConcurrentHashMap<PlayerAdvancements, LevitationTrigger.Listeners> implements ICriterionTrigger<LevitationTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("levitation");

    public LevitationTrigger() {}


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
        DistancePredicate distance = DistancePredicate.deserialize(jObj.get("distance"));
        MinMaxBounds duration = MinMaxBounds.deserialize(jObj.get("duration"));
        return new Instance(distance, duration);
    }


    public void trigger(EntityPlayerMP player, Vec3d vec3d, int duration) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player, vec3d, duration);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(3);
        }


        public void trigger(PlayerAdvancements adv, EntityPlayerMP player, Vec3d vec3d, int duration) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var5 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var5.hasNext()) {
                listener = (ICriterionTrigger.Listener)var5.next();
                if (((Instance)listener.getCriterionInstance()).test(player, vec3d, duration)) {
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
        private final DistancePredicate distance;
        private final MinMaxBounds duration;

        public Instance(DistancePredicate distance, MinMaxBounds duration) {
            super(LevitationTrigger.ID);
            this.distance = distance;
            this.duration = duration;
        }

        public boolean test(EntityPlayerMP player, Vec3d vec3d, int duration) {
            if (!this.distance.test(vec3d.x, vec3d.y, vec3d.z, player.posX, player.posY, player.posZ)) {
                return false;
            } else {
                return this.duration.test((float)duration);
            }
        }
    }
}