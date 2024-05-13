package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.Keep;
import plus.fserver.block.BlockFs;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.BlockPosDelta;
import plus.fserver.world.RedstoneEngine;

import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class BlockRedstoneWire extends BlockFs {
    @Keep
    public static final PropertyEnum<BlockRedstoneWire.EnumAttachPosition> NORTH = PropertyEnum.create("north", BlockRedstoneWire.EnumAttachPosition.class);
    @Keep
    public static final PropertyEnum<BlockRedstoneWire.EnumAttachPosition> EAST = PropertyEnum.create("east", BlockRedstoneWire.EnumAttachPosition.class);
    @Keep
    public static final PropertyEnum<BlockRedstoneWire.EnumAttachPosition> SOUTH = PropertyEnum.create("south", BlockRedstoneWire.EnumAttachPosition.class);
    @Keep
    public static final PropertyEnum<BlockRedstoneWire.EnumAttachPosition> WEST = PropertyEnum.create("west", BlockRedstoneWire.EnumAttachPosition.class);
    @Keep
    public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
    @Keep
    protected static final AxisAlignedBB[] REDSTONE_WIRE_AABB = new AxisAlignedBB[] {
            new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.0625D, 0.8125D),
            new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.0625D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.1875D, 0.8125D, 0.0625D, 0.8125D),
            new AxisAlignedBB(0.0D, 0.0D, 0.1875D, 0.8125D, 0.0625D, 1.0D),
            new AxisAlignedBB(0.1875D, 0.0D, 0.0D, 0.8125D, 0.0625D, 0.8125D),
            new AxisAlignedBB(0.1875D, 0.0D, 0.0D, 0.8125D, 0.0625D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.8125D, 0.0625D, 0.8125D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.8125D, 0.0625D, 1.0D),
            new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 1.0D, 0.0625D, 0.8125D),
            new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 1.0D, 0.0625D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.1875D, 1.0D, 0.0625D, 0.8125D),
            new AxisAlignedBB(0.0D, 0.0D, 0.1875D, 1.0D, 0.0625D, 1.0D),
            new AxisAlignedBB(0.1875D, 0.0D, 0.0D, 1.0D, 0.0625D, 0.8125D),
            new AxisAlignedBB(0.1875D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 0.8125D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D)
    };
    private final RedstoneEngine turbo;

    public BlockRedstoneWire() {
        super(Material.CIRCUITS);
        turbo = new RedstoneEngine(this);
        this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, BlockRedstoneWire.EnumAttachPosition.NONE).withProperty(EAST, BlockRedstoneWire.EnumAttachPosition.NONE).withProperty(SOUTH, BlockRedstoneWire.EnumAttachPosition.NONE).withProperty(WEST, BlockRedstoneWire.EnumAttachPosition.NONE).withProperty(POWER, Integer.valueOf(0)));
    }


    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return BlockRedstoneWire.REDSTONE_WIRE_AABB[getAABBIndex(state.getActualState(source, pos))];
    }


    private static int getAABBIndex(final IBlockState state) {
        int i = 0;
        final boolean flag = state.getValue(BlockRedstoneWire.NORTH) != BlockRedstoneWire.EnumAttachPosition.NONE;
        final boolean flag2 = state.getValue(BlockRedstoneWire.EAST) != BlockRedstoneWire.EnumAttachPosition.NONE;
        final boolean flag3 = state.getValue(BlockRedstoneWire.SOUTH) != BlockRedstoneWire.EnumAttachPosition.NONE;
        final boolean flag4 = state.getValue(BlockRedstoneWire.WEST) != BlockRedstoneWire.EnumAttachPosition.NONE;
        if (flag || (flag3 && !flag2 && !flag4)) {
            i |= 1 << EnumFacing.NORTH.getHorizontalIndex();
        }
        if (flag2 || (flag4 && !flag && !flag3)) {
            i |= 1 << EnumFacing.EAST.getHorizontalIndex();
        }
        if (flag3 || (flag && !flag2 && !flag4)) {
            i |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
        }
        if (flag4 || (flag2 && !flag && !flag3)) {
            i |= 1 << EnumFacing.WEST.getHorizontalIndex();
        }
        return i;
    }


    @Override
    public IBlockState getActualState(IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
        state = state.withProperty(BlockRedstoneWire.WEST, this.getAttachPosition(worldIn, pos, EnumFacing.WEST));
        state = state.withProperty(BlockRedstoneWire.EAST, this.getAttachPosition(worldIn, pos, EnumFacing.EAST));
        state = state.withProperty(BlockRedstoneWire.NORTH, this.getAttachPosition(worldIn, pos, EnumFacing.NORTH));
        state = state.withProperty(BlockRedstoneWire.SOUTH, this.getAttachPosition(worldIn, pos, EnumFacing.SOUTH));
        return state;
    }


    private BlockRedstoneWire.EnumAttachPosition getAttachPosition(final IBlockAccess access, final BlockPos pos, final EnumFacing direction) {
        final BlockPos blockpos = pos.offset(direction);
        final IBlockState state = access.getBlockState(blockpos);
        if (!canConnectTo(state, direction, access, blockpos) && (state.isNormalCube() || !canConnectUpwardsTo(access, blockpos.down()))) {
            final IBlockState iblockstate2 = access.getBlockState(pos.up());
            if (!iblockstate2.isNormalCube()) {
                final boolean flag = state.isSideSolid(access, blockpos, EnumFacing.UP)
                        || state.getBlock() == Blocks.GLOWSTONE;
                if (flag && canConnectUpwardsTo(access, blockpos.up())) {
                    if (state.isBlockNormalCube()) {
                        return BlockRedstoneWire.EnumAttachPosition.UP;
                    }
                    return BlockRedstoneWire.EnumAttachPosition.SIDE;
                }
            }
            return BlockRedstoneWire.EnumAttachPosition.NONE;
        }
        return BlockRedstoneWire.EnumAttachPosition.SIDE;
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos) {
        return BlockRedstoneWire.NULL_AABB;
    }


    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }


    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }


    @Override
    public boolean canPlaceBlockAt(final World worldIn, BlockPos pos) {
        IBlockAccess access = worldIn.access();
        final IBlockState downState = access.getBlockState(pos = pos.down());
        return downState.isTopSolid() || downState.getBlockFaceShape(worldIn, pos, EnumFacing.UP) == BlockFaceShape.SOLID ||
                access.getBlockState(pos).getBlock() == Blocks.GLOWSTONE;
    }


    private void notifyWireNeighborsOfStateChange(final World worldIn, final BlockPos pos) {
        if (worldIn.getBlockState(pos).getBlock() == this) {
            worldIn.notifyNeighborsOfStateChange(pos, this, false);
            for (final EnumFacing enumfacing : EnumFacing.VALUES) {
                worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, false);
            }
        }
    }


    @Override
    public void onBlockAdded(final World worldIn, final BlockPos pos, final IBlockState state) {
        if (!worldIn.isRemote || SERVER_COMPILE)
            this.turbo.updateSurroundingRedstone(worldIn, pos, state, null);
    }


    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (!worldIn.isRemote || SERVER_COMPILE) {
            for (final EnumFacing enumfacing : EnumFacing.VALUES) {
                worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, false);
            }
            this.turbo.updateSurroundingRedstone(worldIn, pos, state, null);
            for (final EnumFacing facing : BlockIDIndexes.FACING_HORIZONTAL) {
                this.notifyWireNeighborsOfStateChange(worldIn, pos.offset(facing));
            }
            for (final EnumFacing facing : BlockIDIndexes.FACING_HORIZONTAL) {
                final BlockPos blockpos = pos.offset(facing);
                if (worldIn.getBlockState(blockpos).isNormalCube()) {
                    this.notifyWireNeighborsOfStateChange(worldIn, blockpos.up());
                }
                else {
                    this.notifyWireNeighborsOfStateChange(worldIn, blockpos.down());
                }
            }
        }
    }


    private int getMaxCurrentStrength(final World worldIn, final BlockPos pos, final int strength) {
        if (worldIn.getBlockState(pos).getBlock() != this) {
            return strength;
        }
        final int i = worldIn.getBlockState(pos).getValue(BlockRedstoneWire.POWER);
        return (i > strength) ? i : strength;
    }


    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        if (!worldIn.isRemote) {
            if (this.canPlaceBlockAt(worldIn, pos)) {
                this.turbo.updateSurroundingRedstone(worldIn, pos, state, fromPos);
            } else {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }
        }
    }


    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return Items.REDSTONE;
    }


    @Override
    public int getStrongPower(final IBlockState state, final IBlockAccess access, final BlockPos pos, final EnumFacing side) {
        return access.getWorld().redstoneCache.redstoneCanProvidePower ?
                getWeakPower(state, access, pos, side) : 0;
    }


    @Override
    public int getWeakPower(final IBlockState blockState, final IBlockAccess access, final BlockPos pos, final EnumFacing side) {
        if (!access.getWorld().redstoneCache.redstoneCanProvidePower) return 0;
        final int i = blockState.getValue(BlockRedstoneWire.POWER);
        if (i == 0) return 0;
        if (side == EnumFacing.UP) return i;

        int mask = 0;
        //todo unchecked
        ChunkSelector selector = access.getWorld().localSelector;
        BlockPosDelta delta = access.getWorld().redstoneCache.mutDelta;
        delta.setPos(pos.getX(), pos.getY(), pos.getZ());
        for (final EnumFacing enumfacing : BlockIDIndexes.FACING_HORIZONTAL)
            if (this.isPowerSourceAt(selector, delta, enumfacing))
                mask = mask | 1<< enumfacing.getIndex();

        return (side.getAxis().isHorizontal() && mask == 0 || ((mask >> side.getIndex()) & 1) == 1
                && ((mask >> side.rotateYCCW().getIndex()) & 1) == 0
                && ((mask >> side.rotateY().getIndex()) & 1) == 0
        )?i:0;
    }


    private boolean isPowerSourceAt(final ChunkSelector access, final BlockPosDelta pos, final EnumFacing side) {
        final IBlockState state = access.getState(pos.delta(side));
        final boolean flag = state.isNormalCube();
        return (!access.getState(pos.shiftY(1)).isNormalCube()
                && flag && canConnectUpwardsTo(access, pos))
                || canConnectTo(state, side, access, pos.reset())
                || (state.getBlock() == Blocks.POWERED_REPEATER
                && state.getValue(BlockRedstoneDiode.FACING) == side)
                || (!flag && canConnectUpwardsTo(access, pos.delta(side.getXOffset(), -1, side.getZOffset())));
    }


    protected static boolean canConnectUpwardsTo(final IBlockAccess access, final BlockPos pos) {
        final IBlockState blockState = access.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block == Blocks.REDSTONE_WIRE || block.canConnectRedstone(blockState, access, pos, null);
    }


    protected static boolean canConnectTo(final IBlockState blockState, @Nullable final EnumFacing side, final IBlockAccess world, final BlockPos pos) {
        final Block block = blockState.getBlock();
        if (block == Blocks.REDSTONE_WIRE) {
            return true;
        }

        if (Blocks.UNPOWERED_REPEATER == block || Blocks.POWERED_REPEATER == block) {
            final EnumFacing enumfacing = blockState.getValue(BlockRedstoneRepeater.FACING);
            return enumfacing == side || enumfacing.getOpposite() == side;
        }
//        if (Blocks.UNPOWERED_REPEATER.isSameDiode(blockState)) {
//            final EnumFacing enumfacing = blockState.getValue(BlockRedstoneRepeater.FACING);
//            return enumfacing == side || enumfacing.getOpposite() == side;
//        }
        if (Blocks.OBSERVER == block) {
            return side == blockState.getValue(BlockObserver.FACING);
        }
        return block.canConnectRedstone(blockState, world, pos, side);
    }


    @Override
    public boolean canProvidePower(final IBlockState state) {
        return true;
    }


    @Override
    public ItemStack getItem(final World worldIn, final BlockPos pos, final IBlockState state) {
        return new ItemStack(Items.REDSTONE);
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockRedstoneWire.POWER, meta);
    }


    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(BlockRedstoneWire.POWER);
    }


    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        switch (rot) {
            case CLOCKWISE_180:
                return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(EAST, state.getValue(WEST)).withProperty(SOUTH, state.getValue(NORTH)).withProperty(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(EAST)).withProperty(EAST, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(WEST)).withProperty(WEST, state.getValue(NORTH));
            case CLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(WEST)).withProperty(EAST, state.getValue(NORTH)).withProperty(SOUTH, state.getValue(EAST)).withProperty(WEST, state.getValue(SOUTH));
            default:
                return state;
        }
    }


    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        switch (mirrorIn) {
            case LEFT_RIGHT:
                return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH));
            case FRONT_BACK:
                return state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST));
            default:
                return super.withMirror(state, mirrorIn);
        }
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, NORTH, EAST, SOUTH, WEST, POWER);
    }


    @Override
    public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final IBlockState state, final BlockPos pos, final EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }


    public IBlockState calculateCurrentChanges(final World worldIn, final BlockPos pos1, final BlockPos pos2, IBlockState state) {
        final IBlockState iblockstate = state;
        final int i = state.getValue(BlockRedstoneWire.POWER);
        int l = 0;
        worldIn.redstoneCache.redstoneCanProvidePower = false;
        final int k = worldIn.getRedstonePowerFromNeighbors(pos1);
        worldIn.redstoneCache.redstoneCanProvidePower = true;
        if (k < 15) {
            for (final EnumFacing enumfacing : BlockIDIndexes.FACING_HORIZONTAL) {
                final BlockPos blockpos = pos1.offset(enumfacing);
                final boolean flag = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();
                if (flag) {
                    l = this.getMaxCurrentStrength(worldIn, blockpos, l);
                }
                if (worldIn.getBlockState(blockpos).isNormalCube() && !worldIn.getBlockState(pos1.up()).isNormalCube()) {
                    if (!flag || pos1.getY() < pos2.getY()) {
                        continue;
                    }
                    l = this.getMaxCurrentStrength(worldIn, blockpos.up(), l);
                } else {
                    if (worldIn.getBlockState(blockpos).isNormalCube() || !flag || pos1.getY() > pos2.getY()) {
                        continue;
                    }
                    l = this.getMaxCurrentStrength(worldIn, blockpos.down(), l);
                }
            }
        }
        int j = l - 1;
        if (k > j) {
            j = k;
        }
        if (i != j) {
            state = state.withProperty(BlockRedstoneWire.POWER, j);
            if (worldIn.getBlockState(pos1) == iblockstate) {
                worldIn.setBlockState(pos1, state, 2);
            }
        }
        return state;
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @SideOnly(Side.CLIENT)
    public static int colorMultiplier(int p_176337_0_) {
        float f = (float)p_176337_0_ / 15.0F;
        float f1 = f * 0.6F + 0.4F;

        if (p_176337_0_ == 0) {
            f1 = 0.3F;
        }

        float f2 = f * f * 0.7F - 0.5F;
        float f3 = f * f * 0.6F - 0.7F;

        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        if (f3 < 0.0F) {
            f3 = 0.0F;
        }
        int i = MathHelper.clamp((int)(f1 * 255.0F), 0, 255);
        int j = MathHelper.clamp((int)(f2 * 255.0F), 0, 255);
        int k = MathHelper.clamp((int)(f3 * 255.0F), 0, 255);
        return -16777216 | i << 16 | j << 8 | k;
    }


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        int i = stateIn.getValue(POWER);

        if (i != 0) {
            double d0 = (double)pos.getX() + 0.5D + ((double)rand.nextFloat() - 0.5D) * 0.2D;
            double d1 = (float)pos.getY() + 0.0625F;
            double d2 = (double)pos.getZ() + 0.5D + ((double)rand.nextFloat() - 0.5D) * 0.2D;
            float f = (float)i / 15.0F;
            float f1 = f * 0.6F + 0.4F;
            float f2 = Math.max(0.0F, f * f * 0.7F - 0.5F);
            float f3 = Math.max(0.0F, f * f * 0.6F - 0.7F);
            worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d0, d1, d2, f1, f2, f3);
        }
    }


    @Keep
    public static enum EnumAttachPosition implements IStringSerializable {
        UP("up"),
        SIDE("side"),
        NONE("none");

        private final String name;

        EnumAttachPosition(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return this.getName();
        }


        @Override
        public String getName() {
            return this.name;
        }
    }
}