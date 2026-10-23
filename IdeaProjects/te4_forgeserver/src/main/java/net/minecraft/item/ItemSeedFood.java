package net.minecraft.item;

import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.block.state.*;
import net.minecraftforge.common.*;
import plus.fserver.api.IBlockEAccess;


public class ItemSeedFood extends ItemFood implements IPlantable {
    private final Block crops;
    private final Block soilId;

    public ItemSeedFood(final int healAmount, final float saturation, final Block crops, final Block soil) {
        super(healAmount, saturation, false);
        this.crops = crops;
        this.soilId = soil;
    }


    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final ItemStack itemstack = player.getHeldItem(hand);
        final IBlockEAccess access = world.access();
        final IBlockState state = access.getBlockState(pos);
        final BlockPos up;
        if (facing == EnumFacing.UP && player.canPlayerEdit(pos.offset(facing), facing, itemstack) && state.getBlock().canSustainPlant(state, access, pos, EnumFacing.UP, this) && access.isAirBlock(up = pos.up())) {
            access.setBlockState(up, this.crops.getDefaultState(), 11);
            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }


    @Override
    public EnumPlantType getPlantType(final IBlockAccess access, final BlockPos pos) {
        return EnumPlantType.Crop;
    }


    @Override
    public IBlockState getPlant(final IBlockAccess access, final BlockPos pos) {
        return this.crops.getDefaultState();
    }
}