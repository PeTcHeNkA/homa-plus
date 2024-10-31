package net.minecraft.block;

import java.util.LinkedList;
import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.asyutl.buf.FastBuffer;
import plus.fserver.ElcaConfig;
import plus.fserver.api.Keep;
import plus.fserver.block.BlockFx;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.ArrayListE6;


public class BlockSponge extends BlockFx {
    @Keep
    public static final PropertyBool WET = PropertyBool.create("wet");

    protected BlockSponge() {
        super(Material.SPONGE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(WET, Boolean.FALSE));
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
            worldIn.setBlockState(pos, state.withProperty(WET, Boolean.TRUE), 2);
            worldIn.playEvent(2001, pos, Block.getIdFromBlock(Blocks.WATER));
        }
    }

    private static class BlockPosIndex extends BlockPos.MutableBlockPos{
        int index;
        EnumFacing excluding;
        EnumFacing excluding2;
        BlockPosIndex next;

        public BlockPosIndex(int x, int y, int z, int index, EnumFacing excluding, EnumFacing excluding2) {
            super(x, y, z);
            this.index = index;
            this.excluding = excluding;
            this.excluding2 = excluding2;
        }

        void add(BlockPosIndex next){
            next.next = this.next;
            this.next = next;
        }
        void forEach(Consumer<BlockPosIndex> func){
            BlockPosIndex next = this;
            while (next != null){
                func.accept(next);
                next = next.next;
            }
        }
    }


    private boolean absorb(World world, BlockPos pos) {
        if(world.isRemote)return false;
        LinkedList<BlockPosIndex> queue = new LinkedList<>();
        queue.add(new BlockPosIndex(pos.getX(), pos.getY(), pos.getZ(), 0, null,null));
        int max = ElcaConfig.MAX_SPONGE_POWER, maxRad = ElcaConfig.MAX_SPONGE_RADIUS;
        ChunkSelector access = world.localSelector;

        BlockPosIndex updates = null;
        //Updates updates = null;

        while (!queue.isEmpty()) {
            BlockPosIndex blockpos = queue.poll();
            //if(updates != null)updates.remove(blockpos);
            int index = blockpos.index;
            EnumFacing excluding = blockpos.excluding;
            EnumFacing excluding2 = blockpos.excluding2;
            int px = blockpos.getX(), py = blockpos.getY(), pz = blockpos.getZ();
            int count = 0;

            for (EnumFacing facing : EnumFacing.VALUES) {
                if(excluding == facing || excluding2 == facing)continue;
                int x, y, z;

                IBlockState state = access.getState(
                        x = px + facing.getXOffset(),
                        y = py + facing.getYOffset(),
                        z = pz + facing.getZOffset()
                );
                if(state == BlockIDIndexes.AIR_STATE){
                    ++count;
                } else if (state.getMaterial() == Material.WATER) {
                    ++count;
                    BlockPosIndex blockpos1 = new BlockPosIndex(x, y, z, index, facing.getOpposite(), excluding);

                    access.setBlockState(blockpos1, BlockIDIndexes.AIR_STATE, 2);
                    if(updates == null) updates = blockpos1;
                    else updates.add(blockpos1);
                    --max;

                    if (index < maxRad) {
                        index += 1;
                        blockpos1.index = index;
                        queue.add(blockpos1);
                    }
                }
            }
            if(count == 0 || count == 6)blockpos.index = -1;
            if (max < 0) break;
        }
        if(updates != null) {
            updates.forEach(pos1 -> {
                if(pos1.index != -1)world.notifyNeighborsOfStateChange(pos1, Blocks.AIR, false);
            });
            return true;
        }
        return false;
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