package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockTripWire extends Block {
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyBool ATTACHED = PropertyBool.create("attached");
    public static final PropertyBool DISARMED = PropertyBool.create("disarmed");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    protected static final AxisAlignedBB AABB = new AxisAlignedBB(0.0, 0.0625, 0.0, 1.0, 0.15625, 1.0);
    protected static final AxisAlignedBB TRIP_WRITE_ATTACHED_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

    public BlockTripWire() {
        super(Material.CIRCUITS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, false).withProperty(ATTACHED, false).withProperty(DISARMED, false).withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false).withProperty(WEST, false));
        this.setTickRandomly(true);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        return !state.getValue(ATTACHED) ? TRIP_WRITE_ATTACHED_AABB : AABB;
    }


    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        return state.withProperty(NORTH, isConnectedTo(access, pos, state, EnumFacing.NORTH)).withProperty(EAST, isConnectedTo(access, pos, state, EnumFacing.EAST)).withProperty(SOUTH, isConnectedTo(access, pos, state, EnumFacing.SOUTH)).withProperty(WEST, isConnectedTo(access, pos, state, EnumFacing.WEST));
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState p_180646_1_, IBlockAccess p_180646_2_, BlockPos p_180646_3_) {
        return NULL_AABB;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }


    @Override
    public Item getItemDropped(IBlockState p_180660_1_, Random p_180660_2_, int p_180660_3_) {
        return Items.STRING;
    }


    @Override
    public ItemStack getItem(World p_185473_1_, BlockPos p_185473_2_, IBlockState p_185473_3_) {
        return new ItemStack(Items.STRING);
    }


    @Override
    public void onBlockAdded(World p_176213_1_, BlockPos p_176213_2_, IBlockState p_176213_3_) {
        p_176213_1_.setBlockState(p_176213_2_, p_176213_3_, 3);
        this.notifyHook(p_176213_1_, p_176213_2_, p_176213_3_);
    }


    @Override
    public void breakBlock(World p_180663_1_, BlockPos p_180663_2_, IBlockState p_180663_3_) {
        this.notifyHook(p_180663_1_, p_180663_2_, p_180663_3_.withProperty(POWERED, true));
    }


    @Override
    public void onBlockHarvested(World p_176208_1_, BlockPos p_176208_2_, IBlockState p_176208_3_, EntityPlayer p_176208_4_) {
        if (!p_176208_1_.isRemote) {
            if (!p_176208_4_.getHeldItemMainhand().isEmpty() && p_176208_4_.getHeldItemMainhand().getItem() == Items.SHEARS) {
                p_176208_1_.setBlockState(p_176208_2_, p_176208_3_.withProperty(DISARMED, true), 4);
            }

        }
    }


    private void notifyHook(World world, BlockPos pos, IBlockState state) {
        for (EnumFacing facing : new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.WEST}) {
            for (int i = 1; i < 42; ++i) {
                BlockPos pos2 = pos.offset(facing, i);
                IBlockState state2 = world.access().getBlockState(pos2);
                if (state2.getBlock() == Blocks.TRIPWIRE_HOOK) {
                    if (state2.getValue(BlockTripWireHook.FACING) == facing.getOpposite()) {
                        Blocks.TRIPWIRE_HOOK.calculateState(world, pos2, state2, false, true, i, state);
                    }
                    break;
                }
                if (state2.getBlock() != Blocks.TRIPWIRE) {
                    break;
                }
            }
        }
    }


    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!world.isRemote) {
            if (!state.getValue(POWERED)) {
                this.updateState(world, pos);
            }
        }
    }


    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random rand) {}


    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {
            if (world.getBlockState(pos).getValue(POWERED)) {
                this.updateState(world, pos);
            }
        }
    }


    private void updateState(World world, BlockPos pos) {
        IBlockState state = world.access().getBlockState(pos);
        boolean powered = state.getValue(POWERED);
        boolean curState = false;
        List<? extends Entity> list =
                world.getEntitiesInAABBexcludingLimited(null,
                        state.getBoundingBox(world, pos).offset(pos), EntitySelectors.NOT_SPECTATING, 5
                );
        if (!list.isEmpty()) {
            if(list.size() >= 5){
                curState = true;
            } else {
                for (Entity entity : list) {
                    if (!entity.doesEntityNotTriggerPressurePlate()) {
                        curState = true;
                        break;
                    }
                }
            }
        }
        if (curState != powered) {
            state = state.withProperty(POWERED, curState);
            world.access().setBlockState(pos, state, 3);
            this.notifyHook(world, pos, state);
        }

        if (curState) {
            world.scheduleUpdate(new BlockPos(pos), this, this.tickRate(world));
        }
    }


    public static boolean isConnectedTo(IBlockAccess access, BlockPos pos, IBlockState state, EnumFacing facing) {
        IBlockState state2 = access.getBlockState(pos.offset(facing));
        Block block = state2.getBlock();
        if (block == Blocks.TRIPWIRE_HOOK) {
            return state2.getValue(BlockTripWireHook.FACING) == facing.getOpposite();
        } else {
            return block == Blocks.TRIPWIRE;
        }
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(POWERED, (meta & 1) > 0).withProperty(ATTACHED, (meta & 4) > 0).withProperty(DISARMED, (meta & 8) > 0);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (state.getValue(POWERED)) {
            meta |= 1;
        }
        if (state.getValue(ATTACHED)) {
            meta |= 4;
        }
        if (state.getValue(DISARMED)) {
            meta |= 8;
        }
        return meta;
    }


    @Nullable
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


    @Nullable
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH));
            case FRONT_BACK:
                return state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST));
            default:
                return super.withMirror(state, mirror);
        }
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
    }


    @Nullable
    public BlockFaceShape getBlockFaceShape(IBlockAccess p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_) {
        return BlockFaceShape.UNDEFINED;
    }
}