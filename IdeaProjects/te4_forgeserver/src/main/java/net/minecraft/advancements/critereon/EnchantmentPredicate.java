package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;


public class EnchantmentPredicate {
    static final EnchantmentPredicate[] EMPTY = new EnchantmentPredicate[0];
    public static final EnchantmentPredicate ANY = new EnchantmentPredicate();
    private final Enchantment enchantment;
    private final MinMaxBounds levels;

    public EnchantmentPredicate() {
        this.enchantment = null;
        this.levels = MinMaxBounds.UNBOUNDED;
    }

    public EnchantmentPredicate(@Nullable Enchantment enchantment, MinMaxBounds levels) {
        this.enchantment = enchantment;
        this.levels = levels;
    }


    public boolean test(Map<Enchantment, Integer> map) {
        if (this.enchantment != null) {
            if (!map.containsKey(this.enchantment)) {
                return false;
            }
            int num = map.get(this.enchantment);
            if (this.levels != null && !this.levels.test((float)num)) {
                return false;
            }
        } else if (this.levels != null) {
            Iterator var4 = map.values().iterator();
            Integer num;
            do {
                if (!var4.hasNext()) {
                    return false;
                }
                num = (Integer)var4.next();
            } while(!this.levels.test((float)num));
            return true;
        }
        return true;
    }


    public static EnchantmentPredicate deserialize(@Nullable JsonElement jEl) {
        if (jEl != null && !jEl.isJsonNull()) {
            JsonObject lvt_1_1_ = JsonUtils.getJsonObject(jEl, "enchantment");
            Enchantment ench = null;
            if (lvt_1_1_.has("enchantment")) {
                ResourceLocation lvt_3_1_ = new ResourceLocation(JsonUtils.getString(lvt_1_1_, "enchantment"));
                ench = Enchantment.REGISTRY.getObject(lvt_3_1_);
                if (ench == null) {
                    throw new JsonSyntaxException("Unknown enchantment '" + lvt_3_1_ + "'");
                }
            }
            MinMaxBounds bounds = MinMaxBounds.deserialize(lvt_1_1_.get("levels"));
            return new EnchantmentPredicate(ench, bounds);
        } else {
            return ANY;
        }
    }


    public static EnchantmentPredicate[] deserializeArray(@Nullable JsonElement jEl) {
        if (jEl != null && !jEl.isJsonNull()) {
            JsonArray jArr = JsonUtils.getJsonArray(jEl, "enchantments");
            EnchantmentPredicate[] predicates = new EnchantmentPredicate[jArr.size()];

            for(int i = 0; i < predicates.length; ++i) {
                predicates[i] = deserialize(jArr.get(i));
            }
            return predicates;
        } else {
            return EMPTY;
        }
    }
}