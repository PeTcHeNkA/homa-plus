package net.minecraft.advancements.critereon;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import plus.tson.utl.Te4HashSet;


public class PlacedBlockTrigger extends ConcurrentHashMap<PlayerAdvancements, PlacedBlockTrigger.Listeners> implements ICriterionTrigger<PlacedBlockTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("placed_block");

    public PlacedBlockTrigger() {}


    @Override //fix no-search field
    public boolean isEmpty(){return super.isEmpty();}


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
        Block lvt_3_1_ = null;
        if (jObj.has("block")) {
            ResourceLocation lvt_4_1_ = new ResourceLocation(JsonUtils.getString(jObj, "block"));
            if (!Block.REGISTRY.containsKey(lvt_4_1_)) {
                throw new JsonSyntaxException("Unknown block type '" + lvt_4_1_ + "'");
            }

            lvt_3_1_ = (Block)Block.REGISTRY.getObject(lvt_4_1_);
        }

        Map<IProperty<?>, Object> lvt_4_2_ = null;
        if (jObj.has("state")) {
            if (lvt_3_1_ == null) {
                throw new JsonSyntaxException("Can't define block state without a specific block type");
            }

            BlockStateContainer lvt_5_1_ = lvt_3_1_.getBlockState();

            IProperty lvt_8_1_;
            Optional lvt_10_1_;
            for(Iterator var6 = JsonUtils.getJsonObject(jObj, "state").entrySet().iterator(); var6.hasNext(); lvt_4_2_.put(lvt_8_1_, lvt_10_1_.get())) {
                Map.Entry<String, JsonElement> lvt_7_1_ = (Map.Entry)var6.next();
                lvt_8_1_ = lvt_5_1_.getProperty((String)lvt_7_1_.getKey());
                if (lvt_8_1_ == null) {
                    throw new JsonSyntaxException("Unknown block state property '" + (String)lvt_7_1_.getKey() + "' for block '" + Block.REGISTRY.getNameForObject(lvt_3_1_) + "'");
                }

                String lvt_9_1_ = JsonUtils.getString((JsonElement)lvt_7_1_.getValue(), (String)lvt_7_1_.getKey());
                lvt_10_1_ = lvt_8_1_.parseValue(lvt_9_1_);
                if (!lvt_10_1_.isPresent()) {
                    throw new JsonSyntaxException("Invalid block state value '" + lvt_9_1_ + "' for property '" + (String)lvt_7_1_.getKey() + "' on block '" + Block.REGISTRY.getNameForObject(lvt_3_1_) + "'");
                }

                if (lvt_4_2_ == null) {
                    lvt_4_2_ = Maps.newHashMap();
                }
            }
        }

        LocationPredicate lvt_5_2_ = LocationPredicate.deserialize(jObj.get("location"));
        ItemPredicate lvt_6_1_ = ItemPredicate.deserialize(jObj.get("item"));
        return new Instance(lvt_3_1_, lvt_4_2_, lvt_5_2_, lvt_6_1_);
    }


    public void trigger(EntityPlayerMP player, BlockPos pos, ItemStack stack) {
        IBlockState state = player.world.getBlockState(pos);
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, state, pos, player.getServerWorld(), stack);
        }
    }


    public void trigger(EntityPlayerMP player, BlockPos pos, ItemStack stack, IBlockState state) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, state, pos, player.getServerWorld(), stack);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>>{
        public Listeners() {
            super(7);
        }


        public void trigger(PlayerAdvancements adv, IBlockState state, BlockPos pos, WorldServer world, ItemStack stack) {
            List<ICriterionTrigger.Listener<Instance>> inst = null;
            Iterator<Listener<Instance>> var6 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var6.hasNext()) {
                listener = var6.next();
                if (((Instance)listener.getCriterionInstance()).test(state, pos, world, stack)) {
                    if (inst == null) {
                        inst = Lists.newArrayList();
                    }

                    inst.add(listener);
                }
            }

            if (inst != null) {
                var6 = inst.iterator();

                while(var6.hasNext()) {
                    listener = var6.next();
                    listener.grantCriterion(adv);
                }
            }
        }
    }


    public static class Instance extends AbstractCriterionInstance {
        private final Block block;
        private final Map<IProperty<?>, Object> properties;
        private final LocationPredicate location;
        private final ItemPredicate item;

        public Instance(@Nullable Block block, @Nullable Map<IProperty<?>, Object> props, LocationPredicate loc, ItemPredicate item) {
            super(PlacedBlockTrigger.ID);
            this.block = block;
            this.properties = props;
            this.location = loc;
            this.item = item;
        }


        public boolean test(IBlockState state, BlockPos pos, WorldServer worldServer, ItemStack stack) {
            if (this.block != null && state.getBlock() != this.block) {
                return false;
            } else {
                if (this.properties != null) {
                    Iterator var5 = this.properties.entrySet().iterator();

                    while(var5.hasNext()) {
                        Map.Entry<IProperty<?>, Object> lvt_6_1_ = (Map.Entry)var5.next();
                        if (state.getValue((IProperty)lvt_6_1_.getKey()) != lvt_6_1_.getValue()) {
                            return false;
                        }
                    }
                }

                if (!this.location.test(worldServer, (float)pos.getX(), (float)pos.getY(), (float)pos.getZ())) {
                    return false;
                } else {
                    return this.item.test(stack);
                }
            }
        }
    }
}
