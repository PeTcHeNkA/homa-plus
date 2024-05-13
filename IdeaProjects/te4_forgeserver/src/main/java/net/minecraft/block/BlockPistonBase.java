package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.api.Keep;
import plus.fserver.utls.collections.ArrayListE6;
import static plus.fserver.block.BlockFs.addCollisionBoxToListFs;


public class BlockPistonBase extends BlockDirectional {
    @Keep
    public static final PropertyBool EXTENDED = PropertyBool.create("extended");
    @Keep
    protected static final AxisAlignedBB PISTON_BASE_EAST_AABB  = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D);
    @Keep
    protected static final AxisAlignedBB PISTON_BASE_WEST_AABB  = new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    @Keep
    protected static final AxisAlignedBB PISTON_BASE_SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D);
    @Keep
    protected static final AxisAlignedBB PISTON_BASE_NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D);
    @Keep
    protected static final AxisAlignedBB PISTON_BASE_UP_AABB    = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);
    @Keep
    protected static final AxisAlignedBB PISTON_BASE_DOWN_AABB  = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 1.0D, 1.0D, 1.0D);
    private final boolean isSticky;

    public BlockPistonBase(boolean isSticky) {
        super(Material.PISTON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(EXTENDED, Boolean.FALSE));
        this.isSticky = isSticky;
        this.setSoundType(SoundType.STONE);
        this.setHardness(0.5F);
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(access, pos));
    }


    @Override
    public boolean causesSuffocation(IBlockState state) {
        return !state.getValue(EXTENDED);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(EXTENDED)) {
            switch (state.getValue(FACING)) {
                case DOWN:
                    return PISTON_BASE_DOWN_AABB;
                case UP:
                default:
                    return PISTON_BASE_UP_AABB;
                case NORTH:
                    return PISTON_BASE_NORTH_AABB;
                case SOUTH:
                    return PISTON_BASE_SOUTH_AABB;
                case WEST:
                    return PISTON_BASE_WEST_AABB;
                case EAST:
                    return PISTON_BASE_EAST_AABB;
            }
        } else {
            return FULL_BLOCK_AABB;
        }
    }


    @Override
    public boolean isTopSolid(IBlockState state) {
        return !state.getValue(EXTENDED) || state.getValue(FACING) == EnumFacing.DOWN;
    }


    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getBoundingBox(worldIn, pos));
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);

        if (!worldIn.isRemote)
            this.checkForMove(worldIn, pos, state);
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote)
            this.checkForMove(worldIn, pos, state);
    }


    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if ((!worldIn.isRemote) && worldIn.getTileEntity(pos) == null) {
            this.checkForMove(worldIn, pos, state);
        }
    }


    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)).withProperty(EXTENDED, Boolean.valueOf(false));
    }


    private void checkForMove(World worldIn, BlockPos pos, IBlockState state) {
        EnumFacing enumfacing = state.getValue(FACING);
        boolean flag = this.shouldBeExtended(worldIn, pos, enumfacing);

        if (flag && !state.getValue(EXTENDED)) {
            if ((new BlockPistonStructureHelper(worldIn, pos, enumfacing, true)).canMove()) {
                worldIn.addBlockEvent(pos, this, 0, enumfacing.getIndex());
            }
        } else if (!flag && state.getValue(EXTENDED)) {
            worldIn.addBlockEvent(pos, this, 1, enumfacing.getIndex());
        }
    }


    private boolean shouldBeExtended(World worldIn, BlockPos pos, EnumFacing facing) {
        BlockPos.MutableBlockPos temp = worldIn.localTempPos;
        for (EnumFacing cur : EnumFacing.VALUES) {
            temp.setPos(
                    pos.getX()+ cur.getXOffset(),
                    pos.getY()+ cur.getYOffset(),
                    pos.getZ()+ cur.getZOffset()
            );
            if (cur != facing && worldIn.isSidePowered(temp, cur)) return true;
        }
        if (worldIn.isSidePowered(pos, EnumFacing.DOWN))
            return true;
        else {
            for (EnumFacing cur : EnumFacing.VALUES) {
                temp.setPos(
                        pos.getX()+ cur.getXOffset(),
                        pos.getY()+ cur.getYOffset()+1,
                        pos.getZ()+ cur.getZOffset()
                );
                if (cur != EnumFacing.DOWN && worldIn.isSidePowered(temp, cur)) return true;
            }
            return false;
        }
    }


    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        EnumFacing enumfacing = state.getValue(FACING);
        IBlockEAccess access;
        if (!worldIn.isRemote) {
            boolean flag = this.shouldBeExtended(worldIn, pos, enumfacing);
            if (flag && id == 1) {
                worldIn.setBlockState(pos, state.withProperty(EXTENDED, Boolean.TRUE), 2);
                return false;
            }
            if (!flag && id == 0) {
                return false;
            }
            access = worldIn.access();
        } else {
            access = worldIn;
        }
        if (id == 0) {
            if (!this.doMove(worldIn, access, pos, enumfacing, true)) {
                return false;
            }
            access.setBlockState(pos, state.withProperty(EXTENDED, Boolean.TRUE), 3);
            worldIn.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, worldIn.rand.nextFloat() * 0.25F + 0.6F);
        }
        else if (id == 1) {
            TileEntity tileentity1 = access.getTileEntity(pos.offset(enumfacing));

            if (tileentity1 instanceof TileEntityPiston) {
                ((TileEntityPiston)tileentity1).clearPistonTileEntity();
            }

            access.setBlockState(pos, Blocks.PISTON_EXTENSION.getDefaultState().withProperty(BlockPistonMoving.FACING, enumfacing).withProperty(BlockPistonMoving.TYPE, this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT), 3);
            worldIn.setTileEntity(pos, BlockPistonMoving.createTilePiston(this.getStateFromMeta(param), enumfacing, false, true));

            if (this.isSticky) {
                BlockPos blockpos = pos.add(enumfacing.getXOffset() * 2, enumfacing.getYOffset() * 2, enumfacing.getZOffset() * 2);
                IBlockState iblockstate = access.getBlockState(blockpos);
                Block block = iblockstate.getBlock();
                boolean flag1 = false;

                if (block == Blocks.PISTON_EXTENSION) {
                    TileEntity tileentity = access.getTileEntity(blockpos);

                    if (tileentity instanceof TileEntityPiston) {
                        TileEntityPiston tileentitypiston = (TileEntityPiston)tileentity;

                        if (tileentitypiston.getFacing() == enumfacing && tileentitypiston.isExtending()) {
                            tileentitypiston.clearPistonTileEntity();
                            flag1 = true;
                        }
                    }
                }

                if (!flag1 && !iblockstate.getBlock().isAir(iblockstate, worldIn, blockpos)
                        && canPush(iblockstate, worldIn, blockpos, enumfacing.getOpposite(),
                        false, enumfacing) && (iblockstate.getPushReaction() == EnumPushReaction.NORMAL
                        || block == Blocks.PISTON || block == Blocks.STICKY_PISTON)) {
                    this.doMove(worldIn, access, pos, enumfacing, false);
                }
            } else {
                access.setBlockState(pos.offset(enumfacing), BlockIDIndexes.AIR_STATE, 3);
            }
            worldIn.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, worldIn.rand.nextFloat() * 0.15F + 0.6F);
        }
        return true;
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @Nullable
    public static EnumFacing getFacing(int meta) {
        int i = meta & 7;
        return i > 5 ? null : EnumFacing.byIndex(i);
    }


    public static boolean canPush(IBlockState blockStateIn, World worldIn, BlockPos pos, EnumFacing facing, boolean destroyBlocks, EnumFacing p_185646_5_) {
        Block block = blockStateIn.getBlock();

        if (block == Blocks.OBSIDIAN) {
            return false;
        } else if (!worldIn.getWorldBorder().contains(pos)) {
            return false;
        } else if (pos.getY() >= 0 && (facing != EnumFacing.DOWN || pos.getY() != 0)) {
            if (pos.getY() <= worldIn.getHeight() - 1 && (facing != EnumFacing.UP
                    || pos.getY() != worldIn.getHeight() - 1)) {
                if (block != Blocks.PISTON && block != Blocks.STICKY_PISTON) {
                    if (blockStateIn.getBlockHardness(worldIn, pos) == -1.0F) {
                        return false;
                    }
                    switch (blockStateIn.getPushReaction()) {
                        case BLOCK:
                            return false;
                        case DESTROY:
                            return destroyBlocks;
                        case PUSH_ONLY:
                            return facing == p_185646_5_;
                    }
                } else if (blockStateIn.getValue(EXTENDED)) {
                    return false;
                }
                return !block.hasTileEntity(blockStateIn);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    private boolean doMove(World worldIn, IBlockEAccess access, BlockPos pos, EnumFacing direction, boolean extending) {
        if (!extending) {
            access.setBlockState(pos.offset(direction), BlockIDIndexes.AIR_STATE, 3);
        }
        BlockPistonStructureHelper helper = new BlockPistonStructureHelper(worldIn, access, pos, direction, extending);

        if (!helper.canMove()) {
            return false;
        } else {
            List<BlockPos> list = helper.getBlocksToMove();
            List<IBlockState> list1 = Lists.newArrayList();

            for (int i = 0; i < list.size(); ++i) {
                BlockPos blockpos = list.get(i);
                list1.add(access.getBlockState(blockpos).getActualState(worldIn, blockpos));
            }

            List<BlockPos> list2 = helper.getBlocksToDestroy();
            int k = list.size() + list2.size();
            IBlockState[] aiblockstate = new IBlockState[k];
            EnumFacing enumfacing = extending ? direction : direction.getOpposite();

            for (int j = list2.size() - 1; j >= 0; --j) {
                BlockPos blockpos1 = list2.get(j);
                IBlockState iblockstate = access.getBlockState(blockpos1);
                // Forge: With our change to how snowballs are dropped this needs to disallow to mimic vanilla behavior.

                float chance = iblockstate.getBlock() instanceof BlockSnow ? -1.0f : 1.0f;
                iblockstate.getBlock().dropBlockAsItemWithChance(worldIn, blockpos1, iblockstate, chance, 0);
                access.setBlockState(blockpos1, BlockIDIndexes.AIR_STATE, 4);
                --k;
                aiblockstate[k] = iblockstate;
            }

            for (int l = list.size() - 1; l >= 0; --l) {
                BlockPos blockpos3 = list.get(l);
                IBlockState iblockstate2 = access.getBlockState(blockpos3);
                access.setBlockState(blockpos3, Blocks.AIR.getDefaultState(), 2);
                blockpos3 = blockpos3.offset(enumfacing);
                access.setBlockState(blockpos3, Blocks.PISTON_EXTENSION.getDefaultState().withProperty(FACING, direction), 4);
                worldIn.setTileEntity(blockpos3, BlockPistonMoving.createTilePiston(list1.get(l), direction, extending, false));
                --k;
                aiblockstate[k] = iblockstate2;
            }

            BlockPos blockpos2 = pos.offset(direction);

            if (extending) {
                BlockPistonExtension.EnumPistonType type = this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT;
                IBlockState iblockstate3 = Blocks.PISTON_HEAD.getDefaultState().withProperty(BlockPistonExtension.FACING, direction).withProperty(BlockPistonExtension.TYPE, type);
                IBlockState iblockstate1 = Blocks.PISTON_EXTENSION.getDefaultState().withProperty(BlockPistonMoving.FACING, direction).withProperty(BlockPistonMoving.TYPE, this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT);
                access.setBlockState(blockpos2, iblockstate1, 4);
                worldIn.setTileEntity(blockpos2, BlockPistonMoving.createTilePiston(iblockstate3, direction, true, true));
            }

            for (int i1 = list2.size() - 1; i1 >= 0; --i1) {
                worldIn.notifyNeighborsOfStateChange(list2.get(i1), aiblockstate[k++].getBlock(), false);
            }

            for (int j1 = list.size() - 1; j1 >= 0; --j1) {
                worldIn.notifyNeighborsOfStateChange(list.get(j1), aiblockstate[k++].getBlock(), false);
            }

            if (extending) {
                worldIn.notifyNeighborsOfStateChange(blockpos2, Blocks.PISTON_HEAD, false);
            }
            return true;
        }
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, getFacing(meta)).withProperty(EXTENDED, (meta & 8) > 0);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(FACING).getIndex();
        if (state.getValue(EXTENDED)) i |= 8;
        return i;
    }


    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }


    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn){
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, EXTENDED);
    }


    /* ======================================== FORGE START =====================================*/
    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        IBlockState state = world.getBlockState(pos);
        return !state.getValue(EXTENDED) && super.rotateBlock(world, pos, axis);
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        state = this.getActualState(state, worldIn, pos);
        return state.getValue(FACING) != face.getOpposite() && state.getValue(EXTENDED) ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
    }
}