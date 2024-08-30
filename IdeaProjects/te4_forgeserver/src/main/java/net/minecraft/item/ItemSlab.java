package net.minecraft.item;

import net.minecraft.block.properties.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.advancements.*;
import net.minecraft.block.state.*;
import net.minecraft.util.math.*;
import net.minecraft.block.*;
import net.minecraftforge.fml.relauncher.*;
import plus.fserver.api.IBlockEAccess;


public class ItemSlab extends ItemBlock {
    private final BlockSlab singleSlab;
    private final BlockSlab doubleSlab;

    public ItemSlab(final Block block, final BlockSlab singleSlab, final BlockSlab doubleSlab) {
        super(block);
        this.singleSlab = singleSlab;
        this.doubleSlab = doubleSlab;
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }


    @Override
    public int getMetadata(final int damage) {
        return damage;
    }


    @Override
    public String getTranslationKey(final ItemStack stack) {
        return this.singleSlab.getTranslationKey(stack.getMetadata());
    }


    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final ItemStack itemstack = player.getHeldItem(hand);
        if (!itemstack.isEmpty() && player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
            final Comparable<?> comparable = this.singleSlab.getTypeForItem(itemstack);
            final IBlockEAccess access = world.access();
            final IBlockState iblockstate = access.getBlockState(pos);
            if (iblockstate.getBlock() == this.singleSlab) {
                final IProperty<?> iproperty = this.singleSlab.getVariantProperty();
                final Comparable<?> comparable2 = iblockstate.getValue(iproperty);
                final BlockSlab.EnumBlockHalf half = iblockstate.getValue(BlockSlab.HALF);

                if (((facing == EnumFacing.UP && half == BlockSlab.EnumBlockHalf.BOTTOM) || (facing == EnumFacing.DOWN && half == BlockSlab.EnumBlockHalf.TOP)) && comparable2 == comparable) {
                    final IBlockState iblockstate2 = this.makeState(iproperty, comparable2);
                    final AxisAlignedBB axisalignedbb = iblockstate2.getCollisionBoundingBox(access, pos);
                    if (axisalignedbb != Block.NULL_AABB && world.checkNoEntityCollision(axisalignedbb.offset(pos)) && access.setBlockState(pos, iblockstate2, 11)) {
                        final SoundType soundtype = this.doubleSlab.getSoundType(iblockstate2, world, pos, player);
                        world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0f) / 2.0f, soundtype.getPitch() * 0.8f);
                        itemstack.shrink(1);
                        if (player instanceof EntityPlayerMP) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, itemstack);
                        }
                    }
                    return EnumActionResult.SUCCESS;
                }
            }
            return this.tryPlace(access, player, itemstack, world, pos.offset(facing), comparable) ? EnumActionResult.SUCCESS : super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        }
        return EnumActionResult.FAIL;
    }


    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(final World worldIn, BlockPos pos, final EnumFacing side, final EntityPlayer player, final ItemStack stack) {
        final BlockPos blockpos = pos;
        final IProperty<?> property = this.singleSlab.getVariantProperty();
        final Comparable<?> comparable = this.singleSlab.getTypeForItem(stack);
        final IBlockState iblockstate = worldIn.getBlockState(pos);
        if (iblockstate.getBlock() == this.singleSlab) {
            final boolean flag = iblockstate.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
            if (((side == EnumFacing.UP && !flag) || (side == EnumFacing.DOWN && flag)) && comparable == iblockstate.getValue(property)) {
                return true;
            }
        }
        pos = pos.offset(side);
        final IBlockState iblockstate2 = worldIn.getBlockState(pos);
        return (iblockstate2.getBlock() == this.singleSlab && comparable == iblockstate2.getValue(property)) || super.canPlaceBlockOnSide(worldIn, blockpos, side, player, stack);
    }


    private boolean tryPlace(final IBlockEAccess access, final EntityPlayer player, final ItemStack stack, final World world, final BlockPos pos, final Object type) {
        IBlockState state = access.getBlockState(pos);
        if (state.getBlock() == this.singleSlab) {
            final Comparable<?> comparable = state.getValue(this.singleSlab.getVariantProperty());
            if (comparable == type) {
                state = this.makeState(this.singleSlab.getVariantProperty(), comparable);
                final AxisAlignedBB axisalignedbb = state.getCollisionBoundingBox(access, pos);
                if (axisalignedbb != Block.NULL_AABB && world.checkNoEntityCollision(axisalignedbb.offset(pos)) && access.setBlockState(pos, state, 11)) {
                    final SoundType soundtype = this.doubleSlab.getSoundType(state, world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0f) / 2.0f, soundtype.getPitch() * 0.8f);
                    stack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }


    protected <T extends Comparable<T>> IBlockState makeState(final IProperty<T> property, final Comparable<?> comparable) {
        return this.doubleSlab.getDefaultState().withProperty(property, (T)comparable);
    }
}