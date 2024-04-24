package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.IBlockEAccess;


public class BlockMycelium extends BlockFs {
    public static final PropertyBool SNOWY = PropertyBool.create("snowy");

    protected BlockMycelium() {
        super(Material.GRASS, MapColor.PURPLE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(SNOWY, Boolean.FALSE));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        Block block = worldIn.getBlockState(pos.up()).getBlock();
        return state.withProperty(SNOWY, block == Blocks.SNOW || block == Blocks.SNOW_LAYER);
    }


    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            if (!worldIn.isAreaLoaded(pos, 2)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
            BlockPos.MutableBlockPos temp = new BlockPos.MutableBlockPos(pos.getX(), pos.getY()+1, pos.getZ());
            IBlockEAccess access = ((WorldServer)worldIn).localSelector;

            if (worldIn.getLightFromNeighbors(temp) < 4 && access.getBlockState(temp).getLightOpacity(worldIn, temp) > 2) {
                access.setBlockState(pos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT));
            } else {
                if (worldIn.getLightFromNeighbors(temp) >= 9) {
                    for (int i = 0; i < 4; ++i) {
                        temp.setPos(pos.getX()+rand.nextInt(3) - 1, pos.getY()+rand.nextInt(5) - 3,pos.getZ()+rand.nextInt(3) - 1);

                        //BlockPos blockpos = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
                        IBlockState iblockstate = access.getBlockState(temp);
                        temp.setY(pos.getY()+1);
                        IBlockState iblockstate1 = access.getBlockState(temp);

                        if (iblockstate.getBlock() == Blocks.DIRT && iblockstate.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT &&
                                worldIn.getLightFromNeighbors(temp) >= 4 && iblockstate1.getLightOpacity(worldIn, temp) <= 2) {

                            temp.setY(pos.getY()-1);
                            worldIn.setBlockState(temp, this.getDefaultState());
                        }
                    }
                }
            }
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);

        if (rand.nextInt(10) == 0) {
            worldIn.spawnParticle(EnumParticleTypes.TOWN_AURA, (float)pos.getX() + rand.nextFloat(), (float)pos.getY() + 1.1F, (float)pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
        }
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), rand, fortune);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SNOWY);
    }
}
