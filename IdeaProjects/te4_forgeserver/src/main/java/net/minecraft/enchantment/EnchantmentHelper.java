package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.Map.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import plus.fserver.utls.collections.ArrayListE6;


public class EnchantmentHelper {
//    private static final EnchantmentHelper.ModifierDamage ENCHANTMENT_MODIFIER_DAMAGE = new EnchantmentHelper.ModifierDamage();
//    private static final EnchantmentHelper.ModifierLiving ENCHANTMENT_MODIFIER_LIVING = new EnchantmentHelper.ModifierLiving();
//    private static final EnchantmentHelper.HurtIterator   ENCHANTMENT_ITERATOR_HURT   = new EnchantmentHelper.HurtIterator();
//    private static final EnchantmentHelper.DamageIterator ENCHANTMENT_ITERATOR_DAMAGE = new EnchantmentHelper.DamageIterator();


    //thread-safe
    public static int getEnchantmentLevel(Enchantment enchID, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        else {
            NBTTagList nbttaglist = stack.getEnchantmentTagList();

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                Enchantment enchantment = Enchantment.getEnchantmentByID(nbttagcompound.getShort("id"));
                int j = nbttagcompound.getShort("lvl");

                if (enchantment == enchID) return j;
            }
            return 0;
        }
    }


    //thread-safe
    public static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> map = Maps.newLinkedHashMap();
        NBTTagList nbttaglist = stack.getItem() == Items.ENCHANTED_BOOK ? ItemEnchantedBook.getEnchantments(stack) : stack.getEnchantmentTagList();

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            Enchantment enchantment = Enchantment.getEnchantmentByID(nbttagcompound.getShort("id"));
            int j = nbttagcompound.getShort("lvl");
            map.put(enchantment, j);
        }
        return map;
    }


    //thread-safe
    public static void setEnchantments(Map<Enchantment, Integer> enchMap, ItemStack stack) {
        NBTTagList nbttaglist = new NBTTagList();

        for (Entry<Enchantment, Integer> entry : enchMap.entrySet()) {
            Enchantment enchantment = entry.getKey();

            if (enchantment != null) {
                int i = entry.getValue();
                NBTTagCompound nbttagcompound = new NBTTagCompound(4);
                nbttagcompound.setShortU("id", (short)Enchantment.getEnchantmentID(enchantment));
                nbttagcompound.setShortU("lvl", (short)i);
                nbttaglist.appendTag(nbttagcompound);

                if (stack.getItem() == Items.ENCHANTED_BOOK)
                    ItemEnchantedBook.addEnchantment(stack, new EnchantmentData(enchantment, i));
            }
        }
        if (nbttaglist.isEmpty())
            if (stack.hasTagCompound()) stack.getTagCompound().removeTag("ench");

        else if (stack.getItem() != Items.ENCHANTED_BOOK)
            stack.setTagInfo("ench", nbttaglist);
    }


