package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.api.Keep;
import plus.fserver.block.BlockFx;
import plus.fserver.server.elca.ChunkLCache;
import plus.fserver.utls.collections.BlockPosQueue;


public abstract class BlockLeaves extends BlockFx implements net.minecraftforge.common.IShearable {
    @Keep
    public static final PropertyBool DECAYABLE = PropertyBool.create("decayable");
    @Keep
    public static final PropertyBool CHECK_DECAY = PropertyBool.create("check_decay");
    @Keep
    protected boolean leavesFancy;

    public BlockLeaves() {
        super(Material.LEAVES);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setHardness(0.2F);
        this.setLightOpacity(1);
        this.setSoundType(SoundType.PLANT);
    }


    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        int k = pos.getX(), l = pos.getY(), i1 = pos.getZ();

        IBlockEAccess access = worldIn.access();
        BlockPos.MutableBlockPos temp = null;
        if (worldIn.isAreaLoaded(k - 2, l - 2, i1 - 2, k + 2, l + 2, i1 + 2, true)) {
            int dx, dy, dz;
            for (dx = -1; dx <= 1; ++dx) {
                for (dz = -1; dz <= 1; ++dz) {
                    for (dy = -1; dy <= 1; ++dy) {
                        int fx = pos.getX()+dx, fy = pos.getY()+dy, fz = pos.getZ()+dz;
                        IBlockState state1 = access.getBlockState(fx, fy, fz);
                        if (state1.getMaterial() == Material.LEAVES) {
                            if(temp == null)temp = new BlockPos.MutableBlockPos(fx, fy, fz);
                            else temp.setPosB(fx, fy, fz);
                            state1.getBlock().beginLeavesDecay(state1, worldIn, temp);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand){
        if (!world.isRemote) {
            if (state.getValue(CHECK_DECAY) && state.getValue(DECAYABLE)) {
                if (world.isAreaLoaded(pos, 6)) {
                    ChunkLCache access = world.bigLocalCache;
                    access.init(world.localSelector.getChunk(pos.getX(), pos.getZ()));

                    BlockPosQueue queue = new BlockPosQueue(pos.getX(), pos.getY(), pos.getZ());

                    if (tryFindBlock(access, queue, pos.getX(), pos.getY(), pos.getZ(), 5)){
                        access.setBlockState(pos, state.withProperty(CHECK_DECAY, Boolean.FALSE), 4);

                        BlockPosQueue update = queue.next();
                        while (update != null){
                            access.setBlockState(update, state.withProperty(CHECK_DECAY, Boolean.FALSE), 4);
                            update = update.next();
                        }
                    }
                    else this.destroy(world, state, pos);
                }
            }
        }
    }


    private static boolean tryFindBlock(ChunkLCache selector, BlockPosQueue cache, int x, int y, int z, int deep){
        if(deep < 0)return false;
        BlockPosQueue next = cache.next();
        if(next != null && next.containsInDeep(x, y, z, Math.min(3, deep)))return false;
        final IBlockState state = selector.getBlockStateUnchecked(x, y, z);
        final Block block = state.getBlock();
        if(block.canSustainLeaves(state, selector, cache.setPos(x, y, z)))return true;
        if(block.isLeaves(state, selector, cache)){
            cache.addToQueue(cache.getX(), cache.getY(), cache.getZ());
            if(y > 1) {
                if(tryFindBlock(selector, cache, x, y-1, z, deep-1))return true;
            }
            if(y < 254) {
                if(tryFindBlock(selector, cache, x, y+1, z, deep-1))return true;
            }
            if(tryFindBlock(selector, cache, x-1, y, z, deep-1))return true;
            if(tryFindBlock(selector, cache, x, y, z-1, deep-1))return true;
            if(tryFindBlock(selector, cache, x+1, y, z, deep-1))return true;
            if(tryFindBlock(selector, cache, x, y, z+1, deep-1))return true;
        }
        return false;
    }


    private void destroy(World worldIn, IBlockState state, BlockPos pos) {
        this.dropBlockAsItem(worldIn, pos, state, 0);
        worldIn.setBlockToAir(pos);
    }


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (worldIn.isRainingAt(pos.up()) && !worldIn.getBlockState(pos.down()).isTopSolid() && rand.nextInt(15) == 1) {
            double d0 = (float)pos.getX() + rand.nextFloat();
            double d1 = (double)pos.getY() - 0.05D;
            double d2 = (float)pos.getZ() + rand.nextFloat();
            worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }


    @Override
    public int quantityDropped(Random random) {
        return random.nextInt(20) == 0 ? 1 : 0;
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.SAPLING);
    }


    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }


    protected void dropApple(World worldIn, BlockPos pos, IBlockState state, int chance) {}


    protected int getSaplingDropChance(IBlockState state) {
        return 20;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return !this.leavesFancy;
    }


    @SideOnly(Side.CLIENT)
    public void setGraphicsLevel(boolean fancy) {
        this.leavesFancy = fancy;
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return this.leavesFancy ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID;
    }


    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }


    public abstract BlockPlanks.EnumType getWoodType(int meta);


    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos){ return true; }
//
//
//    @Override
//    public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos){ return true; }


    @Override
    public void beginLeavesDecay(IBlockState state, World world, BlockPos pos) {
        if (!state.getValue(CHECK_DECAY)) {
            world.setBlockState(pos, state.withProperty(CHECK_DECAY, true), 4);
        }
    }


    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World)world).rand : new Random();
        int chance = this.getSaplingDropChance(state);

        if (fortune > 0) {
            chance -= 2 << fortune;
            if (chance < 10) chance = 10;
        }

        if (rand.nextInt(chance) == 0) {
            ItemStack drop = new ItemStack(getItemDropped(state, rand, fortune), 1, damageDropped(state));
            if (!drop.isEmpty())
                drops.add(drop);
        }

        chance = 200;
        if (fortune > 0) {
            chance -= 10 << fortune;
            if (chance < 40) chance = 40;
        }

        captureDropsTrue();
        if (world instanceof World) dropApple((World)world, pos, state, chance); // Dammet mojang
        drops.addAll(captureDrops(false));
    }


    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return !this.leavesFancy && blockAccess.getBlockState(pos.offset(side)).getBlock() == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
}