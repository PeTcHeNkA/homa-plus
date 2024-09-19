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
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class EnterBlockTrigger extends ConcurrentHashMap<PlayerAdvancements, EnterBlockTrigger.Listeners> implements ICriterionTrigger<EnterBlockTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("enter_block");

    public EnterBlockTrigger() {
        super(7);
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public void addListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> inst) {
        Listeners listeners = super.get(advancements);
        if (listeners == null) {
            listeners = new Listeners();
            super.put(advancements, listeners);
        }

        listeners.add(inst);
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
        Block lvt_3_1_ = null;
        if (p_192166_1_.has("block")) {
            ResourceLocation lvt_4_1_ = new ResourceLocation(JsonUtils.getString(p_192166_1_, "block"));
            if (!Block.REGISTRY.containsKey(lvt_4_1_)) {
                throw new JsonSyntaxException("Unknown block type '" + lvt_4_1_ + "'");
            }

            lvt_3_1_ = (Block)Block.REGISTRY.getObject(lvt_4_1_);
        }

        Map<IProperty<?>, Object> lvt_4_2_ = null;
        if (p_192166_1_.has("state")) {
            if (lvt_3_1_ == null) {
                throw new JsonSyntaxException("Can't define block state without a specific block type");
            }

            BlockStateContainer lvt_5_1_ = lvt_3_1_.getBlockState();

            IProperty lvt_8_1_;
            Optional lvt_10_1_;
            for(Iterator var6 = JsonUtils.getJsonObject(p_192166_1_, "state").entrySet().iterator(); var6.hasNext(); lvt_4_2_.put(lvt_8_1_, lvt_10_1_.get())) {
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

        return new Instance(lvt_3_1_, lvt_4_2_);
    }


    public void trigger(EntityPlayerMP player, IBlockState state) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, state);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {
            super(8);
        }


        public void trigger(PlayerAdvancements adv, IBlockState state) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var3 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var3.hasNext()) {
                listener = (ICriterionTrigger.Listener)var3.next();
                if (((Instance)listener.getCriterionInstance()).test(state)) {
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
        private final Block block;
        private final Map<IProperty<?>, Object> properties;

        public Instance(@Nullable Block block, @Nullable Map<IProperty<?>, Object> props) {
            super(EnterBlockTrigger.ID);
            this.block = block;
            this.properties = props;
        }

        public boolean test(IBlockState state) {
            if (this.block != null && state.getBlock() != this.block) {
                return false;
            } else {
                if (this.properties != null) {
                    Iterator var2 = this.properties.entrySet().iterator();

                    while(var2.hasNext()) {
                        Map.Entry<IProperty<?>, Object> lvt_3_1_ = (Map.Entry)var2.next();
                        if (state.getValue(lvt_3_1_.getKey()) != lvt_3_1_.getValue()) {
                            return false;
                        }
                    }
                }

                return true;
            }
        }
    }
}