//    private static void applyEnchantmentModifier(EnchantmentHelper.IModifier modifier, ItemStack stack)
//    {
//        if (!stack.isEmpty())
//        {
//            NBTTagList nbttaglist = stack.getEnchantmentTagList();
//
//            for (int i = 0; i < nbttaglist.tagCount(); ++i)
//            {
//                int j = nbttaglist.getCompoundTagAt(i).getShort("id");
//                int k = nbttaglist.getCompoundTagAt(i).getShort("lvl");
//
//                if (Enchantment.getEnchantmentByID(j) != null)
//                {
//                    modifier.calculateModifier(Enchantment.getEnchantmentByID(j), k);
//                }
//            }
//        }
//    }
//
//
//    private static void applyEnchantmentModifierArray(EnchantmentHelper.IModifier modifier, Iterable<ItemStack> stacks) {
//        for (ItemStack itemstack : stacks) {
//            applyEnchantmentModifier(modifier, itemstack);
//        }
//    }


    //tread-safe
    private static int getEnchantmentModifierDamage(ItemStack stack, DamageSource source) {
        int in = 0;
        if (!stack.isEmpty()) {
            NBTTagList nbttaglist = stack.getEnchantmentTagList();

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                int id = nbttaglist.getCompoundTagAt(i).getShort("id");
                int lvl = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                Enchantment ench = Enchantment.getEnchantmentByID(id);
                if(ench != null)in += ench.calcModifierDamage(lvl, source);
            }
        }
        return in;
    }


    //tread-safe
    public static int getEnchantmentModifierDamage(Iterable<ItemStack> stacks, DamageSource source) {
        int in = 0;
        for (ItemStack stack:stacks)in += getEnchantmentModifierDamage(stack, source);
        return in;
    }


    //tread-safe
    public static float getModifierForCreature(ItemStack stack, EnumCreatureAttribute attr) {
        float in = 0;
        if (!stack.isEmpty()) {
            NBTTagList nbttaglist = stack.getEnchantmentTagList();

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                int id = nbttaglist.getCompoundTagAt(i).getShort("id");
                int lvl = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                Enchantment ench = Enchantment.getEnchantmentByID(id);
                if(ench != null)in += ench.calcDamageByCreature(lvl, attr);
            }
        }
        return in;
    }


    //tread-safe
    public static float getSweepingDamageRatio(EntityLivingBase entity) {
        int i = getMaxEnchantmentLevel(Enchantments.SWEEPING, entity);
        return i > 0 ? EnchantmentSweepingEdge.getSweepingDamageRatio(i) : 0.0F;
    }


    //tread-safe
    private static void applyThornEnchantments(EntityLivingBase user, Entity attacker, ItemStack stack) {
        if (!stack.isEmpty()) {
            NBTTagList nbttaglist = stack.getEnchantmentTagList();

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                int id = nbttaglist.getCompoundTagAt(i).getShort("id");
                int lvl = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                Enchantment ench = Enchantment.getEnchantmentByID(id);
                if(ench != null)ench.onUserHurt(user, attacker, lvl);
            }
        }
    }


    //tread-safe
    public static void applyThornEnchantments(EntityLivingBase user, Entity attacker) {
        if (user != null)
            for (ItemStack stack:user.getEquipmentAndArmor())
                applyThornEnchantments(user, attacker, stack);

        if (attacker instanceof EntityPlayer)
            applyThornEnchantments(user, attacker, user.getHeldItemMainhand());
    }


    //tread-safe
    private static void applyArthropodEnchantments(EntityLivingBase user, Entity target, ItemStack stack) {
        if (!stack.isEmpty()) {
            NBTTagList nbttaglist = stack.getEnchantmentTagList();

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                int id = nbttaglist.getCompoundTagAt(i).getShort("id");
                int lvl = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                Enchantment ench = Enchantment.getEnchantmentByID(id);
                if(ench != null)ench.onEntityDamaged(user, target, lvl);
            }
        }
    }


    //tread-safe
    public static void applyArthropodEnchantments(EntityLivingBase user, Entity target) {
        if (user != null)
            for (ItemStack stack:user.getEquipmentAndArmor())
                applyArthropodEnchantments(user, target, stack);


        if (user instanceof EntityPlayer) {
            applyArthropodEnchantments(user, target, user.getHeldItemMainhand());
        }
    }


    //thread-safe
    public static int getMaxEnchantmentLevel(Enchantment ench, EntityLivingBase entity) {
        Iterable<ItemStack> iterable = ench.getEntityEquipment(entity);
        if (iterable == null) return 0;
        else {
            int i = 0;

            for (ItemStack itemstack : iterable) {
                int j = getEnchantmentLevel(ench, itemstack);
                if (j > i) i = j;
            }
            return i;
        }
    }


    //thread-safe
    public static int getKnockbackModifier(EntityLivingBase player) {
        return getMaxEnchantmentLevel(Enchantments.KNOCKBACK, player);
    }


    //thread-safe
    public static int getFireAspectModifier(EntityLivingBase player) {
        return getMaxEnchantmentLevel(Enchantments.FIRE_ASPECT, player);
    }


    //thread-safe
    public static int getRespirationModifier(EntityLivingBase p_185292_0_) {
        return getMaxEnchantmentLevel(Enchantments.RESPIRATION, p_185292_0_);
    }


    //thread-safe
    public static int getDepthStriderModifier(EntityLivingBase entity) {
        return getMaxEnchantmentLevel(Enchantments.DEPTH_STRIDER, entity);
    }


    //thread-safe
    public static int getEfficiencyModifier(EntityLivingBase entity) {
        return getMaxEnchantmentLevel(Enchantments.EFFICIENCY, entity);
    }


    //tread-safe
    public static int getFishingLuckBonus(ItemStack stack) {
        return getEnchantmentLevel(Enchantments.LUCK_OF_THE_SEA, stack);
    }


    //tread-safe
    public static int getFishingSpeedBonus(ItemStack stack) {
        return getEnchantmentLevel(Enchantments.LURE, stack);
    }


    //tread-safe
    public static int getLootingModifier(EntityLivingBase entity) {
        return getMaxEnchantmentLevel(Enchantments.LOOTING, entity);
    }


    //tread-safe
    public static boolean getAquaAffinityModifier(EntityLivingBase entity) {
        return getMaxEnchantmentLevel(Enchantments.AQUA_AFFINITY, entity) > 0;
    }


    //tread-safe
    public static boolean hasFrostWalkerEnchantment(EntityLivingBase player) {
        return getMaxEnchantmentLevel(Enchantments.FROST_WALKER, player) > 0;
    }


    //tread-safe
    public static boolean hasBindingCurse(ItemStack stack) {
        return getEnchantmentLevel(Enchantments.BINDING_CURSE, stack) > 0;
    }


    //tread-safe
    public static boolean hasVanishingCurse(ItemStack stack) {
        return getEnchantmentLevel(Enchantments.VANISHING_CURSE, stack) > 0;
    }


    //tread-safe
    public static ItemStack getEnchantedItem(Enchantment ench, EntityLivingBase entity) {
        List<ItemStack> list = ench.getEntityEquipment(entity);
        if (list.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ArrayListE6<ItemStack> list1 = new ArrayListE6<>();

            for (ItemStack itemstack : list) {
                if (!itemstack.isEmpty() && getEnchantmentLevel(ench, itemstack) > 0)
                    list1.addF(itemstack);
            }
            return list1.isEmptyC() ? ItemStack.EMPTY : list1.getC(entity.getRNG().nextInt(list1.sizeC()));
        }
    }


    //tread-safe
    public static int calcItemStackEnchantability(Random rand, int enchantNum, int power, ItemStack stack) {
        Item item = stack.getItem();
        int i = item.getItemEnchantability(stack);

        if (i <= 0) return 0;
        else {
            if (power > 15) power = 15;


            int j = rand.nextInt(8) + 1 + (power >> 1) + rand.nextInt(power + 1);

            if (enchantNum == 0) {
                return Math.max(j / 3, 1);
            } else {
                return enchantNum == 1 ? j * 2 / 3 + 1 : Math.max(j, power * 2);
            }
        }
    }


    //tread-safe
    public static ItemStack addRandomEnchantment(Random random, ItemStack stack, int level, boolean allowTreasure) {
        List<EnchantmentData> list = buildEnchantmentList(random, stack, level, allowTreasure);
        boolean flag = stack.getItem() == Items.BOOK;

        if (flag) stack = new ItemStack(Items.ENCHANTED_BOOK);

        for (EnchantmentData ench : list) {
            if (flag)
                ItemEnchantedBook.addEnchantment(stack, ench);
            else
                stack.addEnchantment(ench.enchantment, ench.enchantmentLevel);
        }
        return stack;
    }


    //tread-safe, mod logic
    public static List<EnchantmentData> buildEnchantmentList(Random randomIn, ItemStack itemStackIn, int level, boolean allowTreasure) {
        Item item = itemStackIn.getItem();
        int cap = item.getItemEnchantability(itemStackIn);

        if (cap <= 0) return Collections.emptyList();
        else {
            List<EnchantmentData> list = Lists.newArrayList();

            level = level + 1 + randomIn.nextInt(cap / 4 + 1) + randomIn.nextInt(cap / 4 + 1);
            float f = (randomIn.nextFloat() + randomIn.nextFloat() - 1.0F) * 0.15F;
            level = MathHelper.clamp(Math.round((float)level + (float)level * f), 1, Integer.MAX_VALUE);
            List<EnchantmentData> list1 = getEnchantmentDatas(level, itemStackIn, allowTreasure);

            if (!list1.isEmpty()) {
                list.add(WeightedRandom.getRandomItem(randomIn, list1));

                while (randomIn.nextInt(50) <= level) {
                    removeIncompatible(list1, Util.getLastElement(list));

                    if (list1.isEmpty()) {
                        break;
                    }
                    list.add(WeightedRandom.getRandomItem(randomIn, list1));
                    level /= 2;
                }
            }
            return list;
        }
    }


    //tread-safe
    public static void removeIncompatible(List<EnchantmentData> list, EnchantmentData data) {
        Iterator<EnchantmentData> iterator = list.iterator();
        while (iterator.hasNext())
            if (!data.enchantment.isCompatibleWith((iterator.next()).enchantment))
                iterator.remove();
    }


    //tread-safe
    public static List<EnchantmentData> getEnchantmentDatas(int lvl, ItemStack stack, boolean allowTreasure) {
        List<EnchantmentData> list = Lists.newArrayList();
        boolean flag = stack.getItem() == Items.BOOK;

        for (Enchantment enchantment : Enchantment.REGISTRY) {
            if ((!enchantment.isTreasureEnchantment() || allowTreasure) && (enchantment.canApplyAtEnchantingTable(stack) || (flag && enchantment.isAllowedOnBooks()))) {
                for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                    if (lvl >= enchantment.getMinEnchantability(i) && lvl <= enchantment.getMaxEnchantability(i)) {
                        list.add(new EnchantmentData(enchantment, i));
                        break;
                    }
                }
            }
        }
        return list;
    }


