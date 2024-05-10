package net.minecraft.enchantment;

import com.google.common.base.Predicate;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

import java.util.Arrays;


public class EnumEnchantmentType {
    public static final EnumEnchantmentType ALL = new EnumEnchantmentType("ALL"){
        public boolean canEnchantItem(Item itemIn) {
            for (EnumEnchantmentType enumenchantmenttype : EnumEnchantmentType.values()) {
                if (enumenchantmenttype != EnumEnchantmentType.ALL && enumenchantmenttype.canEnchantItem(itemIn)) {
                    return true;
                }
            }

            return false;
        }
    };
    public static final EnumEnchantmentType ARMOR = new EnumEnchantmentType("ARMOR"){
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor;
        }
    };
    public static final EnumEnchantmentType ARMOR_FEET = new EnumEnchantmentType("ARMOR_FEET"){
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor && ((ItemArmor)itemIn).armorType == EntityEquipmentSlot.FEET;
        }
    };
    public static final EnumEnchantmentType ARMOR_LEGS = new EnumEnchantmentType("ARMOR_LEGS"){
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor && ((ItemArmor)itemIn).armorType == EntityEquipmentSlot.LEGS;
        }
    };
    public static final EnumEnchantmentType ARMOR_CHEST = new EnumEnchantmentType("ARMOR_CHEST"){
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor && ((ItemArmor)itemIn).armorType == EntityEquipmentSlot.CHEST;
        }
    };
    public static final EnumEnchantmentType ARMOR_HEAD = new EnumEnchantmentType("ARMOR_HEAD"){
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor && ((ItemArmor)itemIn).armorType == EntityEquipmentSlot.HEAD;
        }
    };
    public static final EnumEnchantmentType WEAPON = new EnumEnchantmentType("WEAPON"){
        public boolean canEnchantItem(Item itemIn)
        {
            return itemIn instanceof ItemSword;
        }
    };
    public static final EnumEnchantmentType DIGGER = new EnumEnchantmentType("DIGGER"){
        public boolean canEnchantItem(Item itemIn)
        {
            return itemIn instanceof ItemTool;
        }
    };
    public static final EnumEnchantmentType FISHING_ROD = new EnumEnchantmentType("FISHING_ROD"){
        public boolean canEnchantItem(Item itemIn)
        {
            return itemIn instanceof ItemFishingRod;
        }
    };
    public static final EnumEnchantmentType BREAKABLE = new EnumEnchantmentType("BREAKABLE"){
        public boolean canEnchantItem(Item itemIn)
        {
            return itemIn.isDamageable();
        }
    };
    public static final EnumEnchantmentType BOW = new EnumEnchantmentType("BOW"){
        public boolean canEnchantItem(Item itemIn)
        {
            return itemIn instanceof ItemBow;
        }
    };
    public static final EnumEnchantmentType WEARABLE = new EnumEnchantmentType("WEARABLE"){
        public boolean canEnchantItem(Item itemIn)
        {
            boolean flag = itemIn instanceof ItemBlock && ((ItemBlock)itemIn).getBlock() instanceof BlockPumpkin;
            return itemIn instanceof ItemArmor || itemIn instanceof ItemElytra || itemIn instanceof ItemSkull || flag;
        }
    };


    private static EnumEnchantmentType[] types = new EnumEnchantmentType[]{
            ALL,ARMOR,ARMOR_FEET,ARMOR_LEGS,ARMOR_CHEST,ARMOR_HEAD,WEAPON,DIGGER,FISHING_ROD,BREAKABLE,BOW,WEARABLE
    };

    private static int id = 0;
    private Predicate<Item> delegate = null;
    private final String name;
    private final int ordinal = id++;

    private EnumEnchantmentType(String name) {
        this.name = name;
    }


    private EnumEnchantmentType(String name, Predicate<Item> delegate) {
        this.delegate = delegate;
        this.name = name;
    }


    public final int ordinal() {
        return ordinal;
    }


    public static EnumEnchantmentType valueOf(String name){
        for (EnumEnchantmentType material:types){
            if(material.name.equals(name))return material;
        }
        return null;
    }


    public static EnumEnchantmentType[] values() {
        return types;
    }


    public static EnumEnchantmentType insert(String name, Predicate<Item> delegate){
        EnumEnchantmentType[] newMat = Arrays.copyOf(types, types.length+1);
        EnumEnchantmentType res = newMat[types.length] = new EnumEnchantmentType(name, delegate);
        types = newMat;
        return res;
    }


    public String name() {
        return this.name;
    }


    @Override
    public String toString() {
        return name;
    }


    public boolean canEnchantItem(Item itemIn) {
        return this.delegate == null ? false : this.delegate.apply(itemIn);
    }
}
