package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.block.BlockFx;
import plus.fserver.server.ElcaThread;


public class BlockCake extends BlockFx {
    public static final PropertyInteger BITES = PropertyInteger.create("bites", 0, 6);
    protected static final AxisAlignedBB[] CAKE_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.5, 0.9375), new AxisAlignedBB(0.1875, 0.0, 0.0625, 0.9375, 0.5, 0.9375), new AxisAlignedBB(0.3125, 0.0, 0.0625, 0.9375, 0.5, 0.9375), new AxisAlignedBB(0.4375, 0.0, 0.0625, 0.9375, 0.5, 0.9375), new AxisAlignedBB(0.5625, 0.0, 0.0625, 0.9375, 0.5, 0.9375), new AxisAlignedBB(0.6875, 0.0, 0.0625, 0.9375, 0.5, 0.9375), new AxisAlignedBB(0.8125, 0.0, 0.0625, 0.9375, 0.5, 0.9375)};

    protected BlockCake() {
        super(Material.CAKE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BITES, 0));
        this.setTickRandomly(true);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        return CAKE_AABB[state.getValue(BITES)];
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing p_180639_6_, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            return this.eatCake(world, pos, state, player);
        } else {
            ItemStack stack = player.getHeldItem(hand);
            return this.eatCake(world, pos, state, player) || stack.isEmpty();
        }
    }


    private boolean eatCake(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!player.canEat(false)) {
            return false;
        } else {
            player.addStat(StatList.CAKE_SLICES_EATEN);
            player.getFoodStats().addStats(2, 0.1F);
            int bites = state.getValue(BITES);
            if (bites < 6) {
                world.setBlockState(pos, state.withProperty(BITES, bites + 1), 3);
            } else {
                world.setBlockToAir(pos);
            }
            return true;
        }
    }


    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        ElcaThread.Cache cache = ElcaThread.cache();
        return super.canPlaceBlockAtAsy(cache, pos) && this.canBlockStay(cache, pos);
    }


    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos pos1) {
        ElcaThread.Cache cache = ElcaThread.cache();
        if (!this.canBlockStay(cache, pos)) {
            cache.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
        }
    }


    private boolean canBlockStay(IBlockEAccess access, BlockPos pos) {
        return access.getBlockState(pos.getX(), pos.getY()-1, pos.getZ()).getMaterial().isSolid();
    }


    @Override
    public int quantityDropped(Random rand) {
        return 0;
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }


    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(Items.CAKE);
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BITES, meta);
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BITES);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BITES);
    }


    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return (7 - state.getValue(BITES)) * 2;
    }


    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