//    static final class DamageIterator implements EnchantmentHelper.IModifier {
//        public EntityLivingBase user;
//        public Entity target;
//
//        private DamageIterator() {}
//
//        public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel) {
//            enchantmentIn.onEntityDamaged(this.user, this.target, enchantmentLevel);
//        }
//    }
//
//
//    static final class HurtIterator implements EnchantmentHelper.IModifier {
//        public EntityLivingBase user;
//        public Entity attacker;
//
//        private HurtIterator() {}
//
//        public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel) {
//            enchantmentIn.onUserHurt(this.user, this.attacker, enchantmentLevel);
//        }
//    }
//
//
//    interface IModifier {
//        void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel);
//    }
//
//
//    static final class ModifierDamage implements EnchantmentHelper.IModifier {
//        public int damageModifier;
//        public DamageSource source;
//
//        private ModifierDamage() {}
//
//        public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel) {
//            this.damageModifier += enchantmentIn.calcModifierDamage(enchantmentLevel, this.source);
//        }
//    }
//
//
//    static final class ModifierLiving implements EnchantmentHelper.IModifier {
//        public float livingModifier;
//        public EnumCreatureAttribute entityLiving;
//
//        private ModifierLiving() {}
//
//        public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel) {
//            this.livingModifier += enchantmentIn.calcDamageByCreature(enchantmentLevel, this.entityLiving);
//        }
//    }
}
