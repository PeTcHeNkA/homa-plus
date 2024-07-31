package net.minecraft.block;

import net.minecraft.block.properties.*;
import net.minecraft.world.*;
import java.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.common.*;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraft.item.*;
import net.minecraft.block.state.*;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class BlockCrops extends BlockBush implements IGrowable {
    public static final PropertyInteger AGE;
    private static final AxisAlignedBB[] CROPS_AABB;

    protected BlockCrops() {
        this.setDefaultState(this.blockState.getBaseState().withProperty(this.getAgeProperty(), 0));
        this.setTickRandomly(true);
        this.setCreativeTab(null);
        this.setHardness(0.0f);
        this.setSoundType(SoundType.PLANT);
        this.disableStats();
    }


    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return BlockCrops.CROPS_AABB[state.getValue(this.getAgeProperty())];
    }


    @Override
    protected boolean canSustainBush(final IBlockState state) {
        return state.getBlock() == Blocks.FARMLAND;
    }


    protected PropertyInteger getAgeProperty() {
        return BlockCrops.AGE;
    }


    public int getMaxAge() {
        return 7;
    }


    protected int getAge(final IBlockState state) {
        return state.getValue(this.getAgeProperty());
    }


    public IBlockState withAge(final int age) {
        return this.getDefaultState().withProperty(this.getAgeProperty(), age);
    }


    public boolean isMaxAge(final IBlockState state) {
        return state.getValue(this.getAgeProperty()) >= this.getMaxAge();
    }


    @Override
    public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random rand) {
        super.updateTick(world, pos, state, rand);
        if (world.isRemote || !world.isAreaLoaded(pos, 1)) return;

        if (world.getLightFromNeighbors(
                world.localTempPos.setPos(pos.getX(), pos.getY()+1,pos.getZ())) >= 9) {

            final int age = this.getAge(state);
            if (age < this.getMaxAge()) {
                final float f = getGrowthChance(this, world, pos);
                if (ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt((int)(25.0f / f) + 1) == 0)) {
                    IBlockState resultState = this.withAge(age + 1);
                    world.access().setBlockState(pos, resultState, 2);
                    ForgeHooks.onCropsGrowPost(world, pos, state, resultState);
                }
            }
        }
    }


    public void grow(final World worldIn, final BlockPos pos, final IBlockState state) {
        int i = this.getAge(state) + this.getBonemealAgeIncrease(worldIn);
        final int j = this.getMaxAge();
        if (i > j) {
            i = j;
        }
        worldIn.access().setBlockState(pos, this.withAge(i), 2);
    }


    protected int getBonemealAgeIncrease(final World worldIn) {
        return MathHelper.getInt(worldIn.rand, 2, 5);
    }


    protected static float getGrowthChance(final Block block, final World world, final BlockPos pos) {
        ChunkSelector access = world.localSelector;
        if(access == null)return 0.01f;

        float f = 1.0f;
        int px = pos.getX(), py = pos.getY()-1, pz = pos.getZ();
        BlockPos.MutableBlockPos mut = world.localTempPos;
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                mut.setPosB(px + dx, py, pz + dz);
                float f2 = 0.0f;
                final IBlockState state = access.getBlockState(mut);
                if (state.getBlock().canSustainPlant(state, world, mut, EnumFacing.UP, (IPlantable)block)) {
                    f2 = 1.0f;
                    if (state.getBlock().isFertile(world, mut)) {
                        f2 = 3.0f;
                    }
                }
                if (dx != 0 || dz != 0) {
                    f2 /= 4.0f;
                }
                f += f2;
            }
        }
        py += 1;
        if(py > 255 || py < 0)return 0;

        final boolean flag = block == access.getStateUnchecked(px+1, py, pz).getBlock()
                          || block == access.getStateUnchecked(px-1, py, pz).getBlock();

        final boolean flag2 = block == access.getStateUnchecked(px, py, pz+1).getBlock()
                           || block == access.getStateUnchecked(px, py, pz-1).getBlock();

        if (flag && flag2) {
            f /= 2.0f;
        }
        else {
            final boolean flag3 = block == access.getStateUnchecked(px+1, py, pz+1).getBlock()
                               || block == access.getStateUnchecked(px+1, py, pz-1).getBlock()
                               || block == access.getStateUnchecked(px-1, py, pz+1).getBlock()
                               || block == access.getStateUnchecked(px-1, py, pz-1).getBlock();

            if (flag3) {
                f /= 2.0f;
            }
        }
        return f;
    }


    @Override
    public boolean canBlockStay(final World world, final BlockPos pos, final IBlockState state) {
        final IBlockEAccess access = world.access();
        final IBlockState soil = access.getBlockState(pos.getX(), pos.getY()-1, pos.getZ());
        return (world.getLight(pos) >= 8 || world.canSeeSky(pos)) && soil.getBlock().canSustainPlant(soil, access, pos.down(), EnumFacing.UP, this);
    }


    protected Item getSeed() {
        return Items.WHEAT_SEEDS;
    }


    protected Item getCrop() {
        return Items.WHEAT;
    }


    @Override
    public void getDrops(final NonNullList<ItemStack> drops, final IBlockAccess access, final BlockPos pos, final IBlockState state, final int fortune) {
        super.getDrops(drops, access, pos, state, 0);
        final int age = this.getAge(state);
        final Random rand = (access instanceof World) ? ((World)access).rand : FastRandom.INST;
        if (age >= this.getMaxAge()) {
            final int k = 3 + fortune;
            for (int i = 0; i < k; ++i) {
                if (rand.nextInt(2 * this.getMaxAge()) <= age) {
                    drops.add(new ItemStack(this.getSeed(), 1, 0));
                }
            }
        }
    }


    @Override
    public void dropBlockAsItemWithChance(final World worldIn, final BlockPos pos, final IBlockState state, final float chance, final int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }


    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return this.isMaxAge(state) ? this.getCrop() : this.getSeed();
    }


    @Override
    public ItemStack getItem(final World worldIn, final BlockPos pos, final IBlockState state) {
        return new ItemStack(this.getSeed());
    }


    @Override
    public boolean canGrow(final World worldIn, final BlockPos pos, final IBlockState state, final boolean isClient) {
        return !this.isMaxAge(state);
    }


    @Override
    public boolean canUseBonemeal(final World worldIn, final Random rand, final BlockPos pos, final IBlockState state) {
        return true;
    }


    @Override
    public void grow(final World worldIn, final Random rand, final BlockPos pos, final IBlockState state) {
        this.grow(worldIn, pos, state);
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.withAge(meta);
    }


    @Override
    public int getMetaFromState(final IBlockState state) {
        return this.getAge(state);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockCrops.AGE);
    }

    static {
        AGE = PropertyInteger.create("age", 0, 7);
        CROPS_AABB = new AxisAlignedBB[] { new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.25, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.375, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.625, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.75, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.875, 1.0), new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0) };
    }
}