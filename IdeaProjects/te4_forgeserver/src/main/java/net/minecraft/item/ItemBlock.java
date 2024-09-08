package net.minecraft.item;

import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.entity.*;
import net.minecraft.block.state.*;
import net.minecraft.block.*;
import javax.annotation.*;
import net.minecraft.nbt.*;
import net.minecraft.server.*;
import net.minecraft.tileentity.*;
import net.minecraft.init.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.creativetab.*;
import net.minecraft.util.*;
import java.util.*;
import net.minecraft.client.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.advancements.*;
import plus.fserver.api.IBlockEAccess;


public class ItemBlock extends Item {
    protected final Block block;

    public ItemBlock(final Block block) {
        this.block = block;
    }


    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final IBlockEAccess access = world.access();

        if (!access.getBlockState(pos).getBlock().isReplaceable(access, pos)) {
            pos = pos.offset(facing);
        }
        final ItemStack itemstack = player.getHeldItem(hand);
        if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack) && world.mayPlace(this.block, pos, false, facing, player)) {
            final int i = this.getMetadata(itemstack.getMetadata());
            IBlockState iblockstate2 = this.block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, i, player, hand);
            if (this.placeBlockAt(itemstack, player, world, pos, facing, hitX, hitY, hitZ, iblockstate2)) {
                iblockstate2 = access.getBlockState(pos);
                final SoundType soundtype = iblockstate2.getBlock().getSoundType(iblockstate2, world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0f) / 2.0f, soundtype.getPitch() * 0.8f);
                itemstack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }


    public static boolean setTileEntityNBT(final World worldIn, @Nullable final EntityPlayer player, final BlockPos pos, final ItemStack stackIn) {
        final MinecraftServer minecraftserver = worldIn.getMinecraftServer();
        if (minecraftserver == null) {
            return false;
        }
        final NBTTagCompound nbttagcompound = stackIn.getSubCompound("BlockEntityTag");
        if (nbttagcompound != null) {
            final TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity != null) {
                if (!worldIn.isRemote && tileentity.onlyOpsCanSetNbt() && (player == null || !player.canUseCommandBlock())) {
                    return false;
                }
                final NBTTagCompound nbttagcompound2 = tileentity.writeToNBT(new NBTTagCompound());
                final NBTTagCompound nbttagcompound3 = nbttagcompound2.copy();
                nbttagcompound2.merge(nbttagcompound);
                nbttagcompound2.setInteger("x", pos.getX());
                nbttagcompound2.setInteger("y", pos.getY());
                nbttagcompound2.setInteger("z", pos.getZ());
                if (!nbttagcompound2.equals(nbttagcompound3)) {
                    tileentity.readFromNBT(nbttagcompound2);
                    tileentity.markDirty();
                    return true;
                }
            }
        }
        return false;
    }


    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(final World worldIn, BlockPos pos, EnumFacing side, final EntityPlayer player, final ItemStack stack) {
        final Block block = worldIn.getBlockState(pos).getBlock();
        if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos)) {
            side = EnumFacing.UP;
        }
        else if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(side);
        }
        return worldIn.mayPlace(this.block, pos, false, side, player);
    }


    @Override
    public String getTranslationKey(final ItemStack stack) {
        return this.block.getTranslationKey();
    }


    @Override
    public String getTranslationKey() {
        return this.block.getTranslationKey();
    }


    @Override
    public CreativeTabs getCreativeTab() {
        return this.block.getCreativeTab();
    }


    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            this.block.getSubBlocks(tab, items);
        }
    }


    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, final ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, (List)tooltip, flagIn);
        this.block.addInformation(stack, worldIn, (List)tooltip, flagIn);
    }


    public Block getBlock() {
        return (this.getBlockRaw() == null) ? null : this.getBlockRaw().delegate.get();
    }


    private Block getBlockRaw() {
        return this.block;
    }


    public boolean placeBlockAt(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing facing, final float fx, final float fy, final float fz, final IBlockState state) {
        if (!world.setBlockState(pos, state, 11)) return false;
        final IBlockState curState = world.getBlockState(pos);
        if (curState.getBlock() == this.block) {
            setTileEntityNBT(world, player, pos, stack);
            this.block.onBlockPlacedBy(world, pos, curState, player, stack);
            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack, curState);
            }
        }
        return true;
    }
}