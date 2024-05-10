package plus.fserver.world.generator.three;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import plus.fserver.server.elca.ChunkSelector;
import java.util.Random;


public final class SaplingsGenerator {
    public static final IBlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);

    private final boolean doBlockNotify;
    private final byte biomeEdit;
    private final IBlockState state;

    public SaplingsGenerator(boolean doBlockNotify, byte biomeEdit, IBlockState state) {
        this.doBlockNotify = doBlockNotify;
        this.biomeEdit = biomeEdit;
        this.state = state;
    }


    public void generateSaplings(World worldIn, Random random, BlockPos pos) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        this.placePodzolCircle(selector, worldIn, pos.west().north());
        this.placePodzolCircle(selector, worldIn, pos.east(2).north());
        this.placePodzolCircle(selector, worldIn, pos.west().south(2));
        this.placePodzolCircle(selector, worldIn, pos.east(2).south(2));

        for (int i = 0; i < 5; ++i) {
            int j = random.nextInt(64);
            int k = j % 8;
            int l = j / 8;

            if (k == 0 || k == 7 || l == 0 || l == 7) {
                this.placePodzolCircle(selector, worldIn, pos.add(-3 + k, 0, -3 + l));
            }
        }
    }


    private void placePodzolCircle(ChunkSelector selector, World worldIn, BlockPos center) {
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) != 2 || Math.abs(j) != 2) {
                    this.placePodzolAt(selector, worldIn, center.add(i, 0, j));
                }
            }
        }
    }


    private void placePodzolAt(ChunkSelector selector, World worldIn, BlockPos pos) {
        for (int i = 2; i >= -3; --i) {
            BlockPos blockpos = pos.up(i);
            IBlockState iblockstate = selector.getState(blockpos);
            Block block = iblockstate.getBlock();

            if (block.canSustainPlant(iblockstate, worldIn, blockpos, net.minecraft.util.EnumFacing.UP, ((net.minecraft.block.BlockSapling) Blocks.SAPLING))) {
                this.setBlockAndNotifyAdequately(selector, blockpos);
                break;
            }

            if (iblockstate.getMaterial() != Material.AIR && i < 0) {
                break;
            }
        }
    }


    private void setBlockAndNotifyAdequately(ChunkSelector selector, BlockPos pos) {
        if(biomeEdit != 0){
            byte[] biomes = selector.getChunk(pos).getBiomeArray();
            biomes[(pos.getZ() & 15) << 4 | (pos.getX() & 15)] = biomeEdit;
        }
        if (this.doBlockNotify) {
            selector.setBlockState(pos, state, 3);
        } else {
            selector.setBlockState(pos, state, ForgeModContainer.fixVanillaCascading ? 2| 16 : 2);
        }
    }
}