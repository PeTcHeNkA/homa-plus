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
import plus.fserver.api.IBlockEAccess;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.AABBList;
import plus.fserver.utls.collections.ArrayListE6;
import java.util.*;


public abstract class BlockRailBase extends Block {
    protected static final AxisAlignedBB FLAT_AABB;
    protected static final AxisAlignedBB ASCENDING_AABB;
    protected final boolean isPowered;

    public static boolean isRailBlock(final World worldIn, final BlockPos pos) {
        return isRailBlock(worldIn.getBlockState(pos));
    }

    public static boolean isRailBlock(final IBlockEAccess access, int x, int y, int z) {
        return isRailBlock(access.getBlockState(x, y, z));
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
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList collidingBoxes) {}


    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }


    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        final EnumRailDirection dir = (state.getBlock() == this) ? this.getRailDirection(source, pos, state, null) : null;
        return (dir != null && dir.isAscending()) ? BlockRailBase.ASCENDING_AABB : BlockRailBase.FLAT_AABB;
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
        BlockPos down = pos.down();
        IBlockEAccess access = worldIn.access();
        return access.getBlockState(down).isSideSolid(access, down, EnumFacing.UP);
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
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        if (!world.isRemote) {
            ChunkSelector selector = world.localSelector;
            final IBlockState cur = selector.getBlockState(pos);
            final EnumRailDirection dir = this.getRailDirection(world, pos, (cur.getBlock() == this) ? cur : state, null);
            boolean flag = false;
            if (!selector.getState(pos.getX(), pos.getY()-1, pos.getZ()).isSideSolid(world, pos.down(), EnumFacing.UP)) {
                flag = true;
            }
            else if (dir == EnumRailDirection.ASCENDING_EAST && !selector.getState(pos.getX()+1, pos.getY(), pos.getZ()).isSideSolid(world, pos.east(), EnumFacing.UP)) {
                flag = true;
            }
            else if (dir == EnumRailDirection.ASCENDING_WEST && !selector.getState(pos.getX()-1, pos.getY(), pos.getZ()).isSideSolid(world, pos.west(), EnumFacing.UP)) {
                flag = true;
            }
            else if (dir == EnumRailDirection.ASCENDING_NORTH && !selector.getState(pos.getX(), pos.getY(), pos.getZ()-1).isSideSolid(world, pos.north(), EnumFacing.UP)) {
                flag = true;
            }
            else if (dir == EnumRailDirection.ASCENDING_SOUTH && !selector.getState(pos.getX(), pos.getY(), pos.getZ()+1).isSideSolid(world, pos.south(), EnumFacing.UP)) {
                flag = true;
            }
            if (flag && !cur.getBlock().isAir(cur, world, pos)) {
                this.dropBlockAsItem(world, pos, state, 0);
                selector.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
            }
            else {
                this.updateState(state, world, pos, blockIn);
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


    private static boolean hasRailAt(IBlockEAccess access, int x, int y, int z) {
        return BlockRailBase.isRailBlock(access, x, y, z) || BlockRailBase.isRailBlock(access, x, y+1, z) || BlockRailBase.isRailBlock(access, x, y-1, z);
    }


    public class Rail {
        private final World world;
        private final BlockPos pos;
        private final BlockRailBase block;
        private IBlockState state;
        private final boolean isPowered;
        private final ArrayListE6<BlockPos> connectedRails;
        private final boolean canMakeSlopes;

        public Rail(final World worldIn, final BlockPos pos, final IBlockState state) {
            this.connectedRails = new ArrayListE6<>();
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
            this.connectedRails.fclear();
            switch (railDirection) {
                case NORTH_SOUTH: {
                    this.connectedRails.addF(this.pos.north());
                    this.connectedRails.addF(this.pos.south());
                    break;
                }
                case EAST_WEST: {
                    this.connectedRails.addF(this.pos.west());
                    this.connectedRails.addF(this.pos.east());
                    break;
                }
                case ASCENDING_EAST: {
                    this.connectedRails.addF(this.pos.west());
                    this.connectedRails.addF(this.pos.east().up());
                    break;
                }
                case ASCENDING_WEST: {
                    this.connectedRails.addF(this.pos.west().up());
                    this.connectedRails.addF(this.pos.east());
                    break;
                }
                case ASCENDING_NORTH: {
                    this.connectedRails.addF(this.pos.north().up());
                    this.connectedRails.addF(this.pos.south());
                    break;
                }
                case ASCENDING_SOUTH: {
                    this.connectedRails.addF(this.pos.north());
                    this.connectedRails.addF(this.pos.south().up());
                    break;
                }
                case SOUTH_EAST: {
                    this.connectedRails.addF(this.pos.east());
                    this.connectedRails.addF(this.pos.south());
                    break;
                }
                case SOUTH_WEST: {
                    this.connectedRails.addF(this.pos.west());
                    this.connectedRails.addF(this.pos.south());
                    break;
                }
                case NORTH_WEST: {
                    this.connectedRails.addF(this.pos.west());
                    this.connectedRails.addF(this.pos.north());
                    break;
                }
                case NORTH_EAST: {
                    this.connectedRails.addF(this.pos.east());
                    this.connectedRails.addF(this.pos.north());
                    break;
                }
            }
        }

        private void removeSoftConnections() {
            for (int i = 0; i < this.connectedRails.sizeC(); ++i) {
                final Rail rail = this.findRailAt(this.connectedRails.getC(i));
                if (rail != null && rail.isConnectedToRail(this)) {
                    this.connectedRails.set(i, rail.pos);
                } else {
                    this.connectedRails.remove(i--);
                }
            }
        }


        @Nullable
        private Rail findRailAt(final BlockPos pos) {
            IBlockEAccess access = world.access();
            IBlockState state = access.getBlockState(pos);
            if (BlockRailBase.isRailBlock(state)) {
                return new Rail(this.world, pos, state);
            }
            BlockPos temp = pos.up();
            state = access.getBlockState(temp);

            if (BlockRailBase.isRailBlock(state)) {
                return new Rail(this.world, temp, state);
            }
            BlockPos.unsafeEdit(temp, pos.getX(), pos.getY()-1, pos.getZ());

            state = access.getBlockState(temp);
            return BlockRailBase.isRailBlock(state) ? new Rail(this.world, temp, state) : null;
        }


        private boolean isConnectedToRail(final Rail rail) {
            final BlockPos pos = rail.pos;
            return this.isConnectedTo(pos.getX(), pos.getZ());
        }


        private boolean isConnectedTo(final int x, final int z) {
            for (int i = 0, s = this.connectedRails.sizeC(); i < s; ++i) {
                final BlockPos pos = this.connectedRails.getC(i);
                if (pos.getX() == x && pos.getZ() == z) return true;
            }
            return false;
        }


        protected int countAdjacentRails() {
            IBlockEAccess access = world.access();
            int i = 0;
            int px = pos.getX(), py = pos.getY(), pz = pos.getZ();
            for (final EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                if (hasRailAt(access,
                        px+facing.getXOffset(),
                        py+facing.getYOffset(),
                        pz+facing.getZOffset()
                )) {
                    ++i;
                }
            }
            return i;
        }


        private boolean canConnectTo(final Rail rail) {
            return this.isConnectedToRail(rail) || this.connectedRails.sizeC() != 2;
        }


        private void connectTo(final Rail rail) {
            BlockPos pos = this.pos;
            this.connectedRails.addF(rail.pos);
            final boolean flag  = this.isConnectedTo(pos.getX(), pos.getZ()-1);
            final boolean flag2 = this.isConnectedTo(pos.getX(), pos.getZ()+1);
            final boolean flag3 = this.isConnectedTo(pos.getX()-1, pos.getZ());
            final boolean flag4 = this.isConnectedTo(pos.getX()+1, pos.getZ());
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
            IBlockEAccess access = world.access();
            if (dir == EnumRailDirection.NORTH_SOUTH && this.canMakeSlopes) {
                if (BlockRailBase.isRailBlock(access, pos.getX(), pos.getY()+1, pos.getZ()-1)) {
                    dir = EnumRailDirection.ASCENDING_NORTH;
                }
                if (BlockRailBase.isRailBlock(access, pos.getX(), pos.getY()+1, pos.getZ()+1)) {
                    dir = EnumRailDirection.ASCENDING_SOUTH;
                }
            }
            if (dir == EnumRailDirection.EAST_WEST && this.canMakeSlopes) {
                if (BlockRailBase.isRailBlock(access, pos.getX()+1, pos.getY()+1, pos.getZ())) {
                    dir = EnumRailDirection.ASCENDING_EAST;
                }
                if (BlockRailBase.isRailBlock(access, pos.getX()-1,pos.getY()+1, pos.getZ())) {
                    dir = EnumRailDirection.ASCENDING_WEST;
                }
            }
            if (dir == null) {
                dir = EnumRailDirection.NORTH_SOUTH;
            }
            this.state = this.state.withProperty(this.block.getShapeProperty(), dir);
            access.setBlockState(this.pos, this.state, 3);
        }


        private boolean hasNeighborRail(final BlockPos posIn) {
            final Rail rail = this.findRailAt(posIn);
            if (rail == null) return false;
            rail.removeSoftConnections();
            return rail.canConnectTo(this);
        }


        public Rail place(final boolean powered, final boolean initialPlacement) {
            BlockPos pos = this.pos;

            final BlockPos blockpos  = pos.north();
            final BlockPos blockpos2 = pos.south();
            final BlockPos blockpos3 = pos.west();
            final BlockPos blockpos4 = pos.east();
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
            IBlockEAccess access = world.access();
            if (dir == EnumRailDirection.NORTH_SOUTH && this.canMakeSlopes) {
                if (BlockRailBase.isRailBlock(access, pos.getX(), pos.getY()+1, pos.getZ()-1)) {
                    dir = EnumRailDirection.ASCENDING_NORTH;
                }
                if (BlockRailBase.isRailBlock(access, pos.getX(), pos.getY()+1, pos.getZ()+1)) {
                    dir = EnumRailDirection.ASCENDING_SOUTH;
                }
            }
            if (dir == EnumRailDirection.EAST_WEST && this.canMakeSlopes) {
                if (BlockRailBase.isRailBlock(access, pos.getX()+1, pos.getY()+1, pos.getZ())) {
                    dir = EnumRailDirection.ASCENDING_EAST;
                }
                if (BlockRailBase.isRailBlock(access, pos.getX()-1,pos.getY()+1, pos.getZ())) {
                    dir = EnumRailDirection.ASCENDING_WEST;
                }
            }
            if (dir == null) {
                dir = EnumRailDirection.NORTH_SOUTH;
            }
            this.updateConnectedRails(dir);
            this.state = this.state.withProperty(this.block.getShapeProperty(), dir);
            if (initialPlacement || access.getBlockState(this.pos) != this.state) {
                access.setBlockState(this.pos, this.state, 3);
                for (int i = 0; i < this.connectedRails.sizeC(); ++i) {
                    final Rail rail = this.findRailAt(this.connectedRails.getC(i));
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