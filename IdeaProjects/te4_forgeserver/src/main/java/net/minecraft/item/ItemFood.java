package net.minecraft.item;

import net.minecraft.potion.*;
import net.minecraft.creativetab.*;
import net.minecraft.world.*;
import net.minecraft.entity.*;
import net.minecraft.init.*;
import net.minecraft.stats.*;
import net.minecraft.entity.player.*;
import net.minecraft.advancements.*;
import net.minecraft.util.*;
import plus.fserver.api.Keep;


public class ItemFood extends Item {
    @Keep
    public final int itemUseDuration = 32;
    private final int healAmount;
    private final float saturationModifier;
    private final boolean isWolfsFavoriteMeat;
    private boolean alwaysEdible;
    private PotionEffect potionId;
    private float potionEffectProbability;

    public ItemFood(final int amount, final float saturation, final boolean isWolfFood) {
        this.healAmount = amount;
        this.isWolfsFavoriteMeat = isWolfFood;
        this.saturationModifier = saturation;
        this.setCreativeTab(CreativeTabs.FOOD);
    }


    public ItemFood(final int amount, final boolean isWolfFood) {
        this(amount, 0.6f, isWolfFood);
    }


    @Override
    public ItemStack onItemUseFinish(final ItemStack stack, final World worldIn, final EntityLivingBase living) {
        if (living.isPlayer()) {
            final EntityPlayer player = (EntityPlayer)living;
            player.getFoodStats().addStats(this, stack);
            worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f, worldIn.rand.nextFloat() * 0.1f + 0.9f);
            this.onFoodEaten(stack, worldIn, player);
            player.addStat(StatList.getObjectUseStats(this));
            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP)player, stack);
            }
        }
        stack.shrink(1);
        return stack;
    }


    protected void onFoodEaten(final ItemStack stack, final World worldIn, final EntityPlayer player) {
        if (!worldIn.isRemote && this.potionId != null && worldIn.rand.nextFloat() < this.potionEffectProbability) {
            player.addPotionEffect(new PotionEffect(this.potionId));
        }
    }


    @Override
    public int getMaxItemUseDuration(final ItemStack stack) {
        return 32;
    }


    @Override
    public EnumAction getItemUseAction(final ItemStack stack) {
        return EnumAction.EAT;
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final EntityPlayer playerIn, final EnumHand handIn) {
        final ItemStack stack = playerIn.getHeldItem(handIn);
        if (playerIn.canEat(this.alwaysEdible)) {
            playerIn.setActiveHand(handIn);
            return worldIn.pooledAction.init(EnumActionResult.SUCCESS, stack);
        }
        return worldIn.pooledAction.init(EnumActionResult.FAIL, stack);
    }


    public int getHealAmount(final ItemStack stack) {
        return this.healAmount;
    }


    public float getSaturationModifier(final ItemStack stack) {
        return this.saturationModifier;
    }


    public boolean isWolfsFavoriteMeat() {
        return this.isWolfsFavoriteMeat;
    }


    public ItemFood setPotionEffect(final PotionEffect effect, final float probability) {
        this.potionId = effect;
        this.potionEffectProbability = probability;
        return this;
    }


    public ItemFood setAlwaysEdible() {
        this.alwaysEdible = true;
        return this;
    }
}
