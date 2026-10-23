package net.minecraft.server.management;

import net.minecraft.util.math.*;
import net.minecraft.entity.player.*;
import net.minecraft.block.state.*;
import net.minecraftforge.common.*;
import net.minecraft.entity.*;
import net.minecraft.network.play.server.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.*;
import net.minecraft.util.*;
import net.minecraft.tileentity.*;
import net.minecraft.block.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.world.*;
import plus.fserver.server.elca.ChunkSelector;


public class PlayerInteractionManager {
    //public World world;
    public EntityPlayerMP player;
    private GameType gameType;
    private boolean isDestroyingBlock;
    private int initialDamage;
    private BlockPos destroyPos;
    private int curblockDamage;
    private boolean receivedFinishDiggingPacket;
    private BlockPos delayedDestroyPos;
    private int initialBlockDamage;
    private int durabilityRemainingOnBlock;

    public PlayerInteractionManager(final World worldIn) {
        this.gameType = GameType.NOT_SET;
        this.destroyPos = BlockPos.ORIGIN;
        this.delayedDestroyPos = BlockPos.ORIGIN;
        this.durabilityRemainingOnBlock = -1;
        //this.world = worldIn;
    }


    public void setGameType(final GameType type) {
        (this.gameType = type).configurePlayerCapabilities(this.player.capabilities);
        this.player.sendPlayerAbilities();
        this.player.server.getPlayerList().sendPacketToAllPlayersE(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_GAME_MODE, this.player));
        player.world.updateAllPlayersSleepingFlag();
    }


    public GameType getGameType() {
        return this.gameType;
    }


    public boolean survivalOrAdventure() {
        return this.gameType.isSurvivalOrAdventure();
    }


    public boolean isCreative() {
        return this.gameType.isCreative();
    }


    public void initializeGameType(final GameType type) {
        if (this.gameType == GameType.NOT_SET) {
            this.gameType = type;
        }
        this.setGameType(this.gameType);
    }


    public void updateBlockRemoving() {
        ChunkSelector selector = player.world.localSelector; //safe
        ++this.curblockDamage;
        if (this.receivedFinishDiggingPacket) {
            final int i = this.curblockDamage - this.initialBlockDamage;
            final IBlockState iblockstate = selector.getBlockState(this.delayedDestroyPos);
            if (iblockstate.getBlock().isAir(iblockstate, selector, this.delayedDestroyPos)) {
                this.receivedFinishDiggingPacket = false;
            }
            else {
                final float f = iblockstate.getPlayerRelativeBlockHardness(this.player, this.player.world, this.delayedDestroyPos) * (i + 1);
                final int progress = (int)(f * 10.0f);
                if (progress != this.durabilityRemainingOnBlock) {
                    player.sendBlockBreakProgressToTracking(this.delayedDestroyPos, progress);
                    //this.world.sendBlockBreakProgress(this.player.getEntityId(), this.delayedDestroyPos, progress);
                    this.durabilityRemainingOnBlock = progress;
                }
                if (f >= 1.0f) {
                    this.receivedFinishDiggingPacket = false;
                    this.tryHarvestBlock(this.delayedDestroyPos);
                }
            }
        }
        else if (this.isDestroyingBlock) {
            final IBlockState iblockstate2 = selector.getBlockState(this.destroyPos);
            if (iblockstate2.getBlock().isAir(iblockstate2, selector, this.destroyPos)) {
                player.sendBlockBreakProgressToTracking(this.destroyPos, -1);
                this.durabilityRemainingOnBlock = -1;
                this.isDestroyingBlock = false;
            }
            else {
                final int k = this.curblockDamage - this.initialDamage;
                final float f2 = iblockstate2.getPlayerRelativeBlockHardness(this.player, this.player.world, this.destroyPos) * (k + 1);
                final int progress = (int)(f2 * 10.0f);
                if (progress != this.durabilityRemainingOnBlock) {
                    player.sendBlockBreakProgressToTracking(this.destroyPos, progress);
                    this.durabilityRemainingOnBlock = progress;
                }
            }
        }
    }


    public void onBlockClicked(final BlockPos pos, final EnumFacing side) {
        EntityPlayerMP player = this.player;
        final double reachDist = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        final PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, pos, side, ForgeHooks.rayTraceEyeHitVec(this.player, reachDist + 1.0));
        if (event.isCanceled()) {
            ChunkSelector selector = player.world.localSelector; //safe
            player.connection.netManager.sendPacketE(new SPacketBlockChange(selector, pos));
            IBlockState state = selector.getBlockState(pos);
            player.world.notifyBlockUpdate(pos, state, state, 3);
            return;
        }
        if (this.isCreative()) {
            if (!player.world.extinguishFire(null, pos, side)) {
                this.tryHarvestBlock(pos);
            }
        }
        else {
            World world = player.world;
            ChunkSelector selector = world.localSelector; //safe
            final IBlockState state = selector.getBlockState(pos);
            final Block block = state.getBlock();
            if (this.gameType.hasLimitedInteractions()) {
                if (this.gameType == GameType.SPECTATOR) {
                    return;
                }
                if (!player.isAllowEdit()) {
                    final ItemStack itemstack = player.getHeldItemMainhand();
                    if (itemstack.isEmpty() || !itemstack.canDestroy(block)) return;
                }
            }
            this.initialDamage = this.curblockDamage;
            float f = 1.0f;
            if (!state.getBlock().isAir(state, selector, pos)) {
                if (event.getUseBlock() != Event.Result.DENY) {
                    block.onBlockClicked(world, pos, this.player);
                    world.extinguishFire(null, pos, side);
                }
                else {
                    IBlockState state2 = selector.getBlockState(pos);
                    player.connection.netManager.sendPacketE(new SPacketBlockChange(state2, pos));
                    world.notifyBlockUpdate(pos, state2, state2, 3);
                }
                f = state.getPlayerRelativeBlockHardness(this.player, this.player.world, pos);
            }
            if (event.getUseItem() == Event.Result.DENY) {
                if (f >= 1.0f) {
                    IBlockState state2 = selector.getBlockState(pos);
                    player.connection.netManager.sendPacketE(new SPacketBlockChange(state2, pos));
                    world.notifyBlockUpdate(pos, state2, state2, 3);
                }
                return;
            }
            if (!state.getBlock().isAir(state, selector, pos) && f >= 1.0f) {
                this.tryHarvestBlock(pos);
            }
            else {
                this.isDestroyingBlock = true;
                this.destroyPos = pos;
                final int progress = (int)(f * 10.0f);
                player.sendBlockBreakProgressToTracking(pos, progress);
                this.durabilityRemainingOnBlock = progress;
            }
        }
    }

    public void blockRemoving(final BlockPos pos) {
        if (pos.equals(this.destroyPos)) {
            EntityPlayerMP player = this.player;
            ChunkSelector selector = player.world.localSelector; //safe
            final int i = this.curblockDamage - this.initialDamage;
            final IBlockState state = selector.getState(pos);
            if (!state.getBlock().isAir(state, selector, pos)) {
                final float f = state.getPlayerRelativeBlockHardness(player, player.world, pos) * (i + 1);
                if (f >= 0.7f) {
                    this.isDestroyingBlock = false;
                    player.sendBlockBreakProgressToTracking(pos, -1);
                    this.tryHarvestBlock(pos);
                }
                else if (!this.receivedFinishDiggingPacket) {
                    this.isDestroyingBlock = false;
                    this.receivedFinishDiggingPacket = true;
                    this.delayedDestroyPos = pos;
                    this.initialBlockDamage = this.initialDamage;
                }
            }
        }
    }


    public void cancelDestroyingBlock() {
        this.isDestroyingBlock = false;
        player.sendBlockBreakProgressToTracking(this.destroyPos, -1);
    }


    private boolean removeBlock(final BlockPos pos) {
        return this.removeBlock(pos, false);
    }


    private boolean removeBlock(final BlockPos pos, final boolean b) {
        EntityPlayerMP player = this.player;
        World world = player.world;
        final IBlockState iblockstate = world.localSelector.getState(pos); //safe
        final boolean flag = iblockstate.getBlock().removedByPlayer(iblockstate, world, pos, player, b);
        if (flag) {
            iblockstate.getBlock().onPlayerDestroy(world, pos, iblockstate);
        }
        return flag;
    }


    public boolean tryHarvestBlock(final BlockPos pos) {
        EntityPlayerMP player = this.player;
        World world = player.world;
        final int exp = ForgeHooks.onBlockBreakEvent(world, this.gameType, player, pos);
        if (exp == -1) {
            return false;
        }
        final IBlockState state = world.localSelector.getState(pos); //safe
        final TileEntity tileentity = world.getTileEntity(pos);
        final Block block = state.getBlock();
        if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !player.canUseCommandBlock()) {
            world.notifyBlockUpdate(pos, state, state, 3);
            return false;
        }
        final ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem().onBlockStartBreak(stack, pos, player)) {
            return false;
        }
        world.playEvent(player, 2001, pos, Block.getStateId(state));
        boolean flag1;
        if (this.isCreative()) {
            flag1 = this.removeBlock(pos);
            player.connection.netManager.sendPacketE(new SPacketBlockChange(world.localSelector, pos));//safe
        }
        else {
            final ItemStack itemstack1 = player.getHeldItemMainhand();
            final ItemStack itemstack2 = itemstack1.isEmpty() ? ItemStack.EMPTY : itemstack1.copy();
            final boolean flag2 = state.getBlock().canHarvestBlock(world, pos, player);
            if (!itemstack1.isEmpty()) {
                itemstack1.onBlockDestroyed(world, state, pos, player);
                if (itemstack1.isEmpty()) {
                    ForgeEventFactory.onPlayerDestroyItem(player, itemstack2, EnumHand.MAIN_HAND);
                }
            }
            flag1 = this.removeBlock(pos, flag2);
            if (flag1 && flag2) {
                state.getBlock().harvestBlock(world, player, pos, state, tileentity, itemstack2);
            }
        }
        if (!this.isCreative() && flag1 && exp > 0) {
            state.getBlock().dropXpOnBlockBreak(world, pos, exp);
        }
        return flag1;
    }


    public EnumActionResult processRightClick(final EntityPlayer player, final World worldIn, final ItemStack stack, final EnumHand hand) {
        if (this.gameType == GameType.SPECTATOR) {
            return EnumActionResult.PASS;
        }
        if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return EnumActionResult.PASS;
        }
        final EnumActionResult cancelResult = ForgeHooks.onItemRightClick(player, hand);
        if (cancelResult != null) {
            return cancelResult;
        }
        final int i = stack.getCount();
        final int j = stack.getMetadata();
        final ItemStack copyBeforeUse = stack.copy();
        final ActionResult<ItemStack> actionresult = stack.useItemRightClick(worldIn, player, hand);
        final ItemStack itemstack = actionresult.getResult();
        if (itemstack == stack && itemstack.getCount() == i && itemstack.getMaxItemUseDuration() <= 0 && itemstack.getMetadata() == j) {
            return actionresult.getType();
        }
        if (actionresult.getType() == EnumActionResult.FAIL && itemstack.getMaxItemUseDuration() > 0 && !player.isHandActive()) {
            return actionresult.getType();
        }
        player.setHeldItem(hand, itemstack);
        if (this.isCreative()) {
            itemstack.setCount(i);
            if (itemstack.isItemStackDamageable()) {
                itemstack.setItemDamage(j);
            }
        }
        if (itemstack.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, hand);
            player.setHeldItem(hand, ItemStack.EMPTY);
        }
        if (!player.isHandActive()) {
            ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
        }
        return actionresult.getType();
    }


    public EnumActionResult processRightClickBlock(final EntityPlayer player, final World worldIn, final ItemStack stack, final EnumHand hand, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (this.gameType == GameType.SPECTATOR) {
            final TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof ILockableContainer) {
                final Block block1 = worldIn.getBlockState(pos).getBlock();
                ILockableContainer container = (ILockableContainer)tileentity;
                if (container instanceof TileEntityChest && block1 instanceof BlockChest) {
                    container = ((BlockChest)block1).getLockableContainer(worldIn, pos);
                }
                if (container != null) {
                    player.displayGUIChest(container);
                    return EnumActionResult.SUCCESS;
                }
            }
            else if (tileentity instanceof IInventory) {
                player.displayGUIChest((IInventory)tileentity);
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.PASS;
        }
        final double reachDist = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        final PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, pos, facing, ForgeHooks.rayTraceEyeHitVec((EntityLivingBase)player, reachDist + 1.0));
        if (event.isCanceled()) {
            return event.getCancellationResult();
        }
        EnumActionResult result = EnumActionResult.PASS;
        if (event.getUseItem() != Event.Result.DENY) {
            result = stack.onItemUseFirst(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            if (result != EnumActionResult.PASS) {
                return result;
            }
        }
        final boolean bypass = player.getHeldItemMainhand().doesSneakBypassUse((IBlockAccess)worldIn, pos, player) && player.getHeldItemOffhand().doesSneakBypassUse((IBlockAccess)worldIn, pos, player);
        if (!player.isSneaking() || bypass || event.getUseBlock() == Event.Result.ALLOW) {
            final IBlockState iblockstate = worldIn.getBlockState(pos);
            if (event.getUseBlock() != Event.Result.DENY && iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, facing, hitX, hitY, hitZ)) {
                result = EnumActionResult.SUCCESS;
            }
        }
        if (stack.isEmpty()) {
            return EnumActionResult.PASS;
        }
        if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return EnumActionResult.PASS;
        }
        if (stack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
            final Block block2 = ((ItemBlock)stack.getItem()).getBlock();
            if (block2 instanceof BlockCommandBlock || block2 instanceof BlockStructure) {
                return EnumActionResult.FAIL;
            }
        }
        if (!this.isCreative()) {
            if ((result != EnumActionResult.SUCCESS && event.getUseItem() != Event.Result.DENY) || (result == EnumActionResult.SUCCESS && event.getUseItem() == Event.Result.ALLOW)) {
                final ItemStack copyBeforeUse = stack.copy();
                result = stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                if (stack.isEmpty()) {
                    ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, hand);
                }
            }
            return result;
        }
        final int j = stack.getMetadata();
        final int i = stack.getCount();
        if ((result != EnumActionResult.SUCCESS && event.getUseItem() != Event.Result.DENY) || (result == EnumActionResult.SUCCESS && event.getUseItem() == Event.Result.ALLOW)) {
            final EnumActionResult enumactionresult = stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            stack.setItemDamage(j);
            stack.setCount(i);
            return enumactionresult;
        }
        return result;
    }


    public void setWorld(final WorldServer serverWorld) {
        //this.world = serverWorld;
    }


    @Deprecated
    public double getBlockReachDistance() {
        return this.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
    }


    @Deprecated
    public void setBlockReachDistance(final double distance) {
        this.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).setBaseValue(distance);
    }
}