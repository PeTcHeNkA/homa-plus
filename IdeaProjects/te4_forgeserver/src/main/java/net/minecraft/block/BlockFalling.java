package net.minecraft.block;

import net.minecraft.block.material.*;
import net.minecraft.creativetab.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;
import net.minecraft.block.state.*;
import java.util.*;
import net.minecraft.entity.item.*;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.*;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.server.ElcaThread;


public class BlockFalling extends Block {
    @Deprecated
    public static boolean fallInstantly;

    public BlockFalling() {
        super(Material.SAND);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    public BlockFalling(final Material materialIn) {
        super(materialIn);
    }


    @Override
    public void onBlockAdded(final World worldIn, final BlockPos pos, final IBlockState state) {
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }


    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }


    @Override
    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand) {
        if (!worldIn.isRemote) {
            this.checkFallable(worldIn, pos);
        }
    }


    private void checkFallable(final World world, final BlockPos pos) {
        ElcaThread.Cache access = ElcaThread.cache(world);
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        if ((access.isAirBlock(x, y-1, z) || canFallThrough(access.getBlockState(x, y-1, z))) && y >= 0) {
            if (world.isAreaLoaded(x-32,y-32,z-32, x+32,y+32,z+32,true)) {
                if (!world.isRemote) {
                    final EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, x + 0.5, y, z + 0.5, access.getBlockState(x,y,z));
                    this.onStartFalling(entityfallingblock);
                    world.spawnEntity(entityfallingblock);
                }
            } else {
                final IBlockState state = access.getBlockState(pos);
                access.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);

                for (y -= 1;(access.isAirBlock(x,y,z) || canFallThrough(access.getState(x,y,z))) && y > 0; y-=1) {}
                if (y > 0) {
                    access.setBlockState(x, y+1, z, state, 3);
                }
            }
        }
    }


    protected void onStartFalling(final EntityFallingBlock fallingEntity) {}


    @Override
    public int tickRate(final World worldIn) {
        return 2;
    }


    public static boolean canFallThrough(final IBlockState state) {
        final Block block = state.getBlock();
        final Material material = state.getMaterial();
        return block == Blocks.FIRE || material == Material.AIR || material == Material.WATER || material == Material.LAVA;
    }


    public void onEndFalling(final World worldIn, final BlockPos pos, final IBlockState fallingState, final IBlockState hitState) {}


    public void onBroken(final World worldIn, final BlockPos pos) {}


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(final IBlockState stateIn, final World worldIn, final BlockPos pos, final Random rand) {
        if (rand.nextInt(16) == 0) {
            final BlockPos blockpos = pos.down();
            if (canFallThrough(worldIn.getBlockState(blockpos))) {
                final double d0 = pos.getX() + rand.nextFloat();
                final double d2 = pos.getY() - 0.05;
                final double d3 = pos.getZ() + rand.nextFloat();
                worldIn.spawnParticle(EnumParticleTypes.FALLING_DUST, d0, d2, d3, 0.0, 0.0, 0.0, Block.getStateId(stateIn));
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public int getDustColor(final IBlockState state) {
        return -16777216;
    }
}
