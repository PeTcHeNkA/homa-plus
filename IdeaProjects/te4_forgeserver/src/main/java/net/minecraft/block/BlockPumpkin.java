package net.minecraft.block;

import com.google.common.base.*;
import net.minecraft.block.material.*;
import net.minecraft.creativetab.*;
import net.minecraft.util.math.*;
import net.minecraft.init.*;
import net.minecraft.entity.player.*;
import net.minecraft.advancements.*;
import net.minecraft.entity.monster.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import net.minecraft.entity.*;
import net.minecraft.block.state.*;
import net.minecraft.block.state.pattern.*;
import plus.fserver.ElcaCV2;
import plus.fserver.utls.collections.ArrayListE6;


public class BlockPumpkin extends BlockHorizontal {
    private BlockPattern snowmanBasePattern;
    private BlockPattern snowmanPattern;
    private BlockPattern golemBasePattern;
    private BlockPattern golemPattern;
    private static final Predicate<IBlockState> IS_PUMPKIN;

    protected BlockPumpkin() {
        super(Material.GOURD, MapColor.ADOBE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockPumpkin.FACING, EnumFacing.NORTH));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list) {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }//Up to 40+ times faster
        AxisAlignedBB src;
        AxisAlignedBB blockBox = getCollisionBoundingBox(state, access, pos);
        if((src = list.getSafe(list.sizeC())) != null) blockBox.offsetTo(pos, src);
        else src = blockBox.offset(pos);
        if (entityBox.intersects(src)) list.addF(src);
    }


    @Override
    public void onBlockAdded(final World worldIn, final BlockPos pos, final IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        this.trySpawnGolem(worldIn, pos);
    }


    public boolean canDispenserPlace(final World worldIn, final BlockPos pos) {
        return this.getSnowmanBasePattern().match(worldIn, pos) != null || this.getGolemBasePattern().match(worldIn, pos) != null;
    }


    private void trySpawnGolem(final World world, final BlockPos pos) {
        BlockPattern patern = this.getSnowmanPattern();
        BlockPattern.PatternHelper helper = patern.match(world, pos);

        if (helper != null) {
            for (int i = 0; i < patern.getThumbLength(); ++i) {
                final BlockWorldState state = helper.translateOffset(0, i, 0);
                world.setBlockState(state.getPos(), Blocks.AIR.getDefaultState(), 2);
            }
            final EntitySnowman golem = new EntitySnowman(world);
            final BlockPos blockpos1 = helper.translateOffset(0, 2, 0).getPos();
            golem.setLocationAndAngles(blockpos1.getX() + 0.5, blockpos1.getY() + 0.05, blockpos1.getZ() + 0.5, 0.0f, 0.0f);
            world.spawnEntity(golem);

            for (final EntityPlayerMP entityplayermp : world.getEntitiesWithinAABB((Class<? extends EntityPlayerMP>)EntityPlayerMP.class, golem.getEntityBoundingBox().grow(5.0))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(entityplayermp, golem);
            }
            for (int l = 0; l < 120; ++l) {
                world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, blockpos1.getX() + world.rand.nextDouble(), blockpos1.getY() + world.rand.nextDouble() * 2.5, blockpos1.getZ() + world.rand.nextDouble(), 0.0, 0.0, 0.0, new int[0]);
            }
            for (int i2 = 0; i2 < patern.getThumbLength(); ++i2) {
                final BlockWorldState wState = helper.translateOffset(0, i2, 0);
                world.notifyNeighborsRespectDebug(wState.getPos(), Blocks.AIR, false);
            }
        }
        else {
            patern = this.getGolemPattern();
            helper = patern.match(world, pos);

            if (helper != null) {
                for (int j = 0; j < patern.getPalmLength(); ++j) {
                    for (int k = 0; k < patern.getThumbLength(); ++k) {
                        world.access().setBlockState(helper.translateOffset(j, k, 0).getPos(), Blocks.AIR.getDefaultState(), 2);
                    }
                }

                final BlockPos pos1 = helper.translateOffset(1, 2, 0).getPos();
                final EntityIronGolem entityirongolem = new EntityIronGolem(world);
                entityirongolem.setPlayerCreated(true);
                entityirongolem.setLocationAndAngles(pos1.getX() + 0.5, pos1.getY() + 0.05, pos1.getZ() + 0.5, 0.0f, 0.0f);
                world.spawnEntity(entityirongolem);
                for (final EntityPlayerMP entityplayermp2 : world.getEntitiesWithinAABB((Class<? extends EntityPlayerMP>)EntityPlayerMP.class, entityirongolem.getEntityBoundingBox().grow(5.0))) {
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(entityplayermp2, entityirongolem);
                }
                for (int j2 = 0; j2 < 120; ++j2) {
                    world.spawnParticle(EnumParticleTypes.SNOWBALL, pos1.getX() + world.rand.nextDouble(), pos1.getY() + world.rand.nextDouble() * 3.9, pos1.getZ() + world.rand.nextDouble(), 0.0, 0.0, 0.0, new int[0]);
                }
                for (int k2 = 0; k2 < patern.getPalmLength(); ++k2) {
                    for (int l2 = 0; l2 < patern.getThumbLength(); ++l2) {
                        final BlockWorldState state = helper.translateOffset(k2, l2, 0);
                        world.notifyNeighborsRespectDebug(state.getPos(), Blocks.AIR, false);
                    }
                }
            }
        }
    }


    @Override
    public boolean canPlaceBlockAt(final World worldIn, final BlockPos pos) {
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos) && worldIn.isSideSolid(pos.down(), EnumFacing.UP);
    }


    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot) {
        return state.withProperty(BlockPumpkin.FACING, rot.rotate(state.getValue(BlockPumpkin.FACING)));
    }


    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockPumpkin.FACING)));
    }


    @Override
    public IBlockState getStateForPlacement(final World worldIn, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        return this.getDefaultState().withProperty(BlockPumpkin.FACING, placer.getHorizontalFacing().getOpposite());
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockPumpkin.FACING, EnumFacing.byHorizontalIndex(meta));
    }


    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(BlockPumpkin.FACING).getHorizontalIndex();
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockPumpkin.FACING);
    }


    protected BlockPattern getSnowmanBasePattern() {
        if (this.snowmanBasePattern == null) {
            this.snowmanBasePattern = FactoryBlockPattern.start().aisle(" ", "#", "#").where('#', BlockWorldState.hasState((Predicate<IBlockState>)BlockStateMatcher.forBlock(Blocks.SNOW))).build();
        }
        return this.snowmanBasePattern;
    }


    protected BlockPattern getSnowmanPattern() {
        if (this.snowmanPattern == null) {
            this.snowmanPattern = FactoryBlockPattern.start().aisle("^", "#", "#").where('^', BlockWorldState.hasState(BlockPumpkin.IS_PUMPKIN)).where('#', BlockWorldState.hasState((Predicate<IBlockState>)BlockStateMatcher.forBlock(Blocks.SNOW))).build();
        }
        return this.snowmanPattern;
    }


    protected BlockPattern getGolemBasePattern() {
        if (this.golemBasePattern == null) {
            this.golemBasePattern = FactoryBlockPattern.start().aisle("~ ~", "###", "~#~").where('#', BlockWorldState.hasState((Predicate<IBlockState>)BlockStateMatcher.forBlock(Blocks.IRON_BLOCK))).where('~', BlockWorldState.hasState((Predicate<IBlockState>)BlockMaterialMatcher.forMaterial(Material.AIR))).build();
        }
        return this.golemBasePattern;
    }


    protected BlockPattern getGolemPattern() {
        if (this.golemPattern == null) {
            this.golemPattern = FactoryBlockPattern.start().aisle("~^~", "###", "~#~").where('^', BlockWorldState.hasState(BlockPumpkin.IS_PUMPKIN)).where('#', BlockWorldState.hasState((Predicate<IBlockState>)BlockStateMatcher.forBlock(Blocks.IRON_BLOCK))).where('~', BlockWorldState.hasState((Predicate<IBlockState>)BlockMaterialMatcher.forMaterial(Material.AIR))).build();
        }
        return this.golemPattern;
    }


    static {
        IS_PUMPKIN = state -> state != null && (state.getBlock() == Blocks.PUMPKIN || state.getBlock() == Blocks.LIT_PUMPKIN);
    }
}