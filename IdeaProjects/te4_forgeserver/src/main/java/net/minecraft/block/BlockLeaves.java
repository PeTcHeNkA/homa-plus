package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.utls.ChunkSelector;
import plus.fserver.utls.collections.LeavesCache;


public abstract class BlockLeaves extends BlockFs implements net.minecraftforge.common.IShearable {
    public static final PropertyBool DECAYABLE = PropertyBool.create("decayable");
    public static final PropertyBool CHECK_DECAY = PropertyBool.create("check_decay");
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
        int k = pos.getX();
        int l = pos.getY();
        int i1 = pos.getZ();

        if (worldIn.isAreaLoaded(new BlockPos(k - 2, l - 2, i1 - 2), new BlockPos(k + 2, l + 2, i1 + 2))) {
            for (int j1 = -1; j1 <= 1; ++j1) {
                for (int k1 = -1; k1 <= 1; ++k1) {
                    for (int l1 = -1; l1 <= 1; ++l1) {
                        BlockPos blockpos = pos.add(j1, k1, l1);
                        IBlockState iblockstate = worldIn.getBlockState(blockpos);

                        if (iblockstate.getBlock().isLeaves(iblockstate, worldIn, blockpos)) {
                            iblockstate.getBlock().beginLeavesDecay(iblockstate, worldIn, blockpos);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {
            if (state.getValue(CHECK_DECAY) && state.getValue(DECAYABLE)) {
                // Forge: prevent decaying leaves from updating neighbors and loading unloaded chunks
                //if (!world.isAreaLoaded(pos, 1)) return;
                // Forge: extend range from 5 to 6 to account for neighbor checks in world.markAndNotifyBlock -> world.updateObservingBlocksAt
                if (world.isAreaLoaded(pos, 6)) {
                    int num1 = pos.getX(), cY = pos.getY(), num2 = pos.getZ(), dx, dy, dz;

                    LeavesCache cache = world.getLeavesCache();
                    ChunkSelector access = ((WorldServer)world).localSelector;
                    Block block;
                    for (dx = -4; dx <= 4; ++dx) {
                        for (dz = -4; dz <= 4; ++dz) {
                            for (dy = -4; dy <= 4; ++dy) {
                                IBlockState iblockstate = access.getState(cache.setPos(num1 + dx, cY + dy, num2 + dz));

                                if (!(block = iblockstate.getBlock()).canSustainLeaves(iblockstate, world, cache)) {
                                    if (block.isLeaves(iblockstate, world, cache))
                                         cache.set(dx, dy, dz, -2);
                                    else cache.set(dx, dy, dz, -1);
                                } else   cache.set(dx, dy, dz, 0);
                            }
                        }
                    }

                    for (num2 = 1; num2 <= 4; ++num2) {
                        for (dx = -4; dx <= 4; ++dx) {
                            for (dy = -4; dy <= 4; ++dy) {
                                for (dz = -4; dz <= 4; ++dz) {
                                    if (cache.get(dx, dy, dz) == num2 - 1) {
                                        if(dx != -4) {
                                            num1 = LeavesCache.cursor(dx - 1, dy, dz);
                                            if (cache.get(num1) == -2)
                                                cache.set(num1, num2);
                                        }
                                        if(dx != 4) {
                                            num1 = LeavesCache.cursor(dx + 1, dy, dz);
                                            if (cache.get(num1) == -2)
                                                cache.set(num1, num2);
                                        }
                                        if(dy != -4) {
                                            num1 = LeavesCache.cursor(dx, dy - 1, dz);
                                            if (cache.get(num1) == -2)
                                                cache.set(num1, num2);
                                        }
                                        if(dy != 4) {
                                            num1 = LeavesCache.cursor(dx, dy + 1, dz);
                                            if (cache.get(num1) == -2)
                                                cache.set(num1, num2);
                                        }
                                        if(dz != -4) {
                                            num1 = LeavesCache.cursor(dx, dy, dz - 1);
                                            if (cache.get(num1) == -2)
                                                cache.set(num1, num2);
                                        }
                                        if(dz != 4) {
                                            num1 = LeavesCache.cursor(dx, dy, dz + 1);
                                            if (cache.get(num1) == -2)
                                                cache.set(num1, num2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //int center = cache.get(0,0,0);

                    if (cache.get(0,0,0) >= 0) world.setBlockState(pos, state.withProperty(CHECK_DECAY, Boolean.FALSE), 4);
                    else this.destroy(world, access, pos);
                }
            }
        }
    }


    private void destroy(World worldIn, IBlockEAccess access, BlockPos pos) {
        this.dropBlockAsItem(worldIn, pos, access.getBlockState(pos), 0);
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


    public int quantityDropped(Random random) {
        return random.nextInt(20) == 0 ? 1 : 0;
    }


    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.SAPLING);
    }


    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }

    protected void dropApple(World worldIn, BlockPos pos, IBlockState state, int chance) {}


    protected int getSaplingDropChance(IBlockState state) {
        return 20;
    }


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


    public boolean causesSuffocation(IBlockState state) {
        return false;
    }


    public abstract BlockPlanks.EnumType getWoodType(int meta);


    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos){ return true; }


    @Override
    public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos){ return true; }


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