package net.minecraft.item;

import net.minecraft.creativetab.*;
import net.minecraft.util.math.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.entity.*;
import net.minecraft.init.*;
import net.minecraft.entity.player.*;
import net.minecraft.advancements.*;
import plus.fserver.api.IBlockEAccess;


public class ItemRedstone extends Item {
    public ItemRedstone() {
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }


    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final IBlockEAccess access = world.access();
        final boolean flag = access.getBlockState(pos).getBlock().isReplaceable(access, pos);
        final BlockPos blockpos = flag ? pos : pos.offset(facing);
        final ItemStack itemstack = player.getHeldItem(hand);
        if (player.canPlayerEdit(blockpos, facing, itemstack) && world.mayPlace(access.getBlockState(blockpos).getBlock(), blockpos, false, facing, player) && Blocks.REDSTONE_WIRE.canPlaceBlockAt(world, blockpos)) {
            access.setBlockState(blockpos, Blocks.REDSTONE_WIRE.getDefaultState(), 3);
            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, blockpos, itemstack);
            }
            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }
}