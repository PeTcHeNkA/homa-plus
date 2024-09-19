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
import net.minecraft.world.WorldServer;
import plus.tson.utl.Te4HashSet;


public class NetherTravelTrigger extends ConcurrentHashMap<PlayerAdvancements, NetherTravelTrigger.Listeners> implements ICriterionTrigger<NetherTravelTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("nether_travel");

    public NetherTravelTrigger() {
        super(3);
    }


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
    public Instance deserializeInstance(JsonObject jObj, JsonDeserializationContext p_192166_2_) {
        LocationPredicate lvt_3_1_ = LocationPredicate.deserialize(jObj.get("entered"));
        LocationPredicate lvt_4_1_ = LocationPredicate.deserialize(jObj.get("exited"));
        DistancePredicate lvt_5_1_ = DistancePredicate.deserialize(jObj.get("distance"));
        return new Instance(lvt_3_1_, lvt_4_1_, lvt_5_1_);
    }


    public void trigger(EntityPlayerMP player, Vec3d vec3d) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, player.getServerWorld(), vec3d, player.posX, player.posY, player.posZ);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(3);
        }

        public void trigger(PlayerAdvancements adv, WorldServer world, Vec3d vec3d, double dx, double dy, double dz) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var10 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var10.hasNext()) {
                listener = (ICriterionTrigger.Listener)var10.next();
                if (((Instance)listener.getCriterionInstance()).test(world, vec3d, dx, dy, dz)) {
                    if (listeners == null) {
                        listeners = Lists.newArrayList();
                    }
                    listeners.add(listener);
                }
            }

            if (listeners != null) {
                var10 = listeners.iterator();

                while(var10.hasNext()) {
                    listener = (ICriterionTrigger.Listener)var10.next();
                    listener.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final LocationPredicate entered;
        private final LocationPredicate exited;
        private final DistancePredicate distance;

        public Instance(LocationPredicate ennered, LocationPredicate exited, DistancePredicate distance) {
            super(NetherTravelTrigger.ID);
            this.entered = ennered;
            this.exited = exited;
            this.distance = distance;
        }


        public boolean test(WorldServer world, Vec3d vec3d, double dx, double dy, double dz) {
            if (!this.entered.test(world, vec3d.x, vec3d.y, vec3d.z)) {
                return false;
            } else if (!this.exited.test(world, dx, dy, dz)) {
                return false;
            } else {
                return this.distance.test(vec3d.x, vec3d.y, vec3d.z, dx, dy, dz);
            }
        }
    }
}