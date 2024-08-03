package net.minecraft.item;

import net.minecraft.creativetab.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.stats.*;
import net.minecraft.util.*;


public class ItemSnowball extends Item {
    public ItemSnowball() {
        this.maxStackSize = 16;
        this.setCreativeTab(CreativeTabs.MISC);
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final EntityPlayer playerIn, final EnumHand handIn) {
        final ItemStack stack = playerIn.getHeldItem(handIn);
        if (!playerIn.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (ItemSnowball.itemRand.nextFloat() * 0.4f + 0.8f));
        if (!worldIn.isRemote) {
            final EntitySnowball snowball = new EntitySnowball(worldIn, playerIn);
            snowball.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0f, 1.5f, 1.0f);
            worldIn.spawnEntity(snowball);
        }
        playerIn.addStat(StatList.getObjectUseStats(this));
        return worldIn.pooledAction.init(EnumActionResult.SUCCESS, stack);
    }
}