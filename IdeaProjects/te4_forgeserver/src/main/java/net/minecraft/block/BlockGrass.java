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
import plus.fserver.server.elca.ChunkLCache;
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
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        Block block = access.getBlockState(pos.up()).getBlock();
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
    public void updateTick(final World world, final BlockPos pos, IBlockState state, final Random rand) {
        if (!world.isRemote) {
            ChunkSelector selector = world.localSelector;
            final int posX, posZ;
            if (selector.isAreaLoaded3(posX = pos.getX(), posZ = pos.getX())) {
                final int posY;
                int num = world.getLight(posX, (posY = pos.getY()) + 1, posZ, true);

                final BlockPos up = world.localTempPos.setPos(posX, posY + 1, posZ);

                if (num < 4 && selector.getState(up).getLightOpacity(world, up) > 2) {
                    selector.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
                } else if (num >= 9) {
                    ChunkLCache cache = world.bigLocalCache;
                    cache.init(selector.current());
                    BlockPos.MutableBlockPos blockPos = world.localTempPos2;
                    for (int i = 0; i < 4; ++i) {
                        num = rand.nextInt();
                        blockPos.setPosB(
                                posX + (( num       & 3) % 3) - 1,
                                posY + (((num >> 4) & 7) % 5) - 3,
                                posZ + (((num >> 2) & 3) % 3) - 1
                        );
                        if (blockPos.getY() < 0 || blockPos.getY() > 255) return;
                        Chunk chunk = cache.getExecutor(blockPos.getX() >> 4, blockPos.getZ() >> 4).getLoadedChunk();
                        if (chunk == null) return;

                        final IBlockState state1 = chunk.getBlockState(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
                        final IBlockState state2 = chunk.getBlockStateUnchecked(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                        if (state2.getBlock() == Blocks.DIRT && state2.getValue(BlockDirt.VARIANT)
                                == BlockDirt.DirtType.DIRT &&
                                world.getLight(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), true) >= 4
                                && state1.getLightOpacity(world, up) <= 2) {

                            selector.setBlockState(blockPos, Blocks.GRASS.getDefaultState());
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
