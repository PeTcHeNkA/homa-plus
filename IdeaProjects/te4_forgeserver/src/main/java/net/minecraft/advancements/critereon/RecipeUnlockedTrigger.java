package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import plus.tson.utl.Te4HashSet;


public class RecipeUnlockedTrigger extends ConcurrentHashMap<PlayerAdvancements, RecipeUnlockedTrigger.Listeners> implements ICriterionTrigger<RecipeUnlockedTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("recipe_unlocked");

    public RecipeUnlockedTrigger() {}

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
        ResourceLocation lvt_3_1_ = new ResourceLocation(JsonUtils.getString(jObj, "recipe"));
        IRecipe recipe = CraftingManager.getRecipe(lvt_3_1_);
        if (recipe == null) {
            throw new JsonSyntaxException("Unknown recipe '" + lvt_3_1_ + "'");
        } else {
            return new Instance(recipe);
        }
    }


    public void trigger(EntityPlayerMP player, IRecipe recipe) {
        PlayerAdvancements adv;
        Listeners listeners = super.get(adv = player.getAdvancements());
        if (listeners != null) {
            listeners.trigger(adv, recipe);
        }
    }


    static class Listeners extends Te4HashSet<ICriterionTrigger.Listener<Instance>> {
        public Listeners() {}

        public void trigger(PlayerAdvancements adv, IRecipe recipe) {
            List<ICriterionTrigger.Listener<Instance>> listeners = null;
            Iterator var3 = super.iterator();

            ICriterionTrigger.Listener listener;
            while(var3.hasNext()) {
                listener = (ICriterionTrigger.Listener)var3.next();
                if (((Instance)listener.getCriterionInstance()).test(recipe)) {
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
        private final IRecipe recipe;

        public Instance(IRecipe recipe) {
            super(RecipeUnlockedTrigger.ID);
            this.recipe = recipe;
        }

        public boolean test(IRecipe recipe) {
            return this.recipe == recipe;
        }
    }
}