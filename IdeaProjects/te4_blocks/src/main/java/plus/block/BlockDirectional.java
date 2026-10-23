package plus.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import plus.fserver.block.BlockFx;
import plus.fserver.utls.collections.ArrayListE6;
import plus.tson.TsonMap;
import plus.tson.TsonSerelizable;
import static plus.block.Block.*;


public class BlockDirectional extends net.minecraft.block.BlockDirectional implements TsonSerelizable {
    private final boolean isOpaqueCube;

    public BlockDirectional(String material, String id, String name, Boolean isOpaqueCube) {
        super(parseMaterial(material));
        setRegistryName(id);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setTranslationKey(name);
        this.isOpaqueCube = isOpaqueCube;
        this.setDefaultState(
                this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP)
        );
        setCreativeTab(TE4BLOCKS_TAB);
    }


    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list) {
        BlockFx.addCollisionBoxToListFx(state, access, pos, entityBox, list);
    }


    public BlockDirectional(TsonMap map){
        super(parseMaterial(getMaterialOfMap(map)));
        isOpaqueCube = Block.isOpaqueCube(map);
        onBlockInitFromMap(map, this);
        setCreativeTab(TE4BLOCKS_TAB);
    }


    @Override
    public TsonMap toTson() {
        return genDefaultMap(this);
    }


    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withProperty(FACING, mirror.mirror(state.getValue(FACING)));
    }
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing
            , float f1, float f2, float f3, int f4, EntityLivingBase entityLivingBase) {
        IBlockState state = world.getBlockState(pos.offset(facing.getOpposite()));
        if (state.getBlock() == Blocks.END_ROD) {
            EnumFacing value = state.getValue(FACING);
            if (value == facing) {
                return this.getDefaultState().withProperty(FACING, facing.getOpposite());
            }
        }
        return this.getDefaultState().withProperty(FACING, facing);
    }
    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState();
        state = state.withProperty(FACING, EnumFacing.byIndex(meta));
        return state;
    }
    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(FACING)).getIndex();
    }
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return isOpaqueCube;
    }
}