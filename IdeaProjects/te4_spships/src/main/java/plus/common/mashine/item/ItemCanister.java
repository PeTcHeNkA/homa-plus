package plus.common.mashine.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;


public class ItemCanister extends BasicItem{
    public static final ItemCanister EMPTY_CANISTER = new ItemCanister(Blocks.AIR,   "empty_canister");
    public static final ItemCanister WATER_CANISTER = new ItemCanister(Blocks.WATER, "water_canister");
    public static final ItemCanister LAVA_CANISTER  = new ItemCanister(Blocks.LAVA,  "lava_canister" );

    private final Block containedBlock;

    protected ItemCanister(Block block, String name) {
        super(name);
        setMaxStackSize(1);
        setMaxDamage(17);
        setTranslationKey("canister."+block.getRegistryName().getPath());
        this.containedBlock = block;
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        //boolean flag = itemstack.getItemDamage() >= 16;
        RayTraceResult raytraceresult = this.rayTrace(world, player,
                true
        );
        ActionResult<ItemStack> ret = ForgeEventFactory.onBucketUse(player, world, itemstack, raytraceresult);
        if (ret != null) {
            return ret;
        } else if (raytraceresult == null) {
            return new ActionResult(EnumActionResult.PASS, itemstack);
        } else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult(EnumActionResult.PASS, itemstack);
        } else {
            BlockPos blockpos = raytraceresult.getBlockPos();
            if (!world.isBlockModifiable(player, blockpos)) {
                return new ActionResult(EnumActionResult.FAIL, itemstack);
            } else {
                IBlockState iblockstate = world.getBlockState(blockpos);
                Material material = iblockstate.getMaterial();

                if (material.isLiquid() && !player.isSneaking() && (iblockstate.getBlock() == this.containedBlock || this.containedBlock == Blocks.AIR)) {
                    if (itemstack.getItemDamage() <= 1 || !player.canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack)) {
                        return new ActionResult(EnumActionResult.FAIL, itemstack);
                    } else {

                        if (material == Material.WATER && iblockstate.getValue(BlockLiquid.LEVEL) == 0) {
                            world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                            player.addStat(StatList.getObjectUseStats(this));
                            player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                            return new ActionResult(EnumActionResult.SUCCESS, this.fillBucket(itemstack, player, WATER_CANISTER));
                        } else if (material == Material.LAVA && iblockstate.getValue(BlockLiquid.LEVEL) == 0) {
                            player.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                            world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                            player.addStat(StatList.getObjectUseStats(this));
                            return new ActionResult(EnumActionResult.SUCCESS,
                                    this.fillBucket(itemstack, player, LAVA_CANISTER));
                        } else {
                            return new ActionResult(EnumActionResult.FAIL, itemstack);
                        }
                    }
                } else {
                    boolean flag1 = iblockstate.getBlock().isReplaceable(world, blockpos);
                    BlockPos blockpos1 = flag1 && raytraceresult.sideHit == EnumFacing.UP ? blockpos : blockpos.offset(raytraceresult.sideHit);
                    if (!player.canPlayerEdit(blockpos1, raytraceresult.sideHit, itemstack)) {
                        return new ActionResult(EnumActionResult.FAIL, itemstack);
                    } else if (this.tryPlaceContainedLiquid(player, world, blockpos1)) {
                        if (player instanceof EntityPlayerMP) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, blockpos1, itemstack);
                        }

                        player.addStat(StatList.getObjectUseStats(this));
                        return new ActionResult(EnumActionResult.SUCCESS, freeBucket(itemstack, player));
                    } else {
                        return new ActionResult(EnumActionResult.FAIL, itemstack);
                    }
                }
            }
        }
    }


    private ItemStack fillBucket(ItemStack stack, EntityPlayer player, Item item) {
        int dmg = stack.getItemDamage();
        if(dmg >= 16) {
            ItemStack result = new ItemStack(item);
            result.setItemDamage(13);
            return result;
        }

        stack.setItemDamage(dmg-4);
        return stack;
    }


    private ItemStack freeBucket(ItemStack stack, EntityPlayer player) {
        int dmg = stack.getItemDamage();
        if(dmg >= 13) {
            ItemStack result = new ItemStack(EMPTY_CANISTER);
            result.setItemDamage(17);
            return result;
        }
        if(dmg == 0)dmg = 1;
        stack.setItemDamage(dmg+4);
        return stack;
    }


    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World world, BlockPos pos) {
        if (this.containedBlock == Blocks.AIR) {
            return false;
        } else {
            IBlockState iblockstate = world.getBlockState(pos);
            Material material = iblockstate.getMaterial();
            boolean flag = !material.isSolid();
            boolean flag1 = iblockstate.getBlock().isReplaceable(world, pos);
            if (!world.isAirBlock(pos) && !flag && !flag1) {
                return false;
            } else {
                if (world.provider.doesWaterVaporize() && this.containedBlock == Blocks.FLOWING_WATER) {
                    int l = pos.getX();
                    int i = pos.getY();
                    int j = pos.getZ();
                    world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                    for(int k = 0; k < 8; ++k) {
                        world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)l + Math.random(), (double)i + Math.random(), (double)j + Math.random(), 0.0, 0.0, 0.0, new int[0]);
                    }
                } else {
                    if (!world.isRemote && (flag || flag1) && !material.isLiquid()) {
                        world.destroyBlock(pos, true);
                    }

                    SoundEvent soundevent = this.containedBlock == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
                    world.playSound(player, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);

                    IBlockState state;
                    world.setBlockState(pos, state = this.containedBlock.getDefaultState(), 11);
                    state.neighborChanged(world, pos, this.containedBlock, pos);
                }
                return true;
            }
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> lore, ITooltipFlag flag) {
        int dmg = 16 - (stack.getItemDamage() - 1);
        addLore(lore, "canister_lvl", dmg>>2);
    }


    public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> stacks) {
        if (this.isInCreativeTab(tabs)) {
            ItemStack stack = new ItemStack(this);
            if(this.containedBlock == Blocks.AIR) {
                stack.setItemDamage(17);
            } else {
                stack.setItemDamage(1);
            }
            stacks.add(stack);
        }
    }


    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        stack.setItemDamage(17);
    }
}
