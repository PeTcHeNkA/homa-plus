package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import plus.fserver.utls.collections.ArrayListE6;
import static net.minecraft.block.BlockFs.addCollisionBoxToListFs;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class BlockDaylightDetector extends BlockContainer {
    public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
    protected static final AxisAlignedBB DAYLIGHT_DETECTOR_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D);
    private final boolean inverted;

    public BlockDaylightDetector(boolean inverted) {
        super(Material.WOOD);
        this.inverted = inverted;
        this.setDefaultState(this.blockState.getBaseState().withProperty(POWER, 0));
        this.setCreativeTab(CreativeTabs.REDSTONE);
        this.setHardness(0.2F);
        this.setSoundType(SoundType.WOOD);
        this.setTranslationKey("daylightDetector");
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(access, pos));
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return DAYLIGHT_DETECTOR_AABB;
    }


    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return blockState.getValue(POWER).intValue();
    }


    public void updatePower(World worldIn, BlockPos pos) {
        if (worldIn.provider.hasSkyLight()) {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            int i = worldIn.getLightFor(EnumSkyBlock.SKY, pos) - worldIn.getSkylightSubtracted();
            float f = worldIn.getCelestialAngleRadians(1.0F);

            if (this.inverted) {
                i = 15 - i;
            }

            if (i > 0 && !this.inverted) {
                float f1 = f < (float)Math.PI ? 0.0F : ((float)Math.PI * 2F);
                f = f + (f1 - f) * 0.2F;
                i = Math.round((float)i * MathHelper.cos(f));
            }

            i = MathHelper.clamp(i, 0, 15);

            if (iblockstate.getValue(POWER) != i) {
                worldIn.setBlockState(pos, iblockstate.withProperty(POWER, i), 3);
            }
        }
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (playerIn.isAllowEdit()) {
            if (worldIn.isRemote || !SERVER_COMPILE) {
                return true;
            } else {
                if (this.inverted) {
                    worldIn.setBlockState(pos, Blocks.DAYLIGHT_DETECTOR.getDefaultState().withProperty(POWER, state.getValue(POWER)), 4);
                    Blocks.DAYLIGHT_DETECTOR.updatePower(worldIn, pos);
                } else {
                    worldIn.setBlockState(pos, Blocks.DAYLIGHT_DETECTOR_INVERTED.getDefaultState().withProperty(POWER, state.getValue(POWER)), 4);
                    Blocks.DAYLIGHT_DETECTOR_INVERTED.updatePower(worldIn, pos);
                }
                return true;
            }
        } else {
            return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        }
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR);
    }


    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(Blocks.DAYLIGHT_DETECTOR);
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
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }


    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }


    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityDaylightDetector();
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(POWER, Integer.valueOf(meta));
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(POWER);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {POWER});
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        if (!this.inverted) {
            super.getSubBlocks(itemIn, items);
        }
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
}