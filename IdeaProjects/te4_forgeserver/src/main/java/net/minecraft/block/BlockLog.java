package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import plus.fserver.api.IBlockEAccess;


public abstract class BlockLog extends BlockRotatedPillarFs {
    public static final PropertyEnum<BlockLog.EnumAxis> LOG_AXIS = PropertyEnum.create("axis", BlockLog.EnumAxis.class);

    public BlockLog() {
        super(Material.WOOD);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setHardness(2.0F);
        this.setSoundType(SoundType.WOOD);
    }


    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos(pos.getX()-5, pos.getY()-5, pos.getZ()-5);
        BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos(pos.getX()+5, pos.getY()+5, pos.getZ()+5);

        if (world.isAreaLoaded(pos1, pos2)) {
            pos1.addMut(1,1,1);
            pos2.addMut(-1, -1, -1);

            IBlockEAccess access = world.access();
            for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos1, pos1, pos2)) {
                IBlockState iblockstate = access.getBlockState(blockpos);

                if (iblockstate.getBlock().isLeaves(iblockstate, world, blockpos)) {
                    iblockstate.getBlock().beginLeavesDecay(iblockstate, world, blockpos);
                }
            }
        }
    }


    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getStateFromMeta(meta).withProperty(LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(facing.getAxis()));
    }


    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        switch (rot) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch (state.getValue(LOG_AXIS)) {
                    case X:
                        return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
                    case Z:
                        return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
                    default:
                        return state;
                }
            default: return state;
        }
    }


    @Override public boolean canSustainLeaves(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos){
        return true;
    }


    @Override
    public boolean isWood(net.minecraft.world.IBlockAccess world, BlockPos pos){
        return true;
    }


    public enum EnumAxis implements IStringSerializable {
        X("x"),
        Y("y"),
        Z("z"),
        NONE("none");

        private final String name;

        EnumAxis(String name){
            this.name = name;
        }


        @Override
        public String toString() {
            return this.name;
        }


        public static BlockLog.EnumAxis fromFacingAxis(EnumFacing.Axis axis) {
            switch (axis) {
                case X:
                    return X;
                case Y:
                    return Y;
                case Z:
                    return Z;
                default:
                    return NONE;
            }
        }

        public String getName() {
            return this.name;
        }
    }
}
