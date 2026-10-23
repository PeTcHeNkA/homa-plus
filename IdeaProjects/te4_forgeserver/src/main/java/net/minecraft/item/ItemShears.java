package net.minecraft.item;

import net.minecraft.creativetab.*;
import net.minecraft.block.state.*;
import net.minecraft.util.math.*;
import net.minecraftforge.common.*;
import net.minecraft.block.material.*;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.init.*;
import net.minecraft.enchantment.*;
import java.util.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.*;
import net.minecraft.stats.*;
import plus.fserver.api.IBlockEAccess;


public class ItemShears extends Item {
    public ItemShears() {
        this.setMaxStackSize(1);
        this.setMaxDamage(238);
        this.setCreativeTab(CreativeTabs.TOOLS);
    }


    @Override
    public boolean onBlockDestroyed(final ItemStack stack, final World worldIn, final IBlockState state, final BlockPos pos, final EntityLivingBase entityLiving) {
        if (!worldIn.isRemote) {
            stack.damageItem(1, entityLiving);
        }
        final Block block = state.getBlock();
        return block instanceof IShearable || state.getMaterial() == Material.LEAVES || block == Blocks.WEB || block == Blocks.TALLGRASS || block == Blocks.VINE || block == Blocks.TRIPWIRE || block == Blocks.WOOL || super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }


    @Override
    public boolean canHarvestBlock(final IBlockState blockIn) {
        final Block block = blockIn.getBlock();
        return block == Blocks.WEB || block == Blocks.REDSTONE_WIRE || block == Blocks.TRIPWIRE;
    }


    @Override
    public boolean itemInteractionForEntity(final ItemStack stack, final EntityPlayer playerIn, final EntityLivingBase target0, final EnumHand hand) {
        if (target0.world.isRemote) {
            return false;
        }
        if (target0 instanceof IShearable) {
            final IBlockEAccess access = target0.world.access();
            final IShearable target = (IShearable)target0;
            final BlockPos pos = new BlockPos(target0.posX, target0.posY, target0.posZ);
            if (target.isShearable(stack, access, pos)) {
                final List<ItemStack> drops = target.onSheared(stack, access, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));
                final Random rand = target0.world.rand;
                for (final ItemStack dropStack : drops) {
                    final EntityItem entityDropItem;
                    final EntityItem ent = entityDropItem = target0.entityDropItem(dropStack, 1.0f);
                    entityDropItem.motionY += rand.nextFloat() * 0.05f;
                    final EntityItem entityItem = ent;
                    entityItem.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1f;
                    final EntityItem entityItem2 = ent;
                    entityItem2.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1f;
                }
                stack.damageItem(1, target0);
            }
            return true;
        }
        return false;
    }


    @Override
    public boolean onBlockStartBreak(final ItemStack stack, final BlockPos pos, final EntityPlayer player) {
        if (player.world.isRemote || player.capabilities.isCreativeMode) {
            return false;
        }
        IBlockEAccess access = player.world.access();
        final Block block = access.getBlockState(pos).getBlock();
        if (block instanceof IShearable) {
            final IShearable target = (IShearable)block;
            if (target.isShearable(stack, access, pos)) {
                final List<ItemStack> drops = target.onSheared(stack, access, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));
                final Random rand = player.world.rand;

                for (final ItemStack dropStack : drops) {
                    final float f = 0.7f;
                    final double d = rand.nextFloat() * f + (1.0f - f) * 0.5;
                    final double d2 = rand.nextFloat() * f + (1.0f - f) * 0.5;
                    final double d3 = rand.nextFloat() * f + (1.0f - f) * 0.5;

                    final EntityItem entityitem = new EntityItem(player.world, pos.getX() + d, pos.getY() + d2, pos.getZ() + d3, dropStack);
                    entityitem.setDefaultPickupDelay();
                    player.world.spawnEntity(entityitem);
                }

                stack.damageItem(1, player);
                player.addStat(StatList.getBlockStats(block));
                access.setBlockState(pos, BlockIDIndexes.AIR_STATE, 11);
                return true;
            }
        }
        return false;
    }


    @Override
    public float getDestroySpeed(final ItemStack stack, final IBlockState state) {
        final Block block = state.getBlock();
        if (block != Blocks.WEB && state.getMaterial() != Material.LEAVES) {
            return (block == Blocks.WOOL) ? 5.0f : super.getDestroySpeed(stack, state);
        }
        return 15.0f;
    }
}