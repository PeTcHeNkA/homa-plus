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

    protected BlockLiquid(Material materialIn) {
        super(materialIn);
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
        return (float)(meta + 1) / 9.0F;
    }


    protected int getDepth(IBlockState state) {
        return state.getMaterial() == this.material ? state.getValue(LEVEL) : -1;
    }


    protected int getRenderedDepth(IBlockState state) {
        int i = this.getDepth(state);
        return i >= 8 ? 0 : i;
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


    private boolean causesDownwardCurrent(IBlockAccess access, BlockPos pos, EnumFacing side) {
        IBlockState iblockstate = access.getBlockState(pos);
        Block block = iblockstate.getBlock();
        Material material = iblockstate.getMaterial();

        if (material == this.material) {
            return false;
        } else if (side == EnumFacing.UP) {
            return true;
        } else if (material == Material.ICE) {
            return false;
        } else {
            boolean flag = isExceptBlockForAttachWithPiston(block) || block instanceof BlockStairs;
            return !flag && iblockstate.getBlockFaceShape(access, pos, side) == BlockFaceShape.SOLID;
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (blockAccess.getBlockState(pos.offset(side)).getMaterial() == this.material) {
            return false;
        }
        else {
            return side == EnumFacing.UP || super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
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


    protected Vec3d getFlow(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        double d2 = 0.0D;
        int i = this.getRenderedDepth(state);
        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();

        for (EnumFacing enumfacing : BlockIDIndexes.FACING_HORIZONTAL) {
            blockPos.setPos(pos).move(enumfacing);
            int j = this.getRenderedDepth(worldIn.getBlockState(blockPos));

            if (j < 0) {
                if (!worldIn.getBlockState(blockPos).getMaterial().blocksMovement()) {
                    j = this.getRenderedDepth(worldIn.getBlockState(blockPos.down()));

                    if (j >= 0) {
                        int k = j - (i - 8);
                        d0 += enumfacing.getXOffset() * k;
                        d1 += enumfacing.getYOffset() * k;
                        d2 += enumfacing.getZOffset() * k;
                    }
                }
            } else {
                int l = j - i;
                d0 += enumfacing.getXOffset() * l;
                d1 += enumfacing.getYOffset() * l;
                d2 += enumfacing.getZOffset() * l;
            }
        }
        Vec3d vec3d = new Vec3d(d0, d1, d2);

        if (state.getValue(LEVEL) >= 8) {
            for (EnumFacing enumfacing1 : BlockIDIndexes.FACING_HORIZONTAL) {
                blockPos.setPos(pos).move(enumfacing1);

                if (this.causesDownwardCurrent(worldIn, blockPos, enumfacing1) || this.causesDownwardCurrent(worldIn, blockPos.up(), enumfacing1)) {
                    vec3d = vec3d.normalize().add(0.0D, -6.0D, 0.0D);
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

        if (state.getValue(LEVEL) >= 8) {
            Vec3dMut mut = worldIn.commonEntityVec3d1.init(x, y, z);
            //Vec3d vec3d = new Vec3d(x, y, z);
            for (EnumFacing facing : BlockIDIndexes.FACING_HORIZONTAL) {
                blockPos.setPos(pos).move(facing);//TODO WTF NULL POINTER???

                if (this.causesDownwardCurrent(access, blockPos, facing)) {
                    mut.normalize().add(0.0D, -6.0D, 0.0D);
                    break;
                } else {
                    blockPos.setY(blockPos.getY() + 1);
                    if(this.causesDownwardCurrent(access, blockPos, facing)){
                        mut.normalize().add(0.0D, -6.0D, 0.0D);
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
        else if (this.material == Material.LAVA) {
            return worldIn.provider.isNether() ? 10 : 30;
        } else {
            return 0;
        }
    }


    @SideOnly(Side.CLIENT)
    public boolean shouldRenderSides(IBlockAccess blockAccess, BlockPos pos) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                IBlockState iblockstate = blockAccess.getBlockState(pos.add(i, 0, j));
                if (iblockstate.getMaterial() != this.material && !iblockstate.isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.checkForMixing(worldIn, pos, state);
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        this.checkForMixing(worldIn, pos, state);
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


    public boolean checkForMixing(World worldIn, BlockPos pos, IBlockState state) {
        return checkForMixing(worldIn, new ChunkSelector(worldIn), pos, state);
    }


    public boolean checkForMixing(World worldIn, ChunkSelector selector, BlockPos pos, IBlockState state) {
        if (this.material == Material.LAVA) {
            boolean flag = false;

            for (EnumFacing enumfacing : EnumFacing.values()) {
                if (enumfacing != EnumFacing.DOWN
                        && selector.getState(pos.offset(enumfacing)).getMaterial() == Material.WATER) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                int integer = state.getValue(LEVEL);
                if (integer == 0) {
                    selector.setBlockState(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, pos, pos, Blocks.OBSIDIAN.getDefaultState()), 3);
                    this.triggerMixEffects(worldIn, pos);
                    return true;
                }
                if (integer <= 4) {
                    selector.setBlockState(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, pos, pos, Blocks.COBBLESTONE.getDefaultState()), 3);
                    this.triggerMixEffects(worldIn, pos);
                    return true;
                }
            }
        }
        return false;
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return this.material == Material.WATER ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
    }


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double d0 = (double)pos.getX();
        double d1 = (double)pos.getY();
        double d2 = (double)pos.getZ();

        if (this.material == Material.WATER)
        {
            int i = ((Integer)stateIn.getValue(LEVEL)).intValue();

            if (i > 0 && i < 8)
            {
                if (rand.nextInt(64) == 0)
                {
                    worldIn.playSound(d0 + 0.5D, d1 + 0.5D, d2 + 0.5D, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, rand.nextFloat() * 0.25F + 0.75F, rand.nextFloat() + 0.5F, false);
                }
            }
            else if (rand.nextInt(10) == 0)
            {
                worldIn.spawnParticle(EnumParticleTypes.SUSPENDED, d0 + (double)rand.nextFloat(), d1 + (double)rand.nextFloat(), d2 + (double)rand.nextFloat(), 0.0D, 0.0D, 0.0D);
            }
        }

        if (this.material == Material.LAVA && worldIn.getBlockState(pos.up()).getMaterial() == Material.AIR && !worldIn.getBlockState(pos.up()).isOpaqueCube())
        {
            if (rand.nextInt(100) == 0)
            {
                double d8 = d0 + (double)rand.nextFloat();
                double d4 = d1 + stateIn.getBoundingBox(worldIn, pos).maxY;
                double d6 = d2 + (double)rand.nextFloat();
                worldIn.spawnParticle(EnumParticleTypes.LAVA, d8, d4, d6, 0.0D, 0.0D, 0.0D);
                worldIn.playSound(d8, d4, d6, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }

            if (rand.nextInt(200) == 0)
            {
                worldIn.playSound(d0, d1, d2, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }
        }

        if (rand.nextInt(10) == 0 && worldIn.getBlockState(pos.down()).isTopSolid())
        {
            Material material = worldIn.getBlockState(pos.down(2)).getMaterial();

            if (!material.blocksMovement() && !material.isLiquid())
            {
                double d3 = d0 + (double)rand.nextFloat();
                double d5 = d1 - 1.05D;
                double d7 = d2 + (double)rand.nextFloat();

                if (this.material == Material.WATER)
                {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d3, d5, d7, 0.0D, 0.0D, 0.0D);
                }
                else
                {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_LAVA, d3, d5, d7, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public static float getSlopeAngle(IBlockAccess worldIn, BlockPos pos, Material materialIn, IBlockState state) {
        Vec3d vec3d = getFlowingBlock(materialIn).getFlow(worldIn, pos, state);
        return vec3d.x == 0.0D && vec3d.z == 0.0D ? -1000.0F : (float)MathHelper.atan2(vec3d.z, vec3d.x) - ((float)Math.PI / 2F);
    }


    protected void triggerMixEffects(World worldIn, BlockPos pos) {
        double d0 = pos.getX();
        double d1 = pos.getY();
        double d2 = pos.getZ();
        worldIn.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

        for (int i = 0; i < 8; ++i) {
            worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d0 + Math.random(), d1 + 1.2D, d2 + Math.random(), 0.0D, 0.0D, 0.0D);
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


    public static BlockDynamicLiquid getFlowingBlock(Material materialIn) {
        if (materialIn == Material.WATER) {
            return Blocks.FLOWING_WATER;
        } else if (materialIn == Material.LAVA) {
            return Blocks.FLOWING_LAVA;
        } else {
            throw new IllegalArgumentException("Invalid material");
        }
    }


    public static BlockStaticLiquid getStaticBlock(Material materialIn) {
        if (materialIn == Material.WATER) {
            return Blocks.WATER;
        } else if (materialIn == Material.LAVA) {
            return Blocks.LAVA;
        } else {
            throw new IllegalArgumentException("Invalid material");
        }
    }


    public static float getBlockLiquidHeight(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int i = state.getValue(LEVEL);
        return (i & 7) == 0 && worldIn.getBlockState(pos.up()).getMaterial() == Material.WATER ? 1.0F : 1.0F - getLiquidHeightPercent(i);
    }


    public static float getLiquidHeight(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return (float)pos.getY() + getBlockLiquidHeight(state, worldIn, pos);
    }


    @Override
    public float getBlockLiquidHeight(World world, BlockPos pos, IBlockState state, Material material) {
        return BlockLiquid.getBlockLiquidHeight(state, world, pos);
    }

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