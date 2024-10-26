package net.minecraft.advancements.critereon;

import javax.annotation.*;
import net.minecraft.item.*;
import java.util.*;
import net.minecraft.potion.*;
import net.minecraft.enchantment.*;
import net.minecraft.util.*;
import net.minecraftforge.advancements.critereon.*;
import java.util.function.*;
import com.google.gson.*;


public class ItemPredicate {
    static final ItemPredicate[] EMPTY = new ItemPredicate[0];
    public static final ItemPredicate ANY;
    private final Item item;
    private final Integer data;
    private final MinMaxBounds count;
    private final MinMaxBounds durability;
    private final EnchantmentPredicate[] enchantments;
    private final PotionType potion;
    private final NBTPredicate nbt;

    public ItemPredicate() {
        this.item = null;
        this.data = null;
        this.potion = null;
        this.count = MinMaxBounds.UNBOUNDED;
        this.durability = MinMaxBounds.UNBOUNDED;
        this.enchantments = EnchantmentPredicate.EMPTY;
        this.nbt = NBTPredicate.ANY;
    }

    public ItemPredicate(@Nullable final Item item, @Nullable final Integer data, final MinMaxBounds count, final MinMaxBounds durability, final EnchantmentPredicate[] enchantments, @Nullable final PotionType potion, final NBTPredicate nbt) {
        this.item = item;
        this.data = data;
        this.count = count;
        this.durability = durability;
        this.enchantments = enchantments;
        this.potion = potion;
        this.nbt = nbt;
    }


    public boolean test(final ItemStack item) {
        if (this.item != null && item.getItem() != this.item) {
            return false;
        }
        if (this.data != null && item.getMetadata() != this.data) {
            return false;
        }
        if (!this.count.test((float)item.getCount())) {
            return false;
        }
        if (this.durability != MinMaxBounds.UNBOUNDED && !item.isItemStackDamageable()) {
            return false;
        }
        if (!this.durability.test((float)(item.getMaxDamage() - item.getItemDamage()))) {
            return false;
        }
        if (!this.nbt.test(item)) {
            return false;
        }
        final Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(item);
        for (EnchantmentPredicate enchantment : this.enchantments) {
            if (!enchantment.test(map)) return false;
        }
        final PotionType potiontype = PotionUtils.getPotionFromItem(item);
        return this.potion == null || this.potion == potiontype;
    }


    public static ItemPredicate deserialize(@Nullable final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return ItemPredicate.ANY;
        }
        final JsonObject jsonobject = JsonUtils.getJsonObject(element, "item");
        if (!jsonobject.has("type")) {
            final MinMaxBounds minmaxbounds = MinMaxBounds.deserialize(jsonobject.get("count"));
            final MinMaxBounds minmaxbounds2 = MinMaxBounds.deserialize(jsonobject.get("durability"));
            final Integer integer = jsonobject.has("data") ? Integer.valueOf(JsonUtils.getInt(jsonobject, "data")) : null;
            final NBTPredicate nbtpredicate = NBTPredicate.deserialize(jsonobject.get("nbt"));
            Item item = null;
            if (jsonobject.has("item")) {
                final ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonobject, "item"));
                item = Item.REGISTRY.getObject(resourcelocation);
                if (item == null) {
                    throw new JsonSyntaxException("Unknown item id '" + resourcelocation + "'");
                }
            }
            final EnchantmentPredicate[] aenchantmentpredicate = EnchantmentPredicate.deserializeArray(jsonobject.get("enchantments"));
            PotionType potiontype = null;
            if (jsonobject.has("potion")) {
                final ResourceLocation resourcelocation2 = new ResourceLocation(JsonUtils.getString(jsonobject, "potion"));
                if (!PotionType.REGISTRY.containsKey(resourcelocation2)) {
                    throw new JsonSyntaxException("Unknown potion '" + resourcelocation2 + "'");
                }
                potiontype = PotionType.REGISTRY.getObject(resourcelocation2);
            }
            return new ItemPredicate(item, integer, minmaxbounds, minmaxbounds2, aenchantmentpredicate, potiontype, nbtpredicate);
        }
        final ResourceLocation rl = new ResourceLocation(JsonUtils.getString(jsonobject, "type"));
        final Map<ResourceLocation, Function<JsonObject, ItemPredicate>> map = ItemPredicates.getPredicates();
        if (map.containsKey(rl)) {
            return map.get(rl).apply(jsonobject);
        }
        throw new JsonSyntaxException("There is no ItemPredicate of type " + rl);
    }


    public static ItemPredicate[] deserializeArray(@Nullable final JsonElement element) {
        if (element != null && !element.isJsonNull()) {
            final JsonArray jsonarray = JsonUtils.getJsonArray(element, "items");
            final ItemPredicate[] aitempredicate = new ItemPredicate[jsonarray.size()];
            for (int i = 0; i < aitempredicate.length; ++i) {
                aitempredicate[i] = deserialize(jsonarray.get(i));
            }
            return aitempredicate;
        }
        return EMPTY;
    }


    static {
        ANY = new ItemPredicate();
    }
}