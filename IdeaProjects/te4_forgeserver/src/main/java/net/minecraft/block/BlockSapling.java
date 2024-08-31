package net.minecraft.block;

import net.minecraft.block.properties.*;
import net.minecraft.creativetab.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.translation.*;
import net.minecraft.world.*;
import java.util.*;
import net.minecraftforge.event.terraingen.*;
import net.minecraft.init.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.util.*;
import net.minecraft.item.*;
import net.minecraft.block.state.*;
import plus.fserver.api.IBlockEAccess;


public class BlockSapling extends BlockBush implements IGrowable {
    public static final PropertyEnum<BlockPlanks.EnumType> TYPE;
    public static final PropertyInteger STAGE;
    protected static final AxisAlignedBB SAPLING_AABB;

    protected BlockSapling() {
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.OAK).withProperty(BlockSapling.STAGE, 0));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }


    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return BlockSapling.SAPLING_AABB;
    }


    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal(this.getTranslationKey() + "." + BlockPlanks.EnumType.OAK.getTranslationKey() + ".name");
    }


    @Override
    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand) {
        if (!worldIn.isRemote) {
            super.updateTick(worldIn, pos, state, rand);
            if (!worldIn.isAreaLoaded(pos, 1)) {
                return;
            }
            if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0) {
                this.grow(worldIn, pos, state, rand);
            }
        }
    }


    public void grow(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand) {
        if (state.getValue(BlockSapling.STAGE) == 0) {
            worldIn.setBlockState(pos, state.cycleProperty(BlockSapling.STAGE), 4);
        } else {
            this.generateTree(worldIn, pos, state, rand);
        }
    }


    public void generateTree(final World world, final BlockPos pos, final IBlockState state, final Random rand) {
        if (!TerrainGen.saplingGrowTree(world, rand, pos)) {
            return;
        }
        WorldGenerator gen = (rand.nextInt(10) == 0) ? new WorldGenBigTree(true) : new WorldGenTrees(true);
        int i = 0;
        int j = 0;
        boolean flag = false;
        switch (state.getValue(BlockSapling.TYPE)) {
            case SPRUCE: {
                Label_0175:
                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (this.isTwoByTwoOfType(world, pos, i, j, BlockPlanks.EnumType.SPRUCE)) {
                            gen = new WorldGenMegaPineTree(false, rand.nextBoolean());
                            flag = true;
                            break Label_0175;
                        }
                    }
                }
                if (!flag) {
                    i = 0;
                    j = 0;
                    gen = new WorldGenTaiga2(true);
                    break;
                }
                break;
            }
            case BIRCH: {
                gen = new WorldGenBirchTree(true, false);
                break;
            }
            case JUNGLE: {
                final IBlockState iblockstate = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
                final IBlockState iblockstate2 = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, false);
                Label_0333:
                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (this.isTwoByTwoOfType(world, pos, i, j, BlockPlanks.EnumType.JUNGLE)) {
                            gen = new WorldGenMegaJungle(true, 10, 20, iblockstate, iblockstate2);
                            flag = true;
                            break Label_0333;
                        }
                    }
                }
                if (!flag) {
                    i = 0;
                    j = 0;
                    gen = new WorldGenTrees(true, 4 + rand.nextInt(7), iblockstate, iblockstate2, false);
                    break;
                }
                break;
            }
            case ACACIA: {
                gen = new WorldGenSavannaTree(true);
                break;
            }
            case DARK_OAK: {
                Label_0446:
                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (this.isTwoByTwoOfType(world, pos, i, j, BlockPlanks.EnumType.DARK_OAK)) {
                            gen = new WorldGenCanopyTree(true);
                            flag = true;
                            break Label_0446;
                        }
                    }
                }
                if (!flag) {
                    return;
                }
                break;
            }
        }
        final IBlockState air = Blocks.AIR.getDefaultState();
        final IBlockEAccess access = world.access();
        if (flag) {
            access.setBlockState(pos.add(i, 0, j), air, 4);
            access.setBlockState(pos.add(i + 1, 0, j), air, 4);
            access.setBlockState(pos.add(i, 0, j + 1), air, 4);
            access.setBlockState(pos.add(i + 1, 0, j + 1), air, 4);
        }
        else {
            access.setBlockState(pos, air, 4);
        }
        if (!gen.generate(world, rand, pos.add(i, 0, j))) {
            if (flag) {
                access.setBlockState(pos.add(i, 0, j), state, 4);
                access.setBlockState(pos.add(i + 1, 0, j), state, 4);
                access.setBlockState(pos.add(i, 0, j + 1), state, 4);
                access.setBlockState(pos.add(i + 1, 0, j + 1), state, 4);
            }
            else {
                access.setBlockState(pos, state, 4);
            }
        }
    }


    private boolean isTwoByTwoOfType(final World world, final BlockPos p, final int x, final int z, final BlockPlanks.EnumType type) {
        BlockPos.MutableBlockPos mut = world.localTempPos2;
        return    this.isTypeAt(world, mut.setPos(p.getX()+x  , p.getY(), p.getZ()+z  ), type)
               && this.isTypeAt(world, mut.setPos(p.getX()+x+1, p.getY(), p.getZ()+z  ), type)
               && this.isTypeAt(world, mut.setPos(p.getX()+x  , p.getY(), p.getZ()+z+1), type)
               && this.isTypeAt(world, mut.setPos(p.getX()+x+1, p.getY(), p.getZ()+z+1), type);
    }


    public boolean isTypeAt(final World worldIn, final BlockPos pos, final BlockPlanks.EnumType type) {
        final IBlockState state = worldIn.access().getBlockState(pos);
        return state.getBlock() == this && state.getValue(BlockSapling.TYPE) == type;
    }


    @Override
    public int damageDropped(final IBlockState state) {
        return state.getValue(BlockSapling.TYPE).getMetadata();
    }


    @Override
    public void getSubBlocks(final CreativeTabs itemIn, final NonNullList<ItemStack> items) {
        for (final BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
    }


    @Override
    public boolean canGrow(final World worldIn, final BlockPos pos, final IBlockState state, final boolean isClient) {
        return true;
    }


    @Override
    public boolean canUseBonemeal(final World worldIn, final Random rand, final BlockPos pos, final IBlockState state) {
        return worldIn.rand.nextFloat() < 0.45;
    }


    @Override
    public void grow(final World worldIn, final Random rand, final BlockPos pos, final IBlockState state) {
        this.grow(worldIn, pos, state, rand);
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.byMetadata(meta & 0x7)).withProperty(BlockSapling.STAGE, (meta & 0x8) >> 3);
    }


    @Override
    public int getMetaFromState(final IBlockState state) {
        int i = 0;
        i |= state.getValue(BlockSapling.TYPE).getMetadata();
        i |= state.getValue(BlockSapling.STAGE) << 3;
        return i;
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockSapling.TYPE, BlockSapling.STAGE);
    }


    static {
        TYPE = PropertyEnum.create("type", BlockPlanks.EnumType.class);
        STAGE = PropertyInteger.create("stage", 0, 1);
        SAPLING_AABB = new AxisAlignedBB(0.09999999403953552, 0.0, 0.09999999403953552, 0.8999999761581421, 0.800000011920929, 0.8999999761581421);
    }
}