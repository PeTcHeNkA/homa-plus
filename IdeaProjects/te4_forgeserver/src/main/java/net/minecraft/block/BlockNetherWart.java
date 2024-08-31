package net.minecraft.block;

import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.*;
import net.minecraftforge.common.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.block.state.*;
import plus.fserver.utls.math.FastRandom;


public class BlockNetherWart extends BlockBush {
    public static final PropertyInteger AGE;
    private static final AxisAlignedBB[] NETHER_WART_AABB;

    protected BlockNetherWart() {
        super(Material.PLANTS, MapColor.RED);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockNetherWart.AGE, 0));
        this.setTickRandomly(true);
        this.setCreativeTab(null);
    }


    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return BlockNetherWart.NETHER_WART_AABB[state.getValue(BlockNetherWart.AGE)];
    }


    @Override
    protected boolean canSustainBush(final IBlockState state) {
        return state.getBlock() == Blocks.SOUL_SAND;
    }


    @Override
    public boolean canBlockStay(final World worldIn, final BlockPos pos, final IBlockState state) {
        return super.canBlockStay(worldIn, pos, state);
    }


    @Override
    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand) {
        final int i = state.getValue(BlockNetherWart.AGE);
        if (i < 3 && ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt(10) == 0)) {
            final IBlockState newState = state.withProperty(BlockNetherWart.AGE, i + 1);
            worldIn.access().setBlockState(pos, newState, 2);
            ForgeHooks.onCropsGrowPost(worldIn, pos, state, newState);
        }
        super.updateTick(worldIn, pos, state, rand);
    }


    @Override
    public void dropBlockAsItemWithChance(final World worldIn, final BlockPos pos, final IBlockState state, final float chance, final int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }


    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return Items.AIR;
    }


    @Override
    public int quantityDropped(final Random random) {
        return 0;
    }


    @Override
    public ItemStack getItem(final World worldIn, final BlockPos pos, final IBlockState state) {
        return new ItemStack(Items.NETHER_WART);
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockNetherWart.AGE, meta);
    }


    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(BlockNetherWart.AGE);
    }


    @Override
    public void getDrops(final NonNullList<ItemStack> drops, final IBlockAccess access, final BlockPos p_getDrops_3_, final IBlockState p_getDrops_4_, final int p_getDrops_5_) {
        final Random rand = (access instanceof World) ? ((World)access).rand : FastRandom.INST;
        int count = 1;
        if (p_getDrops_4_.getValue(BlockNetherWart.AGE) >= 3) {
            count = 2 + rand.nextInt(3) + ((p_getDrops_5_ > 0) ? rand.nextInt(p_getDrops_5_ + 1) : 0);
        }
        for (int i = 0; i < count; ++i) {
            drops.add(new ItemStack(Items.NETHER_WART));
        }
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockNetherWart.AGE);
    }


    static {
        AGE = PropertyInteger.create("age", 0, 3);
        NETHER_WART_AABB = new AxisAlignedBB[] { new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.3125, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.6875, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.875, 1.0) };
    }
}