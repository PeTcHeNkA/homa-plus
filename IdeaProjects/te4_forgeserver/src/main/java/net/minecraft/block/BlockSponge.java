package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.Queue;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaConfig;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.utls.collections.ArrayListE10;


public class BlockSponge extends BlockFx {
    public static final PropertyBool WET = PropertyBool.create("wet");

    protected BlockSponge() {
        super(Material.SPONGE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(WET, Boolean.valueOf(false)));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal(this.getTranslationKey() + ".dry.name");
    }


    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(WET) ? 1 : 0;
    }


    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.tryAbsorb(worldIn, pos, state);
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        this.tryAbsorb(worldIn, pos, state);
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }


    protected void tryAbsorb(World worldIn, BlockPos pos, IBlockState state) {
        if (!state.getValue(WET).booleanValue() && this.absorb(worldIn, pos)) {
            worldIn.setBlockState(pos, state.withProperty(WET, Boolean.valueOf(true)), 2);
            worldIn.playEvent(2001, pos, Block.getIdFromBlock(Blocks.WATER));
        }
    }

    private static class BlockPosIndex extends BlockPos.MutableBlockPos{
        private int index;
        public BlockPosIndex(int x, int y, int z, int index) {
            super(x, y, z);
        }
    }


    private boolean absorb(World world, BlockPos pos) {
        Queue<BlockPosIndex> queue = Lists.newLinkedList();

        ArrayListE10<BlockPos> list = new ArrayListE10<>();
        queue.add(new BlockPosIndex(pos.getX(), pos.getY(), pos.getZ(), 0));

        int i = ElcaConfig.MAX_SPONGE_POWER;

        IBlockEAccess access = world.access();

        while (!queue.isEmpty()) {
            BlockPosIndex blockpos = queue.poll();
            BlockPosIndex temp = new BlockPosIndex(0,0,0, blockpos.index);

            for (EnumFacing facing : EnumFacing.VALUES) {
                temp.setPos(
                        blockpos.getX()+facing.getXOffset(),
                        blockpos.getY()+facing.getYOffset(),
                        blockpos.getZ()+facing.getZOffset()
                );

                if (access.getBlockState(temp).getMaterial() == Material.WATER) {
                    access.setBlockState(temp, Blocks.AIR.getDefaultState(), 2);
                    list.addF(temp);
                    --i;

                    if (blockpos.index < 6) {
                        blockpos.index += 1;
                        queue.add(temp);
                    }
                    temp = new BlockPosIndex(temp.getX(), temp.getY(), temp.getZ(), temp.index);
                }
            }
            if (i < 0) {
                break;
            }
        }
        for (BlockPos pos1 : list) {
            world.notifyNeighborsOfStateChange(pos1, Blocks.AIR, false);
        }
        return i > 0;
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(WET, (meta & 1) == 1);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(WET) ? 1 : 0;
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, WET);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.getValue(WET)) {
            EnumFacing enumfacing = EnumFacing.random(rand);

            if (enumfacing != EnumFacing.UP && !worldIn.getBlockState(pos.offset(enumfacing)).isTopSolid()) {
                double d0 = pos.getX();
                double d1 = pos.getY();
                double d2 = pos.getZ();

                if (enumfacing == EnumFacing.DOWN) {
                    d1 = d1 - 0.05D;
                    d0 += rand.nextDouble();
                    d2 += rand.nextDouble();
                } else {
                    d1 = d1 + rand.nextDouble() * 0.8D;

                    if (enumfacing.getAxis() == EnumFacing.Axis.X) {
                        d2 += rand.nextDouble();

                        if (enumfacing == EnumFacing.EAST) {
                            ++d0;
                        } else {
                            d0 += 0.05D;
                        }
                    } else {
                        d0 += rand.nextDouble();

                        if (enumfacing == EnumFacing.SOUTH) {
                            ++d2;
                        } else {
                            d2 += 0.05D;
                        }
                    }
                }
                worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}