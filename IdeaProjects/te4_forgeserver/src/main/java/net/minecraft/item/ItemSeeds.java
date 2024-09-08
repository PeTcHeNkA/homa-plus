//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\ExtraApps\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package net.minecraft.item;

import net.minecraft.block.*;
import net.minecraft.creativetab.*;
import net.minecraft.util.math.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;
import net.minecraft.advancements.*;
import net.minecraft.block.state.*;
import net.minecraftforge.common.*;
import net.minecraft.init.*;
import plus.fserver.api.IBlockEAccess;


public class ItemSeeds extends Item implements IPlantable {
    private final Block crops;
    private final Block soilBlockID;

    public ItemSeeds(final Block crops, final Block soil) {
        this.crops = crops;
        this.soilBlockID = soil;
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }


    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final ItemStack itemstack = player.getHeldItem(hand);
        final IBlockEAccess access = world.access();
        IBlockState state = access.getBlockState(pos);
        final BlockPos up;
        if (facing == EnumFacing.UP && player.canPlayerEdit(pos.offset(facing), facing, itemstack) && state.getBlock().canSustainPlant(state, access, pos, EnumFacing.UP, this) && access.isAirBlock(up = pos.up())) {
            access.setBlockState(up, state = this.crops.getDefaultState(), 3);
            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, up, itemstack, state);
            }
            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }


    @Override
    public EnumPlantType getPlantType(final IBlockAccess access, final BlockPos pos) {
        return (this.crops == Blocks.NETHER_WART) ? EnumPlantType.Nether : EnumPlantType.Crop;
    }


    @Override
    public IBlockState getPlant(final IBlockAccess access, final BlockPos pos) {
        return this.crops.getDefaultState();
    }
}