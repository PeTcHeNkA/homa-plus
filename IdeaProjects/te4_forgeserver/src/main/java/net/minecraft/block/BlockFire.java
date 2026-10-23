package net.minecraft.block;

import net.minecraft.block.properties.*;
import com.google.common.collect.*;
import net.minecraft.util.math.*;
import javax.annotation.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.block.material.*;
import net.minecraft.init.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.util.*;
import net.minecraft.block.state.*;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.AABBList;


public class BlockFire extends Block {
    public static final PropertyInteger AGE;
    public static final PropertyBool NORTH;
    public static final PropertyBool EAST;
    public static final PropertyBool SOUTH;
    public static final PropertyBool WEST;
    public static final PropertyBool UPPER;
    private final Map<Block, Integer> encouragements;
    private final Map<Block, Integer> flammabilities;


    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess access, final BlockPos pos) {
        BlockPos down = pos.down();
        if (!access.getBlockState(down).isSideSolid(access, down, EnumFacing.UP) && !Blocks.FIRE.canCatchFire(access, down, EnumFacing.UP)) {
            return state
                    .withProperty(BlockFire.NORTH, this.canCatchFire(access, pos.north(), EnumFacing.SOUTH))
                    .withProperty(BlockFire.EAST , this.canCatchFire(access, pos.east() , EnumFacing.WEST))
                    .withProperty(BlockFire.SOUTH, this.canCatchFire(access, pos.south(), EnumFacing.NORTH))
                    .withProperty(BlockFire.WEST , this.canCatchFire(access, pos.west() , EnumFacing.EAST))
                    .withProperty(BlockFire.UPPER, this.canCatchFire(access, pos.up()   , EnumFacing.DOWN));
        }
        return this.getDefaultState();
    }


    protected BlockFire() {
        super(Material.FIRE);
        this.encouragements = Maps.newIdentityHashMap();
        this.flammabilities = Maps.newIdentityHashMap();
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockFire.AGE, 0).withProperty(BlockFire.NORTH, false).withProperty(BlockFire.EAST, false).withProperty(BlockFire.SOUTH, false).withProperty(BlockFire.WEST, false).withProperty(BlockFire.UPPER, false));
        this.setTickRandomly(true);
    }


    public static void init() {
        BlockFire FIRE = Blocks.FIRE;

        FIRE.setFireInfo(Blocks.PLANKS, 5, 20);
        FIRE.setFireInfo(Blocks.DOUBLE_WOODEN_SLAB, 5, 20);
        FIRE.setFireInfo(Blocks.WOODEN_SLAB, 5, 20);
        FIRE.setFireInfo(Blocks.OAK_FENCE_GATE, 5, 20);
        FIRE.setFireInfo(Blocks.SPRUCE_FENCE_GATE, 5, 20);
        FIRE.setFireInfo(Blocks.BIRCH_FENCE_GATE, 5, 20);
        FIRE.setFireInfo(Blocks.JUNGLE_FENCE_GATE, 5, 20);
        FIRE.setFireInfo(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
        FIRE.setFireInfo(Blocks.ACACIA_FENCE_GATE, 5, 20);
        FIRE.setFireInfo(Blocks.OAK_FENCE, 5, 20);
        FIRE.setFireInfo(Blocks.SPRUCE_FENCE, 5, 20);
        FIRE.setFireInfo(Blocks.BIRCH_FENCE, 5, 20);
        FIRE.setFireInfo(Blocks.JUNGLE_FENCE, 5, 20);
        FIRE.setFireInfo(Blocks.DARK_OAK_FENCE, 5, 20);
        FIRE.setFireInfo(Blocks.ACACIA_FENCE, 5, 20);
        FIRE.setFireInfo(Blocks.OAK_STAIRS, 5, 20);
        FIRE.setFireInfo(Blocks.BIRCH_STAIRS, 5, 20);
        FIRE.setFireInfo(Blocks.SPRUCE_STAIRS, 5, 20);
        FIRE.setFireInfo(Blocks.JUNGLE_STAIRS, 5, 20);
        FIRE.setFireInfo(Blocks.ACACIA_STAIRS, 5, 20);
        FIRE.setFireInfo(Blocks.DARK_OAK_STAIRS, 5, 20);
        FIRE.setFireInfo(Blocks.LOG, 5, 5);
        FIRE.setFireInfo(Blocks.LOG2, 5, 5);
        FIRE.setFireInfo(Blocks.LEAVES, 30, 60);
        FIRE.setFireInfo(Blocks.LEAVES2, 30, 60);
        FIRE.setFireInfo(Blocks.BOOKSHELF, 30, 20);
        FIRE.setFireInfo(Blocks.TNT, 15, 100);
        FIRE.setFireInfo(Blocks.TALLGRASS, 60, 100);
        FIRE.setFireInfo(Blocks.DOUBLE_PLANT, 60, 100);
        FIRE.setFireInfo(Blocks.YELLOW_FLOWER, 60, 100);
        FIRE.setFireInfo(Blocks.RED_FLOWER, 60, 100);
        FIRE.setFireInfo(Blocks.DEADBUSH, 60, 100);
        FIRE.setFireInfo(Blocks.WOOL, 30, 60);
        FIRE.setFireInfo(Blocks.VINE, 15, 100);
        FIRE.setFireInfo(Blocks.COAL_BLOCK, 5, 5);
        FIRE.setFireInfo(Blocks.HAY_BLOCK, 60, 20);
        FIRE.setFireInfo(Blocks.CARPET, 60, 20);
    }


    public void setFireInfo(final Block blockIn, final int encouragement, final int flammability) {
        if (blockIn == Blocks.AIR) {
            throw new IllegalArgumentException("Tried to set air on fire... This is bad.");
        }
        this.encouragements.put(blockIn, encouragement);
        this.flammabilities.put(blockIn, flammability);
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos) {
        return BlockFire.NULL_AABB;
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList collidingBoxes) {}


    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }


    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }


    @Override
    public int quantityDropped(final Random random) {
        return 0;
    }


    @Override
    public int tickRate(final World worldIn) {
        return 30;
    }


    @Override
    public void updateTick(final World world, final BlockPos pos, IBlockState state, final Random rand) {
        if (world.getGameRules().doFireTick.getBoolean()) {
            if (!world.isAreaLoaded(pos, 2)) {
                return;
            }
            ChunkSelector selector = world.localSelector; //safe
            if (!this.canPlaceBlockAt(world, pos)) {
                selector.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
            }

            BlockPos.MutableBlockPos temp = world.localTempPos.setPos(pos.getX(), pos.getY()-1, pos.getZ()); //safe

            final Block block = selector.getState(temp).getBlock();
            final boolean flag = block.isFireSource(world, temp, EnumFacing.UP);

            final int sAge = state.getValue(BlockFire.AGE);
            if (!flag && world.isRaining() && this.canDie(world, pos) && rand.nextFloat() < 0.2f + sAge * 0.03f) {
                selector.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
            }
            else {
                if (sAge < 15) {
                    state = state.withProperty(BlockFire.AGE, sAge + rand.nextInt(3) / 2);
                    selector.setBlockState(pos, state, 4);
                }
                world.scheduleUpdate(pos, this, this.tickRate(world) + rand.nextInt(10));
                if (!flag) {
                    if (!this.canNeighborCatchFire(selector, pos)) {
                        if (!selector.getBlockState(temp).isSideSolid(world, temp, EnumFacing.UP) || sAge > 3) {
                            selector.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
                        }
                        return;
                    }
                    if (!this.canCatchFire(selector, temp, EnumFacing.UP) && sAge == 15 && rand.nextInt(4) == 0) {
                        selector.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
                        return;
                    }
                }
                final boolean flag2 = selector.getBiome(pos).isHighHumidity();
                int ch = 0;
                if (flag2) {
                    ch = -50;
                }

                temp.setPosB(pos.getX()+1, pos.getY(), pos.getZ());
                this.tryCatchFire(world, selector, temp, 300 + ch, rand, sAge, EnumFacing.WEST);

                temp.setPosB(pos.getX()-1, pos.getY(), pos.getZ());
                this.tryCatchFire(world, selector, temp, 300 + ch, rand, sAge, EnumFacing.EAST);

                temp.setPosB(pos.getX(), pos.getY()+1, pos.getZ());
                this.tryCatchFire(world, selector, temp, 250 + ch, rand, sAge, EnumFacing.UP);

                temp.setPosB(pos.getX(), pos.getY()-1, pos.getZ());
                this.tryCatchFire(world, selector, temp, 250 + ch, rand, sAge, EnumFacing.DOWN);

                temp.setPosB(pos.getX(), pos.getY(), pos.getZ()+1);
                this.tryCatchFire(world, selector, temp, 300 + ch, rand, sAge, EnumFacing.SOUTH);

                temp.setPosB(pos.getX(), pos.getY(), pos.getZ()-1);
                this.tryCatchFire(world, selector, temp, 300 + ch, rand, sAge, EnumFacing.NORTH);

                for (int dX = -1; dX <= 1; ++dX) {
                    for (int dZ = -1; dZ <= 1; ++dZ) {
                        for (int dY = -1; dY <= 4; ++dY) {
                            if (dX != 0 || dY != 0 || dZ != 0) {
                                ch = 100;
                                if (dY > 1) {
                                    ch += (dY - 1) * 100;
                                }
                                final BlockPos blockpos = temp.setPos(dX+pos.getX(), dY+pos.getY(), dZ+pos.getZ()); //pos.add(dX, dY, dZ);
                                final int k2 = getNeighborEncouragement(selector, world, blockpos);
                                if (k2 > 0) {
                                    int l2 = (k2 + 40 + world.getDifficulty().getId() * 7) / (sAge + 30);
                                    if (flag2) {
                                        l2 /= 2;
                                    }
                                    if (l2 > 0 && rand.nextInt(ch) <= l2 && (!world.isRaining() || !this.canDie(world, blockpos))) {
                                        int age = sAge + rand.nextInt(5) / 4;
                                        if (age > 15) {
                                            age = 15;
                                        }
                                        selector.setBlockState(blockpos, state.withProperty(BlockFire.AGE, age), 3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    protected boolean canDie(final World worldIn, final BlockPos pos) {
        return worldIn.isRainingAt(pos) || worldIn.isRainingAt(pos.west()) || worldIn.isRainingAt(pos.east()) || worldIn.isRainingAt(pos.north()) || worldIn.isRainingAt(pos.south());
    }


    @Override
    public boolean requiresUpdates() {
        return false;
    }


    @Deprecated
    public int getFlammability(final Block blockIn) {
        final Integer integer = this.flammabilities.get(blockIn);
        return (integer == null) ? 0 : integer;
    }


    @Deprecated
    public int getEncouragement(final Block blockIn) {
        final Integer integer = this.encouragements.get(blockIn);
        return (integer == null) ? 0 : integer;
    }


    @Deprecated
    private void catchOnFire(final World worldIn, final BlockPos pos, final int chance, final Random random, final int age) {
        this.tryCatchFire(worldIn, pos, chance, random, age, EnumFacing.UP);
    }


    private void tryCatchFire(final World world, ChunkSelector selector, final BlockPos pos, final int chance, final Random rand, final int chance2, final EnumFacing facing) {
        final int i = selector.getBlockState(pos).getBlock().getFlammability(world, pos, facing);
        if (rand.nextInt(chance) < i) {
            final IBlockState iblockstate = world.getBlockState(pos);
            if (rand.nextInt(chance2 + 10) < 5 && !world.isRainingAt(pos)) {
                int j = chance2 + rand.nextInt(5) / 4;
                if (j > 15) {
                    j = 15;
                }
                selector.setBlockState(pos, this.getDefaultState().withProperty(BlockFire.AGE, j), 3);
            }
            else {
                selector.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
            }
            if (iblockstate.getBlock() == Blocks.TNT) {
                Blocks.TNT.onPlayerDestroy(world, pos, iblockstate.withProperty(BlockTNT.EXPLODE, true));
            }
        }
    }


    private void tryCatchFire(final World world, final BlockPos pos, final int chance, final Random rand, final int chance2, final EnumFacing facing) {
        final int i = world.getBlockState(pos).getBlock().getFlammability(world, pos, facing);
        if (rand.nextInt(chance) < i) {
            final IBlockState iblockstate = world.getBlockState(pos);
            if (rand.nextInt(chance2 + 10) < 5 && !world.isRainingAt(pos)) {
                int j = chance2 + rand.nextInt(5) / 4;
                if (j > 15) {
                    j = 15;
                }
                world.setBlockState(pos, this.getDefaultState().withProperty(BlockFire.AGE, j), 3);
            }
            else {
                world.setBlockToAir(pos);
            }
            if (iblockstate.getBlock() == Blocks.TNT) {
                Blocks.TNT.onPlayerDestroy(world, pos, iblockstate.withProperty(BlockTNT.EXPLODE, true));
            }
        }
    }


    private boolean canNeighborCatchFire(final IBlockEAccess access, final BlockPos pos) {
        for (final EnumFacing facing : EnumFacing.VALUES) {
            if (this.canCatchFire(access, pos.offset(facing), facing.getOpposite())) {
                return true;
            }
        }
        return false;
    }


    @Deprecated
    private static int getNeighborEncouragement(final ChunkSelector selector, final World world, final BlockPos pos) {
        if (!selector.isAirBlock(pos)) return 0;
        int i = 0;
        final BlockPos.MutableBlockPos temp = world.localTempPos2; //safe
        for (final EnumFacing facing : EnumFacing.VALUES) {
            temp.setPosB(
                    facing.getXOffset() + pos.getX(),
                    facing.getYOffset() + pos.getY(),
                    facing.getZOffset() + pos.getZ()
            );
            i = Math.max(selector.getState(temp).getBlock().getFireSpreadSpeed(selector, temp, facing.getOpposite()), i);
        }
        return i;
    }


    @Override
    public boolean isCollidable() {
        return false;
    }


    @Deprecated
    public boolean canCatchFire(final IBlockAccess access, final BlockPos pos) {
        return this.canCatchFire(access, pos, EnumFacing.UP);
    }


    @Override
    public boolean canPlaceBlockAt(final World world, final BlockPos pos) {
        return world.access().getBlockState(pos.getX(), pos.getY()-1, pos.getZ()).isTopSolid() || this.canNeighborCatchFire(world.access(), pos);
    }


    @Override
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        final IBlockEAccess access = world.access();
        if (!world.getBlockState(pos.getX(), pos.getY()-1, pos.getZ()).isTopSolid() && !this.canNeighborCatchFire(access, pos)) {
            access.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
        }
    }


    @Override
    public void onBlockAdded(final World world, final BlockPos pos, final IBlockState state) {
        if (world.provider.getDimensionType().getId() > 0 || !Blocks.PORTAL.trySpawnPortal(world, pos)) {
            final IBlockEAccess access = world.access();
            if (!access.getBlockState(pos.getX(), pos.getY()-1, pos.getZ()).isTopSolid() && !this.canNeighborCatchFire(access, pos)) {
                access.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
            }
            else {
                world.scheduleUpdate(pos, this, this.tickRate(world) + world.rand.nextInt(10));
            }
        }
    }


    @Override
    public MapColor getMapColor(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
        return MapColor.TNT;
    }


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(final IBlockState stateIn, final World world, final BlockPos pos, final Random rand) {
        if (rand.nextInt(24) == 0) {
            world.playSound(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0f + rand.nextFloat(), rand.nextFloat() * 0.7f + 0.3f, false);
        }
        if (!world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP) && !Blocks.FIRE.canCatchFire(world, pos.down(), EnumFacing.UP)) {
            if (Blocks.FIRE.canCatchFire(world, pos.west(), EnumFacing.EAST)) {
                for (int j = 0; j < 2; ++j) {
                    final double d3 = pos.getX() + rand.nextDouble() * 0.10000000149011612;
                    final double d4 = pos.getY() + rand.nextDouble();
                    final double d5 = pos.getZ() + rand.nextDouble();
                    world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d3, d4, d5, 0.0, 0.0, 0.0, new int[0]);
                }
            }
            if (Blocks.FIRE.canCatchFire(world, pos.east(), EnumFacing.WEST)) {
                for (int k = 0; k < 2; ++k) {
                    final double d6 = pos.getX() + 1 - rand.nextDouble() * 0.10000000149011612;
                    final double d7 = pos.getY() + rand.nextDouble();
                    final double d8 = pos.getZ() + rand.nextDouble();
                    world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d6, d7, d8, 0.0, 0.0, 0.0, new int[0]);
                }
            }
            if (Blocks.FIRE.canCatchFire(world, pos.north(), EnumFacing.SOUTH)) {
                for (int l = 0; l < 2; ++l) {
                    final double d9 = pos.getX() + rand.nextDouble();
                    final double d10 = pos.getY() + rand.nextDouble();
                    final double d11 = pos.getZ() + rand.nextDouble() * 0.10000000149011612;
                    world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d9, d10, d11, 0.0, 0.0, 0.0, new int[0]);
                }
            }
            if (Blocks.FIRE.canCatchFire(world, pos.south(), EnumFacing.NORTH)) {
                for (int i1 = 0; i1 < 2; ++i1) {
                    final double d12 = pos.getX() + rand.nextDouble();
                    final double d13 = pos.getY() + rand.nextDouble();
                    final double d14 = pos.getZ() + 1 - rand.nextDouble() * 0.10000000149011612;
                    world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d12, d13, d14, 0.0, 0.0, 0.0, new int[0]);
                }
            }
            if (Blocks.FIRE.canCatchFire(world, pos.up(), EnumFacing.DOWN)) {
                for (int j2 = 0; j2 < 2; ++j2) {
                    final double d15 = pos.getX() + rand.nextDouble();
                    final double d16 = pos.getY() + 1 - rand.nextDouble() * 0.10000000149011612;
                    final double d17 = pos.getZ() + rand.nextDouble();
                    world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d15, d16, d17, 0.0, 0.0, 0.0, new int[0]);
                }
            }
        }
        else {
            for (int m = 0; m < 3; ++m) {
                final double d18 = pos.getX() + rand.nextDouble();
                final double d19 = pos.getY() + rand.nextDouble() * 0.5 + 0.5;
                final double d20 = pos.getZ() + rand.nextDouble();
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d18, d19, d20, 0.0, 0.0, 0.0, new int[0]);
            }
        }
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockFire.AGE, meta);
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(BlockFire.AGE);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockFire.AGE, BlockFire.NORTH, BlockFire.EAST, BlockFire.SOUTH, BlockFire.WEST, BlockFire.UPPER);
    }


    public boolean canCatchFire(final IBlockAccess access, final BlockPos pos, final EnumFacing facing) {
        return access.getBlockState(pos).getBlock().isFlammable(access, pos, facing);
    }


    @Deprecated
    public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final IBlockState state, final BlockPos pos, final EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }


    static {
        AGE = PropertyInteger.create("age", 0, 15);
        NORTH = PropertyBool.create("north");
        EAST = PropertyBool.create("east");
        SOUTH = PropertyBool.create("south");
        WEST = PropertyBool.create("west");
        UPPER = PropertyBool.create("up");
    }
}