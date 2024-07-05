package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.math.Vec3dMut;


public abstract class BlockLiquid extends Block {
    @Keep
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15);

    protected BlockLiquid(Material material) {
        super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, 0));
        this.setTickRandomly(true);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess worldIn, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {}


    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {}


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }


    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return this.material != Material.LAVA;
    }


    public static float getLiquidHeightPercent(int meta) {
        if (meta >= 8) {
            meta = 0;
        }
        return (meta + 1) / 9.0f;
    }


    protected int getDepth(final IBlockState state) {
        return (state.getMaterial() == this.material) ? state.getValue(BlockLiquid.LEVEL) : -1;
    }


    protected int getRenderedDepth(final IBlockState state) {
        final int i = this.getDepth(state);
        return (i >= 8) ? 0 : i;
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return hitIfLiquid && state.getValue(LEVEL) == 0;
    }


    private boolean causesDownwardCurrent(final IBlockAccess access, final BlockPos pos, final EnumFacing side) {
        final IBlockState state = access.getBlockState(pos);
        final Block block = state.getBlock();
        final Material material = state.getMaterial();
        if (material == this.material) return false;
        if (side == EnumFacing.UP)     return true;
        if (material == Material.ICE)  return false;

        final boolean flag = isExceptBlockForAttachWithPiston(block) || block instanceof BlockStairs;
        return !flag && state.getBlockFaceShape(access, pos, side) == BlockFaceShape.SOLID;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(final IBlockState blockState, final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing side) {
        return blockAccess.getBlockState(pos.offset(side)).getMaterial() != this.material && (side == EnumFacing.UP || super.shouldSideBeRendered(blockState, blockAccess, pos, side));
    }


    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.LIQUID;
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }


    @Override
    public int quantityDropped(Random random) {
        return 0;
    }


    protected Vec3d getFlow(final IBlockAccess access, final BlockPos pos, final IBlockState state) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        double d2 = 0.0D;
        int i = this.getRenderedDepth(state);
        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();

        for (EnumFacing enumfacing : BlockIDIndexes.FACING_HORIZONTAL) {
            blockPos.setPos(pos).move(enumfacing);
            int j = this.getRenderedDepth(access.getBlockState(blockPos));

            if (j < 0) {
                if (access.getBlockState(blockPos).getMaterial().blocksMovement()) {
                    continue;
                }
                j = this.getRenderedDepth(access.getBlockState(blockPos.down()));
                if (j < 0) continue;

                final int k = j - (i - 8);
                d0 += enumfacing.getXOffset() * k;
                d1 += enumfacing.getYOffset() * k;
                d2 += enumfacing.getZOffset() * k;
            }
            else {
                if (j < 0) continue;
                final int l = j - i;
                d0 += enumfacing.getXOffset() * l;
                d1 += enumfacing.getYOffset() * l;
                d2 += enumfacing.getZOffset() * l;
            }
        }
        Vec3d vec3d = new Vec3d(d0, d1, d2);
        if (state.getValue(BlockLiquid.LEVEL) >= 8) {
            for (final EnumFacing enumfacing2 : EnumFacing.Plane.HORIZONTAL) {
                blockPos.setPos(pos).move(enumfacing2);
                if (this.causesDownwardCurrent(access, blockPos, enumfacing2) || this.causesDownwardCurrent(access, blockPos.up(), enumfacing2)) {
                    vec3d = vec3d.normalize().add(0.0, -6.0, 0.0);
                    break;
                }
            }
        }
        blockPos.release();
        return vec3d.normalize();
    }


    @Override
    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
        IBlockEAccess access = worldIn.access();
        IBlockState state = access.getBlockState(pos);
        double x = 0.0D, y = 0.0D, z = 0.0D;

        int i = this.getRenderedDepth(state);
        BlockPos.MutableBlockPos blockPos = worldIn.localTempPos;

        for (EnumFacing facing : BlockIDIndexes.FACING_HORIZONTAL) {
            blockPos.setPos(pos).move(facing);
            int j = this.getRenderedDepth(access.getBlockState(blockPos));

            if (j < 0) {
                if (!access.getBlockState(blockPos).getMaterial().blocksMovement()) {
                    j = this.getRenderedDepth(access.getBlockState(blockPos.getX(), blockPos.getY()-1, blockPos.getZ()));

                    if (j >= 0) {
                        int k = j - (i - 8);
                        x += facing.getXOffset() * k;
                        y += facing.getYOffset() * k;
                        z += facing.getZOffset() * k;
                    }
                }
            } else {
                int l = j - i;
                x += facing.getXOffset() * l;
                y += facing.getYOffset() * l;
                z += facing.getZOffset() * l;
            }
        }
        //hotfix NullPointerException
        Integer lvl = state.getValue(LEVEL);
        if (lvl != null && lvl >= 8) {
            Vec3dMut mut = worldIn.commonEntityVec3d1.init(x, y, z);
            for (EnumFacing facing : BlockIDIndexes.FACING_HORIZONTAL) {
                blockPos.setPos(pos).move(facing);

                if (this.causesDownwardCurrent(access, blockPos, facing)) {
                    mut.normalize().addY(-6.0D);
                    break;
                } else {
                    blockPos.setY(blockPos.getY() + 1);
                    if(this.causesDownwardCurrent(access, blockPos, facing)){
                        mut.normalize().addY(-6.0D);
                        break;
                    }
                }
            }

            return mut.normalize().vec();
        } else {
            double d0 = MathHelper.sqrt(x * x + y * y + z * z);
            return d0 < 1.0E-4D ? Vec3d.ZERO : new Vec3d(x / d0, y / d0, z / d0);
        }
    }


    @Override
    public int tickRate(World worldIn) {
        if (this.material == Material.WATER) return 5;
        if (this.material == Material.LAVA) return worldIn.provider.isNether() ? 10 : 30;
        return 0;
    }


    @SideOnly(Side.CLIENT)
    public boolean shouldRenderSides(final IBlockAccess access, final BlockPos pos) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                final IBlockState state = access.getBlockState(pos.add(i, 0, j));
                if (state.getMaterial() != this.material && !state.isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void onBlockAdded(final World world, final BlockPos pos, final IBlockState state) {
        this.checkForMixing(world, pos, state);
    }


    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        this.checkForMixing(world, pos, state);
    }


    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        int i = source.getCombinedLight(pos, 0);
        int j = source.getCombinedLight(pos.up(), 0);
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }


    public boolean checkForMixing(final World worldIn, final BlockPos pos, final IBlockState state) {
        return checkForMixing(worldIn, new ChunkSelector(worldIn), pos, state);
    }


    public boolean checkForMixing(final World world, ChunkSelector selector, final BlockPos pos, final IBlockState state) {
        if (this.material == Material.LAVA) {;
            boolean flag = false;
            for (final EnumFacing enumfacing : EnumFacing.VALUES) {
                if (enumfacing != EnumFacing.DOWN &&
                        world.getBlockState(pos.offset(enumfacing))
                                .getMaterial() == Material.WATER) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                final Integer integer = state.getValue(BlockLiquid.LEVEL);
                if (integer == 0) {
                    selector.setBlockState(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, pos, Blocks.OBSIDIAN.getDefaultState()));
                    this.triggerMixEffects(world, pos);
                    return true;
                }
                if (integer <= 4) {
                    selector.setBlockState(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, pos, Blocks.COBBLESTONE.getDefaultState()));
                    this.triggerMixEffects(world, pos);
                    return true;
                }
            }
        }
        return false;
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return (this.material == Material.WATER) ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
    }


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(final IBlockState stateIn, final World worldIn, final BlockPos pos, final Random rand) {
        final double d0 = pos.getX();
        final double d2 = pos.getY();
        final double d3 = pos.getZ();
        if (this.material == Material.WATER) {
            final int i = stateIn.getValue(BlockLiquid.LEVEL);
            if (i > 0 && i < 8) {
                if (rand.nextInt(64) == 0) {
                    worldIn.playSound(d0 + 0.5, d2 + 0.5, d3 + 0.5, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, rand.nextFloat() * 0.25f + 0.75f, rand.nextFloat() + 0.5f, false);
                }
            }
            else if (rand.nextInt(10) == 0) {
                worldIn.spawnParticle(EnumParticleTypes.SUSPENDED, d0 + rand.nextFloat(), d2 + rand.nextFloat(), d3 + rand.nextFloat(), 0.0, 0.0, 0.0, new int[0]);
            }
        }
        if (this.material == Material.LAVA && worldIn.getBlockState(pos.up()).getMaterial() == Material.AIR && !worldIn.getBlockState(pos.up()).isOpaqueCube()) {
            if (rand.nextInt(100) == 0) {
                final double d4 = d0 + rand.nextFloat();
                final double d5 = d2 + stateIn.getBoundingBox(worldIn, pos).maxY;
                final double d6 = d3 + rand.nextFloat();
                worldIn.spawnParticle(EnumParticleTypes.LAVA, d4, d5, d6, 0.0, 0.0, 0.0, new int[0]);
                worldIn.playSound(d4, d5, d6, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2f + rand.nextFloat() * 0.2f, 0.9f + rand.nextFloat() * 0.15f, false);
            }
            if (rand.nextInt(200) == 0) {
                worldIn.playSound(d0, d2, d3, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2f + rand.nextFloat() * 0.2f, 0.9f + rand.nextFloat() * 0.15f, false);
            }
        }
        if (rand.nextInt(10) == 0 && worldIn.getBlockState(pos.down()).isTopSolid()) {
            final Material material = worldIn.getBlockState(pos.down(2)).getMaterial();
            if (!material.blocksMovement() && !material.isLiquid()) {
                final double d7 = d0 + rand.nextFloat();
                final double d8 = d2 - 1.05;
                final double d9 = d3 + rand.nextFloat();
                if (this.material == Material.WATER) {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d7, d8, d9, 0.0, 0.0, 0.0, new int[0]);
                }
                else {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_LAVA, d7, d8, d9, 0.0, 0.0, 0.0, new int[0]);
                }
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public static float getSlopeAngle(final IBlockAccess worldIn, final BlockPos pos, final Material materialIn, final IBlockState state) {
        final Vec3d vec3d = getFlowingBlock(materialIn).getFlow(worldIn, pos, state);
        return (vec3d.x == 0.0 && vec3d.z == 0.0) ? -1000.0f : ((float)MathHelper.atan2(vec3d.z, vec3d.x) - 1.5707964f);
    }


    protected void triggerMixEffects(World world, BlockPos pos) {
        double d0 = pos.getX();
        double d1 = pos.getY();
        double d2 = pos.getZ();
        world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

        for (int i = 0; i < 8; ++i) {
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d0 + Math.random(), d1 + 1.2D, d2 + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(LEVEL, meta);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LEVEL);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LEVEL);
    }


    public static BlockDynamicLiquid getFlowingBlock(final Material material) {
        if (material == Material.WATER) return Blocks.FLOWING_WATER;
        if (material == Material.LAVA) return Blocks.FLOWING_LAVA;
        throw new IllegalArgumentException("Invalid material");
    }


    public static BlockStaticLiquid getStaticBlock(final Material materialIn) {
        if (materialIn == Material.WATER) {
            return Blocks.WATER;
        }
        if (materialIn == Material.LAVA) {
            return Blocks.LAVA;
        }
        throw new IllegalArgumentException("Invalid material");
    }


    public static float getBlockLiquidHeight(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
        final int i = state.getValue(BlockLiquid.LEVEL);
        return ((i & 0x7) == 0x0 && worldIn.getBlockState(pos.up()).getMaterial() == Material.WATER) ? 1.0f : (1.0f - getLiquidHeightPercent(i));
    }


    public static float getLiquidHeight(final IBlockState state, final IBlockAccess access, final BlockPos pos) {
        return (float)pos.getY() + getBlockLiquidHeight(state, access, pos);
    }


    @Override
    public float getBlockLiquidHeight(World world, BlockPos pos, IBlockState state, Material material) {
        return BlockLiquid.getBlockLiquidHeight(state, world, pos);
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }


    @Override
    @SideOnly (Side.CLIENT)
    public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
        Vec3d viewport = net.minecraft.client.renderer.ActiveRenderInfo.projectViewFromEntity(entity, partialTicks);

        if (state.getMaterial().isLiquid()) {
            float height = 0.0F;
            if (state.getBlock() instanceof BlockLiquid)
            {
                height = getLiquidHeightPercent(state.getValue(LEVEL)) - 0.11111111F;
            }
            float f1 = (float) (pos.getY() + 1) - height;
            if (viewport.y > (double)f1) {
                BlockPos upPos = pos.up();
                IBlockState upState = world.getBlockState(upPos);
                return upState.getBlock().getFogColor(world, upPos, upState, entity, originalColor, partialTicks);
            }
        }

        return super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }
}