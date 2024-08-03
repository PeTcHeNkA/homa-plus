package net.minecraft.item;

import net.minecraft.creativetab.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.stats.*;
import net.minecraft.util.*;


public class ItemEgg extends Item {
    public ItemEgg() {
        this.maxStackSize = 16;
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final EntityPlayer playerIn, final EnumHand handIn) {
        final ItemStack stack = playerIn.getHeldItem(handIn);
        if (!playerIn.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5f, 0.4f / (ItemEgg.itemRand.nextFloat() * 0.4f + 0.8f));
        if (!worldIn.isRemote) {
            final EntityEgg egg = new EntityEgg(worldIn, playerIn);
            egg.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0f, 1.5f, 1.0f);
            worldIn.spawnEntity(egg);
        }
        playerIn.addStat(StatList.getObjectUseStats(this));
        return worldIn.pooledAction.init(EnumActionResult.SUCCESS, stack);
    }
}