package net.minecraft.block;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.utls.collections.ArrayListE6;
import static net.minecraft.block.BlockFs.addCollisionBoxToListFs;


public class BlockGlazedTerracotta extends BlockHorizontal {
    public BlockGlazedTerracotta(EnumDyeColor color) {
        super(Material.ROCK, MapColor.getBlockColor(color));
        this.setHardness(1.4F);
        this.setSoundType(SoundType.STONE);
        String s = color.getTranslationKey();

        if (s.length() > 1) {
            String s1 = s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
            this.setTranslationKey("glazedTerracotta" + s1);
        }

        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }


    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }


    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }


    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(FACING).getHorizontalIndex();
        return i;
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }


    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.PUSH_ONLY;
    }
}