package net.minecraft.enchantment;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.fserver.ElcaCV;
import plus.fserver.server.elca.ChunkSelector;


public class EnchantmentFrostWalker extends Enchantment {
    public EnchantmentFrostWalker(Enchantment.Rarity rarityIn, EntityEquipmentSlot... slots) {
        super(rarityIn, EnumEnchantmentType.ARMOR_FEET, slots);
        this.setName("frostWalker");
    }


    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return enchantmentLevel * 10;
    }


    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 15;
    }


    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }


    @Override
    public int getMaxLevel() {
        return 2;
    }


    //is server side only
    public static void freezeNearby(EntityLivingBase living, World worldIn, BlockPos pos, int level) {
        if (living.onGround) {
            ChunkSelector selector = worldIn.localSelector;
            float f = (float)Math.min(ElcaCV.MAX_FROST_WALK_RANGE, 2 + level);

            for (BlockPos.MutableBlockPos mut : BlockPos.getAllInBoxMutable(pos.add(-f, -1.0D, -f), pos.add(f, -1.0D, f))) {

                if (mut.distanceSqToCenter(living.posX, living.posY, living.posZ) <= (double)(f * f)) {
                    IBlockState state = selector.getState(mut.getX(), mut.getY() + 1, mut.getZ());

                    if (state.getMaterial() == Material.AIR) {
                        IBlockState toFreeze = selector.getState(mut);

                        if (toFreeze.getMaterial() == Material.WATER && (toFreeze.getBlock() == net.minecraft.init.Blocks.WATER
                                || toFreeze.getBlock() == net.minecraft.init.Blocks.FLOWING_WATER) && toFreeze.getValue(BlockLiquid.LEVEL) == 0 &&
                                worldIn.mayPlace(Blocks.FROSTED_ICE, mut, false, EnumFacing.DOWN, (Entity)null)) {
                            selector.setBlockState(mut, Blocks.FROSTED_ICE.getDefaultState());

                            worldIn.scheduleUpdate(mut.toImmutable(), Blocks.FROSTED_ICE, MathHelper.getInt(living.getRNG(), 60, 120));
                        }
                    }
                }
            }
        }
    }


    @Override
    public boolean canApplyTogether(Enchantment ench) {
        return super.canApplyTogether(ench) && ench != Enchantments.DEPTH_STRIDER;
    }
}
