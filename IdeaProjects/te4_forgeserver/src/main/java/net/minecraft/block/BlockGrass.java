package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.block.BlockFx;
import plus.fserver.server.elca.ChunkSelector;


public class BlockGrass extends BlockFx implements IGrowable {
    public static final PropertyBool SNOWY = PropertyBool.create("snowy");

    protected BlockGrass() {
        super(Material.GRASS);
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
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), rand, fortune);
    }


    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }


    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }


    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        BlockPos blockpos = pos.up();

        for (int i = 0; i < 128; ++i) {
            BlockPos blockpos1 = blockpos;
            int j = 0;

            while (true) {
                if (j >= i / 16) {
                    if (worldIn.isAirBlock(blockpos1)) {
                        if (rand.nextInt(8) == 0) {
                            worldIn.getBiome(blockpos1).plantFlower(worldIn, rand, blockpos1);
                        } else {
                            IBlockState iblockstate1 = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);

                            if (Blocks.TALLGRASS.canBlockStay(worldIn, blockpos1, iblockstate1)) {
                                worldIn.setBlockState(blockpos1, iblockstate1, 3);
                            }
                        }
                    }
                    break;
                }
                blockpos1 = blockpos1.add(rand.nextInt(3) - 1, (rand.nextInt(3) - 1) * rand.nextInt(3) / 2, rand.nextInt(3) - 1);

                if (worldIn.getBlockState(blockpos1.down()).getBlock() != Blocks.GRASS || worldIn.getBlockState(blockpos1).isNormalCube()) {
                    break;
                }
                ++j;
            }
        }
    }


    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            if (!worldIn.isAreaLoaded(pos, 3)) return;
            BlockPos.MutableBlockPos up =
                    worldIn.localTempPos.setPos(pos.getX(), pos.getY()+1, pos.getZ());

            ChunkSelector selector = worldIn.localSelector;
            Chunk chunk = selector.getChunkE(pos.getX()>>4, pos.getZ()>>4);

            int light = worldIn.getLightFromNeighbors(up);
            IBlockState swap;
            if (light < 4 && (swap = chunk.getBlockState(up)).getLightOpacity(selector, up) > 2) {
                IBlockState newState;
                chunk.setBlockStateFastNoTile(
                        pos.getX(), pos.getY(), pos.getZ(),
                        newState = Blocks.DIRT.getDefaultState()
                );
                worldIn.notifyBlockUpdate(pos, swap, newState, 2);
            } else {
                if (light >= 9) {
                    BlockPos.MutableBlockPos blockpos;
                    for (int i = 0; i < 4; ++i) {
                        blockpos = worldIn.localTempPos2.setPos(
                                pos.getX()+(rand.nextInt(3) - 1),
                                pos.getY()+(rand.nextInt(5) - 4),
                                pos.getZ()+(rand.nextInt(3) - 1)
                        );

                        if (blockpos.getY() < 0 || blockpos.getY() > 255 || !worldIn.isBlockLoaded(blockpos))
                            return;

                        chunk = selector.getChunk(pos.getX()>>4, pos.getZ()>>4);

                        state = chunk.getBlockState(blockpos);
                        up.setPos(blockpos.getX(), blockpos.getY()+1, blockpos.getZ());

                        if (state.getBlock() == Blocks.DIRT && state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT
                                && worldIn.getLightFromNeighbors(up) >= 4 && chunk.getBlockState(up).getLightOpacity(worldIn, up) <= 2) {

                            chunk.setBlockStateFastNoTile(
                                    blockpos.getX(), blockpos.getY(), blockpos.getZ(),
                                    swap = Blocks.GRASS.getDefaultState()
                            );
                            worldIn.notifyBlockUpdate(blockpos, state, swap, 2);
                        }
                    }
                }
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
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
