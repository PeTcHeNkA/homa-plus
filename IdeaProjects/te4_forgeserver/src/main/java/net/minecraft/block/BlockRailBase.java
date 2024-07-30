package net.minecraft.block;

import net.minecraft.util.math.*;
import net.minecraft.creativetab.*;
import net.minecraft.world.*;
import javax.annotation.*;
import net.minecraft.entity.item.*;
import net.minecraft.block.state.*;
import net.minecraft.block.material.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.block.properties.*;
import net.minecraft.util.*;
import com.google.common.collect.*;
import java.util.*;


public abstract class BlockRailBase extends Block {
    protected static final AxisAlignedBB FLAT_AABB;
    protected static final AxisAlignedBB ASCENDING_AABB;
    protected final boolean isPowered;

    public static boolean isRailBlock(final World worldIn, final BlockPos pos) {
        return isRailBlock(worldIn.getBlockState(pos));
    }


    public static boolean isRailBlock(final IBlockState state) {
        final Block block = state.getBlock();
        return block instanceof BlockRailBase;
    }


    protected BlockRailBase(final boolean isPowered) {
        super(Material.CIRCUITS);
        this.isPowered = isPowered;
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos) {
        return BlockRailBase.NULL_AABB;
    }


    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }


    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        final EnumRailDirection blockrailbase$enumraildirection = (state.getBlock() == this) ? this.getRailDirection(source, pos, state, null) : null;
        return (blockrailbase$enumraildirection != null && blockrailbase$enumraildirection.isAscending()) ? BlockRailBase.ASCENDING_AABB : BlockRailBase.FLAT_AABB;
    }


    @Override
    public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final IBlockState state, final BlockPos pos, final EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }


    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }


    @Override
    public boolean canPlaceBlockAt(final World worldIn, final BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP);
    }


    @Override
    public void onBlockAdded(final World worldIn, final BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            state = this.updateDir(worldIn, pos, state, true);
            if (this.isPowered) {
                state.neighborChanged(worldIn, pos, this, pos);
            }
        }
    }


    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        if (!worldIn.isRemote) {
            final IBlockState currentState = worldIn.getBlockState(pos);
            final EnumRailDirection blockrailbase$enumraildirection = this.getRailDirection(worldIn, pos, (currentState.getBlock() == this) ? currentState : state, null);
            boolean flag = false;
            if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP)) {
                flag = true;
            }
            if (blockrailbase$enumraildirection == EnumRailDirection.ASCENDING_EAST && !worldIn.getBlockState(pos.east()).isSideSolid(worldIn, pos.east(), EnumFacing.UP)) {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == EnumRailDirection.ASCENDING_WEST && !worldIn.getBlockState(pos.west()).isSideSolid(worldIn, pos.west(), EnumFacing.UP)) {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == EnumRailDirection.ASCENDING_NORTH && !worldIn.getBlockState(pos.north()).isSideSolid(worldIn, pos.north(), EnumFacing.UP)) {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == EnumRailDirection.ASCENDING_SOUTH && !worldIn.getBlockState(pos.south()).isSideSolid(worldIn, pos.south(), EnumFacing.UP)) {
                flag = true;
            }
            if (flag && !currentState.getBlock().isAir(currentState, (IBlockAccess)worldIn, pos)) {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }
            else {
                this.updateState(state, worldIn, pos, blockIn);
            }
        }
    }


    protected void updateState(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn) {}


    protected IBlockState updateDir(final World worldIn, final BlockPos pos, final IBlockState state, final boolean initialPlacement) {
        return worldIn.isRemote ? state : new Rail(worldIn, pos, state).place(worldIn.isBlockPowered(pos), initialPlacement).getBlockState();
    }


    @Override
    public EnumPushReaction getPushReaction(final IBlockState state) {
        return EnumPushReaction.NORMAL;
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (this.getRailDirection(worldIn, pos, state, null).isAscending()) {
            worldIn.notifyNeighborsOfStateChange(pos.up(), this, false);
        }
        if (this.isPowered) {
            worldIn.notifyNeighborsOfStateChange(pos, this, false);
            worldIn.notifyNeighborsOfStateChange(pos.down(), this, false);
        }
    }


    public abstract IProperty<EnumRailDirection> getShapeProperty();


    public boolean isFlexibleRail(final IBlockAccess access, final BlockPos pos) {
        return !this.isPowered;
    }


    public boolean canMakeSlopes(final IBlockAccess access, final BlockPos pos) {
        return true;
    }


    public EnumRailDirection getRailDirection(final IBlockAccess access, final BlockPos pos, final IBlockState state, @Nullable final EntityMinecart cart) {
        return state.getValue(this.getShapeProperty());
    }


    public float getRailMaxSpeed(final World world, final EntityMinecart cart, final BlockPos pos) {
        return 0.4f;
    }


    public void onMinecartPass(final World world, final EntityMinecart cart, final BlockPos pos) {}


    @Override
    public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing facing) {
        final IBlockState state = world.getBlockState(pos);
        for (final IProperty<?> prop : state.getProperties().keySet()) {
            if (prop.getName().equals("shape")) {
                world.setBlockState(pos, state.cycleProperty(prop));
                return true;
            }
        }
        return false;
    }


    static {
        FLAT_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
        ASCENDING_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);
    }


    public enum EnumRailDirection implements IStringSerializable {
        NORTH_SOUTH(0, "north_south"),
        EAST_WEST(1, "east_west"),
        ASCENDING_EAST(2, "ascending_east"),
        ASCENDING_WEST(3, "ascending_west"),
        ASCENDING_NORTH(4, "ascending_north"),
        ASCENDING_SOUTH(5, "ascending_south"),
        SOUTH_EAST(6, "south_east"),
        SOUTH_WEST(7, "south_west"),
        NORTH_WEST(8, "north_west"),
        NORTH_EAST(9, "north_east");

        private static final EnumRailDirection[] META_LOOKUP;
        private final int meta;
        private final String name;

        private EnumRailDirection(final int meta, final String name) {
            this.meta = meta;
            this.name = name;
        }


        public int getMetadata() {
            return this.meta;
        }


        @Override
        public String toString() {
            return this.name;
        }


        public boolean isAscending() {
            return this == EnumRailDirection.ASCENDING_NORTH || this == EnumRailDirection.ASCENDING_EAST || this == EnumRailDirection.ASCENDING_SOUTH || this == EnumRailDirection.ASCENDING_WEST;
        }


        public static EnumRailDirection byMetadata(int meta) {
            if (meta < 0 || meta >= EnumRailDirection.META_LOOKUP.length) {
                meta = 0;
            }
            return EnumRailDirection.META_LOOKUP[meta];
        }


        @Override
        public String getName() {
            return this.name;
        }


        static {
            META_LOOKUP = new EnumRailDirection[values().length];
            for (final EnumRailDirection blockrailbase$enumraildirection : values()) {
                EnumRailDirection.META_LOOKUP[blockrailbase$enumraildirection.getMetadata()] = blockrailbase$enumraildirection;
            }
        }
    }


    public class Rail {
        private final World world;
        private final BlockPos pos;
        private final BlockRailBase block;
        private IBlockState state;
        private final boolean isPowered;
        private final List<BlockPos> connectedRails;
        private final boolean canMakeSlopes;

        public Rail(final World worldIn, final BlockPos pos, final IBlockState state) {
            this.connectedRails = Lists.newArrayList();
            this.world = worldIn;
            this.pos = pos;
            this.state = state;
            this.block = (BlockRailBase)state.getBlock();
            final EnumRailDirection dir = this.block.getRailDirection(worldIn, pos, state, null);
            this.isPowered = !this.block.isFlexibleRail(worldIn, pos);
            this.canMakeSlopes = this.block.canMakeSlopes(worldIn, pos);
            this.updateConnectedRails(dir);
        }


        public List<BlockPos> getConnectedRails() {
            return this.connectedRails;
        }


        private void updateConnectedRails(final EnumRailDirection railDirection) {
            this.connectedRails.clear();
            switch (railDirection) {
                case NORTH_SOUTH: {
                    this.connectedRails.add(this.pos.north());
                    this.connectedRails.add(this.pos.south());
                    break;
                }
                case EAST_WEST: {
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.east());
                    break;
                }
                case ASCENDING_EAST: {
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.east().up());
                    break;
                }
                case ASCENDING_WEST: {
                    this.connectedRails.add(this.pos.west().up());
                    this.connectedRails.add(this.pos.east());
                    break;
                }
                case ASCENDING_NORTH: {
                    this.connectedRails.add(this.pos.north().up());
                    this.connectedRails.add(this.pos.south());
                    break;
                }
                case ASCENDING_SOUTH: {
                    this.connectedRails.add(this.pos.north());
                    this.connectedRails.add(this.pos.south().up());
                    break;
                }
                case SOUTH_EAST: {
                    this.connectedRails.add(this.pos.east());
                    this.connectedRails.add(this.pos.south());
                    break;
                }
                case SOUTH_WEST: {
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.south());
                    break;
                }
                case NORTH_WEST: {
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.north());
                    break;
                }
                case NORTH_EAST: {
                    this.connectedRails.add(this.pos.east());
                    this.connectedRails.add(this.pos.north());
                    break;
                }
            }
        }

        private void removeSoftConnections() {
            for (int i = 0; i < this.connectedRails.size(); ++i) {
                final Rail blockrailbase$rail = this.findRailAt(this.connectedRails.get(i));
                if (blockrailbase$rail != null && blockrailbase$rail.isConnectedToRail(this)) {
                    this.connectedRails.set(i, blockrailbase$rail.pos);
                }
                else {
                    this.connectedRails.remove(i--);
                }
            }
        }

        private boolean hasRailAt(final BlockPos pos) {
            return BlockRailBase.isRailBlock(this.world, pos) || BlockRailBase.isRailBlock(this.world, pos.up()) || BlockRailBase.isRailBlock(this.world, pos.down());
        }

        @Nullable
        private Rail findRailAt(final BlockPos pos) {
            IBlockState iblockstate = this.world.getBlockState(pos);
            if (BlockRailBase.isRailBlock(iblockstate)) {
                return new Rail(this.world, pos, iblockstate);
            }
            BlockPos lvt_2_1_ = pos.up();
            iblockstate = this.world.getBlockState(lvt_2_1_);
            if (BlockRailBase.isRailBlock(iblockstate)) {
                return new Rail(this.world, lvt_2_1_, iblockstate);
            }
            lvt_2_1_ = pos.down();
            iblockstate = this.world.getBlockState(lvt_2_1_);
            return BlockRailBase.isRailBlock(iblockstate) ? new Rail(this.world, lvt_2_1_, iblockstate) : null;
        }

        private boolean isConnectedToRail(final Rail rail) {
            return this.isConnectedTo(rail.pos);
        }

        private boolean isConnectedTo(final BlockPos posIn) {
            for (int i = 0; i < this.connectedRails.size(); ++i) {
                final BlockPos blockpos = this.connectedRails.get(i);
                if (blockpos.getX() == posIn.getX() && blockpos.getZ() == posIn.getZ()) {
                    return true;
                }
            }
            return false;
        }

        protected int countAdjacentRails() {
            int i = 0;
            for (final EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                if (this.hasRailAt(this.pos.offset(enumfacing))) {
                    ++i;
                }
            }
            return i;
        }

        private boolean canConnectTo(final Rail rail) {
            return this.isConnectedToRail(rail) || this.connectedRails.size() != 2;
        }


        private void connectTo(final Rail rail) {
            this.connectedRails.add(rail.pos);
            final BlockPos blockpos = this.pos.north();
            final BlockPos blockpos2 = this.pos.south();
            final BlockPos blockpos3 = this.pos.west();
            final BlockPos blockpos4 = this.pos.east();
            final boolean flag = this.isConnectedTo(blockpos);
            final boolean flag2 = this.isConnectedTo(blockpos2);
            final boolean flag3 = this.isConnectedTo(blockpos3);
            final boolean flag4 = this.isConnectedTo(blockpos4);
            EnumRailDirection dir = null;
            if (flag || flag2) {
                dir = EnumRailDirection.NORTH_SOUTH;
            }
            if (flag3 || flag4) {
                dir = EnumRailDirection.EAST_WEST;
            }
            if (!this.isPowered) {
                if (flag2 && flag4 && !flag && !flag3) {
                    dir = EnumRailDirection.SOUTH_EAST;
                }
                if (flag2 && flag3 && !flag && !flag4) {
                    dir = EnumRailDirection.SOUTH_WEST;
                }
                if (flag && flag3 && !flag2 && !flag4) {
                    dir = EnumRailDirection.NORTH_WEST;
                }
                if (flag && flag4 && !flag2 && !flag3) {
                    dir = EnumRailDirection.NORTH_EAST;
                }
            }
            if (dir == EnumRailDirection.NORTH_SOUTH && this.canMakeSlopes) {
                if (BlockRailBase.isRailBlock(this.world, blockpos.up())) {
                    dir = EnumRailDirection.ASCENDING_NORTH;
                }
                if (BlockRailBase.isRailBlock(this.world, blockpos2.up())) {
                    dir = EnumRailDirection.ASCENDING_SOUTH;
                }
            }
            if (dir == EnumRailDirection.EAST_WEST && this.canMakeSlopes) {
                if (BlockRailBase.isRailBlock(this.world, blockpos4.up())) {
                    dir = EnumRailDirection.ASCENDING_EAST;
                }
                if (BlockRailBase.isRailBlock(this.world, blockpos3.up())) {
                    dir = EnumRailDirection.ASCENDING_WEST;
                }
            }
            if (dir == null) {
                dir = EnumRailDirection.NORTH_SOUTH;
            }
            this.state = this.state.withProperty(this.block.getShapeProperty(), dir);
            this.world.setBlockState(this.pos, this.state, 3);
        }


        private boolean hasNeighborRail(final BlockPos posIn) {
            final Rail blockrailbase$rail = this.findRailAt(posIn);
            if (blockrailbase$rail == null) {
                return false;
            }
            blockrailbase$rail.removeSoftConnections();
            return blockrailbase$rail.canConnectTo(this);
        }


        public Rail place(final boolean powered, final boolean initialPlacement) {
            final BlockPos blockpos = this.pos.north();
            final BlockPos blockpos2 = this.pos.south();
            final BlockPos blockpos3 = this.pos.west();
            final BlockPos blockpos4 = this.pos.east();
            final boolean flag = this.hasNeighborRail(blockpos);
            final boolean flag2 = this.hasNeighborRail(blockpos2);
            final boolean flag3 = this.hasNeighborRail(blockpos3);
            final boolean flag4 = this.hasNeighborRail(blockpos4);
            EnumRailDirection dir = null;
            if ((flag || flag2) && !flag3 && !flag4) {
                dir = EnumRailDirection.NORTH_SOUTH;
            }
            if ((flag3 || flag4) && !flag && !flag2) {
                dir = EnumRailDirection.EAST_WEST;
            }
            if (!this.isPowered) {
                if (flag2 && flag4 && !flag && !flag3) {
                    dir = EnumRailDirection.SOUTH_EAST;
                }
                if (flag2 && flag3 && !flag && !flag4) {
                    dir = EnumRailDirection.SOUTH_WEST;
                }
                if (flag && flag3 && !flag2 && !flag4) {
                    dir = EnumRailDirection.NORTH_WEST;
                }
                if (flag && flag4 && !flag2 && !flag3) {
                    dir = EnumRailDirection.NORTH_EAST;
                }
            }
            if (dir == null) {
                if (flag || flag2) {
                    dir = EnumRailDirection.NORTH_SOUTH;
                }
                if (flag3 || flag4) {
                    dir = EnumRailDirection.EAST_WEST;
                }
                if (!this.isPowered) {
                    if (powered) {
                        if (flag2 && flag4) {
                            dir = EnumRailDirection.SOUTH_EAST;
                        }
                        if (flag3 && flag2) {
                            dir = EnumRailDirection.SOUTH_WEST;
                        }
                        if (flag4 && flag) {
                            dir = EnumRailDirection.NORTH_EAST;
                        }
                        if (flag && flag3) {
                            dir = EnumRailDirection.NORTH_WEST;
                        }
                    }
                    else {
                        if (flag && flag3) {
                            dir = EnumRailDirection.NORTH_WEST;
                        }
                        if (flag4 && flag) {
                            dir = EnumRailDirection.NORTH_EAST;
                        }
                        if (flag3 && flag2) {
                            dir = EnumRailDirection.SOUTH_WEST;
                        }
                        if (flag2 && flag4) {
                            dir = EnumRailDirection.SOUTH_EAST;
                        }
                    }
                }
            }
            if (dir == EnumRailDirection.NORTH_SOUTH && this.canMakeSlopes) {
                if (BlockRailBase.isRailBlock(this.world, blockpos.up())) {
                    dir = EnumRailDirection.ASCENDING_NORTH;
                }
                if (BlockRailBase.isRailBlock(this.world, blockpos2.up())) {
                    dir = EnumRailDirection.ASCENDING_SOUTH;
                }
            }
            if (dir == EnumRailDirection.EAST_WEST && this.canMakeSlopes) {
                if (BlockRailBase.isRailBlock(this.world, blockpos4.up())) {
                    dir = EnumRailDirection.ASCENDING_EAST;
                }
                if (BlockRailBase.isRailBlock(this.world, blockpos3.up())) {
                    dir = EnumRailDirection.ASCENDING_WEST;
                }
            }
            if (dir == null) {
                dir = EnumRailDirection.NORTH_SOUTH;
            }
            this.updateConnectedRails(dir);
            this.state = this.state.withProperty(this.block.getShapeProperty(), dir);
            if (initialPlacement || this.world.getBlockState(this.pos) != this.state) {
                this.world.setBlockState(this.pos, this.state, 3);
                for (int i = 0; i < this.connectedRails.size(); ++i) {
                    final Rail rail = this.findRailAt(this.connectedRails.get(i));
                    if (rail != null) {
                        rail.removeSoftConnections();
                        if (rail.canConnectTo(this)) {
                            rail.connectTo(this);
                        }
                    }
                }
            }
            return this;
        }


        public IBlockState getBlockState() {
            return this.state;
        }
    }
}