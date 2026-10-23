package net.minecraft.item;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.IBlockEAccess;


public class ItemSpade extends ItemTool {
    private static final Set<Block> EFFECTIVE_ON;

    public ItemSpade(Item.ToolMaterial p_i45353_1_) {
        super(1.5F, -3.0F, p_i45353_1_, EFFECTIVE_ON);
    }


    @Override
    public boolean canHarvestBlock(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.SNOW_LAYER) {
            return true;
        } else {
            return block == Blocks.SNOW;
        }
    }


    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return EnumActionResult.FAIL;
        } else {
            final IBlockEAccess access = world.access();
            IBlockState state = access.getBlockState(pos);
            Block block = state.getBlock();
            if (facing != EnumFacing.DOWN && access.getBlockState(pos.getX(), pos.getY()+1, pos.getZ()).getMaterial() == Material.AIR && block == Blocks.GRASS) {
                IBlockState state2 = Blocks.GRASS_PATH.getDefaultState();
                world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!world.isRemote) {
                    access.setBlockState(pos, state2, 11);
                    stack.damageItem(1, player);
                }
                return EnumActionResult.SUCCESS;
            } else {
                return EnumActionResult.PASS;
            }
        }
    }

    static {
        EFFECTIVE_ON = Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.CONCRETE_POWDER);
    }
}