package net.minecraft.network;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEnchantItem;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlaceRecipe;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketRecipeInfo;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketSeenAdvancements;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import plus.fserver.utls.collections.TransactMap;
import plus.fserver.utls.math.VecDFD;


public class NetHandlerPlayServer implements INetHandlerPlayServer, ITickable {
    private static final Logger LOGGER = LogManager.getLogger();
    public final NetworkManager netManager;
    private final MinecraftServer server;
    public EntityPlayerMP player;
    private int networkTickCount;
    private long field_194402_f;
    private boolean field_194403_g;
    private long field_194404_h;
    private short chatSpamThresholdCount;
    private short itemDropThreshold;
    //todo slim this
    private final TransactMap pendingTransactions = new TransactMap();
    private double firstGoodX;
    private double firstGoodY;
    private double firstGoodZ;
    private double lastGoodX;
    private double lastGoodY;
    private double lastGoodZ;
    private Entity lowestRiddenEnt;
    private double lowestRiddenX;
    private double lowestRiddenY;
    private double lowestRiddenZ;
    private double lowestRiddenX1;
    private double lowestRiddenY1;
    private double lowestRiddenZ1;
    private VecDFD targetPos;
    private int teleportId;
    private int lastPositionUpdate;
    private boolean floating;
    private int floatingTickCount;
    private boolean vehicleFloating;
    private int vehicleFloatingTickCount;
    private int movePacketCounter;
    private int lastMovePacketCounter;


    public NetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
        this.server = server;
        this.netManager = networkManagerIn;
        networkManagerIn.setNetHandler(this);
        this.player = playerIn;
        playerIn.connection = this;
    }


    private void captureCurrentPosition() {
        EntityPlayerMP player = this.player;
        this.firstGoodX = this.lastGoodX = player.posX;
        this.firstGoodY = this.lastGoodY = player.posY;
        this.firstGoodZ = this.lastGoodZ = player.posZ;
    }


    public NetworkManager getNetworkManager() {
        return this.netManager;
    }


    public void disconnect(final ITextComponent textComponent) {
        this.netManager.sendPacket(new SPacketDisconnect(textComponent), future -> NetHandlerPlayServer.this.netManager.closeChannel(textComponent));
        this.netManager.disableAutoRead();
        Futures.getUnchecked(this.server.addScheduledTask(NetHandlerPlayServer.this.netManager::handleDisconnection));
    }


    private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) {
        if (Doubles.isFinite(packetIn.getX(0.0D)) && Doubles.isFinite(packetIn.getY(0.0D)) && Doubles.isFinite(packetIn.getZ(0.0D)) && Floats.isFinite(packetIn.getPitch(0.0F)) && Floats.isFinite(packetIn.getYaw(0.0F))) {
            return Math.abs(packetIn.getX(0.0D)) > 3.0E7D || Math.abs(packetIn.getY(0.0D)) > 3.0E7D || Math.abs(packetIn.getZ(0.0D)) > 3.0E7D;
        } else {
            return true;
        }
    }


    private static boolean isMoveVehiclePacketInvalid(CPacketVehicleMove packetIn) {
        return !Doubles.isFinite(packetIn.getX()) || !Doubles.isFinite(packetIn.getY()) || !Doubles.isFinite(packetIn.getZ()) || !Floats.isFinite(packetIn.getPitch()) || !Floats.isFinite(packetIn.getYaw());
    }


    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
        this.setPlayerLocation(x, y, z, yaw, pitch, Collections.emptySet());
    }


    public void handleResourcePackStatus(CPacketResourcePackStatus packetIn) {}


    @Override
    public void onDisconnect(ITextComponent reason) {
        EntityPlayerMP player = this.player;
        LOGGER.info("{} lost connection: {}", player.getName(), reason.getUnformattedText());
        MinecraftServer server = this.server;
        server.refreshStatusNextTick();

        TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("multiplayer.player.left", player.getDisplayName());
        textcomponenttranslation.getStyle().setColor(TextFormatting.YELLOW);
        server.getPlayerList().sendMessage(textcomponenttranslation);
        player.mountEntityAndWakeUp();
        server.getPlayerList().playerLoggedOut(player);

        if (server.isSinglePlayer() && player.getName().equals(server.getServerOwner())) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            server.initiateShutdown();
        }
    }


    private void handleSlashCommand(String command) {
        this.server.getCommandManager().executeCommand(this.player, command);
    }


    @Override
    public void processKeepAlive(CPacketKeepAlive packetIn) {
        if (this.field_194403_g && packetIn.getKey() == this.field_194404_h) {
            int i = (int)(this.currentTimeMillis() - this.field_194402_f);
            this.player.ping = (this.player.ping * 3 + i) / 4;
            this.field_194403_g = false;
        } else if (!this.player.getName().equals(this.server.getServerOwner())) {
            this.disconnect(new TextComponentTranslation("disconnect.timeout"));
        }
    }


    private long currentTimeMillis() {
        return System.nanoTime() / 1000000L;
    }


    private boolean locked = false;


    private void addToQueue(Runnable runnable){
        player.getServerWorld().addSyncTask(runnable);
    }


    public boolean locked() {
        return locked;
    }


    public void setLock(boolean locked) {
        this.locked = locked;
    }


    /**
     * @author Te4hno
     */
    public void update() {}


    public void onTickWorld(){
        if(locked)return;

        final EntityPlayerMP player = this.player;

        this.firstGoodX = player.posX;
        this.firstGoodY = player.posY;
        this.firstGoodZ = player.posZ;
        this.lastGoodX  = player.posX;
        this.lastGoodY  = player.posY;
        this.lastGoodZ  = player.posZ;

        player.onUpdateEntity();
        player.setPositionAndRotation(this.firstGoodX, this.firstGoodY, this.firstGoodZ, player.rotationYaw, player.rotationPitch);
        ++this.networkTickCount;
        this.lastMovePacketCounter = this.movePacketCounter;
        if (this.floating) {
            if (++this.floatingTickCount > 80) {
                LOGGER.warn("{} was kicked for floating too long!", player.getName());
                this.disconnect(new TextComponentTranslation("multiplayer.disconnect.flying"));
                return;
            }
        } else {
            this.floatingTickCount = 0;
        }
        this.lowestRiddenEnt = player.getLowestRidingEntity();

        if (this.lowestRiddenEnt != player && this.lowestRiddenEnt.getControllingPassenger() == player) {
            this.lowestRiddenX = this.lowestRiddenEnt.posX;
            this.lowestRiddenY = this.lowestRiddenEnt.posY;
            this.lowestRiddenZ = this.lowestRiddenEnt.posZ;
            this.lowestRiddenX1 = this.lowestRiddenEnt.posX;
            this.lowestRiddenY1 = this.lowestRiddenEnt.posY;
            this.lowestRiddenZ1 = this.lowestRiddenEnt.posZ;

            if (this.vehicleFloating && player.getLowestRidingEntity().getControllingPassenger() == player) {
                if (++this.vehicleFloatingTickCount > 80) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", player.getName());
                    this.disconnect(new TextComponentTranslation("multiplayer.disconnect.flying"));
                    return;
                }
            } else {
                this.vehicleFloating = false;
                this.vehicleFloatingTickCount = 0;
            }
        } else {
            this.lowestRiddenEnt = null;
            this.vehicleFloating = false;
            this.vehicleFloatingTickCount = 0;
        }

        long i = this.currentTimeMillis();

        if (i - this.field_194402_f >= 15000L) {
            if (this.field_194403_g) {
                this.disconnect(new TextComponentTranslation("disconnect.timeout"));
            } else {
                this.field_194403_g = true;
                this.field_194402_f = i;
                this.field_194404_h = i;
                netManager.sendPacketE(new SPacketKeepAlive(this.field_194404_h));
            }
        }

        if (this.chatSpamThresholdCount > 0) --this.chatSpamThresholdCount;
        if (this.itemDropThreshold > 0) --this.itemDropThreshold;

        MinecraftServer server;
        if (player.getLastActiveTime() > 0L && (server = this.server).getMaxPlayerIdleMinutes() > 0 && MinecraftServer.getCurrentTimeMillis() - player.getLastActiveTime() > server.getMaxPlayerIdleMinutes() * 60000L) {
            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.idling"));
        }
    }

    /**
     * @author Te4hno
     */
    //async
    public void processInput(CPacketInput packetIn) {
        this.player.setEntityActionState(
                packetIn.getStrafeSpeed(),
                packetIn.getForwardSpeed(),
                packetIn.isJumping(),
                packetIn.isSneaking()
        );
    }


    @Override
    public void processPlayer(@NotNull CPacketPlayer packetIn) {
        if (isMovePlayerPacketInvalid(packetIn)) {
            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_player_movement"));
        } else {
            if (networkTickCount == 0) {
                ++networkTickCount;
                this.captureCurrentPosition();
            }
            if (!this.player.queuedEndExit)
                addToQueue(() -> processPlayerSync(packetIn));
        }
    }


    private void processPlayerSync(CPacketPlayer packetIn){
        if (this.targetPos != null) {
            if (this.networkTickCount - this.lastPositionUpdate > 20) {
                this.lastPositionUpdate = this.networkTickCount;
                this.setPlayerLocation(this.targetPos.x, this.targetPos.y, this.targetPos.z, this.player.rotationYaw, this.player.rotationPitch);
            }
        } else {
            final EntityPlayerMP player;
            WorldServer worldserver = (player = this.player).getServerWorld();

            this.lastPositionUpdate = this.networkTickCount;

            if (player.isRiding()) {
                player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetIn.getYaw(player.rotationYaw), packetIn.getPitch(player.rotationPitch));

                worldserver.getPlayerChunkMap().updateMovingPlayer(player);
            } else {
                double d4 = packetIn.getX(player.posX);
                double d5 = packetIn.getY(player.posY);
                double d6 = packetIn.getZ(player.posZ);
                float yaw = packetIn.getYaw(player.rotationYaw);
                float pitch = packetIn.getPitch(player.rotationPitch);
                double d7 = d4 - this.firstGoodX;
                double d8 = d5 - this.firstGoodY;
                double d9 = d6 - this.firstGoodZ;
                double d10 = player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ;
                double d11 = d7 * d7 + d8 * d8 + d9 * d9;

                if (player.isPlayerSleeping()) {
                    if (d11 > 1.0D) {
                        this.setPlayerLocation(player.posX, player.posY, player.posZ, packetIn.getYaw(player.rotationYaw), packetIn.getPitch(player.rotationPitch));
                    }
                } else {
                    ++this.movePacketCounter;
                    int i = this.movePacketCounter - this.lastMovePacketCounter;

                    if (i > 5) {
                        LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", player.getName(), i);
                        i = 1;
                    }

                    if (!player.isInvulnerableDimensionChange()) {
                        if (!player.isCreative() && d11 - d10 > ((player.isElytraFlying() ? 300.0F : 100.0F) * i)) {
                            LOGGER.warn("{} moved too quickly! {},{},{}", player.getName(), d7, d8, d9);
                            this.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
                            return;
                        }
                    }
                    boolean flag2 = worldserver.getCollisionBoxes(player, player.getEntityBoundingBox().shrink(0.0625D)).isEmpty();

                    double d0 = player.posX,
                           d1 = player.posY,
                           d2 = player.posZ;

                    d7 = d4 - this.lastGoodX;
                    d8 = d5 - this.lastGoodY;
                    d9 = d6 - this.lastGoodZ;

                    if (player.onGround && !packetIn.isOnGround() && d8 > 0.0D) {
                        player.jump();
                    }

                    player.moveNow(MoverType.PLAYER, d7, d8, d9);
                    player.onGround = packetIn.isOnGround();
                    double d12 = d8;
                    d7 = d4 - player.posX;
                    d8 = d5 - player.posY;

                    if (d8 > -0.5D || d8 < 0.5D) {
                        d8 = 0.0D;
                    }

                    d9 = d6 - player.posZ;
                    d11 = d7 * d7 + d8 * d8 + d9 * d9;
                    boolean flag = false;

                    //todo is safe? 0.0625D -> 0.625D
                    if (!player.isInvulnerableDimensionChange() && d11 > 0.625D && !player.isPlayerSleeping() && !player.interactionManager.isCreative() && player.interactionManager.getGameType() != GameType.SPECTATOR) {
                        flag = true;
                        LOGGER.warn("{} moved wrongly! [{}]", player.getName(), d11-0.0625D);
                    }

                    player.setPositionAndRotation(d4, d5, d6, yaw, pitch);
                    player.addMovementStat(player.posX - d0, player.posY - d1, player.posZ - d2);

                    if (!player.noClip && !player.isPlayerSleeping() && flag2) {
                        if (flag || !worldserver.getCollisionBoxes(player, player.getEntityBoundingBox().shrink(0.0625D)).isEmpty()) {
                            this.setPlayerLocation(d0, d1, d2, yaw, pitch);
                            return;
                        }
                    }
                    boolean floating;
                    floating = this.floating = d12 >= -0.03125D;
                    if(floating) {
                        floating = this.floating = !this.server.isFlightAllowed() && !player.capabilities.allowFlying;
                        if(floating)
                            this.floating = !player.isPotionActive(MobEffects.LEVITATION) && !player.isElytraFlying()
                                && !worldserver.checkBlockCollision( //todo is safe 'y' fix? -0.4875D -> 0.0625
                                player.getEntityBoundingBox().grow(0.0625D, 0.0625, 0.0625D)
                        );
                    }
                    player.onGround = packetIn.isOnGround();
                    player.getServerWorld().getPlayerChunkMap().updateMovingPlayer(player);
                    player.handleFalling(player.posY - d1, packetIn.isOnGround());
                    this.lastGoodX = player.posX;
                    this.lastGoodY = player.posY;
                    this.lastGoodZ = player.posZ;
                }
            }
        }
    }


    @Override
    public void processPlayerDigging(@NotNull CPacketPlayerDigging packetIn) {
        player.markPlayerActive();
        switch (packetIn.getAction()) {
            case SWAP_HELD_ITEMS:
                if (!this.player.isSpectator()) {
                    addToQueue(() -> {
                        EntityPlayerMP lPlayer;
                        ItemStack itemstack = (lPlayer = player).getHeldItem(EnumHand.OFF_HAND);
                        lPlayer.setHeldItem(EnumHand.OFF_HAND, lPlayer.getHeldItem(EnumHand.MAIN_HAND));
                        lPlayer.setHeldItem(EnumHand.MAIN_HAND, itemstack);
                    });
                }
                return;
            case DROP_ITEM:
                if (!this.player.isSpectator()) {
                    addToQueue(() -> player.dropItem(false));
                }
                return;
            case DROP_ALL_ITEMS:
                if (!this.player.isSpectator()) {
                    addToQueue(() -> player.dropItem(true));
                }
                return;
            case RELEASE_USE_ITEM:
                addToQueue(() -> player.stopActiveHand());
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                addToQueue(() -> processPlayerDigging0(packetIn));
                return;
        }
    }


    private void processPlayerDigging0(CPacketPlayerDigging packetIn) {
        BlockPos pos = packetIn.getPosition();
        if (pos.getY() >= this.server.getBuildLimit()) return;
        final EntityPlayerMP player;
        double d0 = (player = this.player).posX - ((double) pos.getX() + 0.5D);
        double d1 = player.posY - ((double) pos.getY() + 0.5D) + 1.5D;
        double d2 = player.posZ - ((double) pos.getZ() + 0.5D);

        d0 = d0 * d0 + d1 * d1 + d2 * d2;

        d1 = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1;

        if (d0 < d1 * d1) {
            WorldServer world = player.getServerWorld();
            if (packetIn.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                if (world.isInWorldBorder(pos.getX(), pos.getZ()))
                    player.interactionManager.onBlockClicked(pos, packetIn.getFacing());
                else
                    player.connection.netManager.sendPacketE(new SPacketBlockChange(world, pos));
            } else {
                if (packetIn.getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)
                    player.interactionManager.blockRemoving(pos);
                else
                    //if(packetIn.getAction() == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK)
                    player.interactionManager.cancelDestroyingBlock();
                if (world.localSelector.getBlockState(pos).getMaterial() != Material.AIR) //safe
                    player.connection.netManager.sendPacketE(new SPacketBlockChange(world, pos));
            }
        }
    }


    @Override
    public void processVehicleMove(@NotNull CPacketVehicleMove packetIn) {
        if (isMoveVehiclePacketInvalid(packetIn)) {
            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_vehicle_movement"));
        } else {
            addToQueue(() -> processVehicleMove0(packetIn));
        }
    }


    private void processVehicleMove0(CPacketVehicleMove packetIn){
        final EntityPlayerMP player;
        Entity entity = (player = this.player).getLowestRidingEntity();

        if (entity != player && entity.getControllingPassenger() == player && entity == this.lowestRiddenEnt) {
            double d3 = packetIn.getX();
            double d4 = packetIn.getY();
            double d5 = packetIn.getZ();

            double d6 = d3 - this.lowestRiddenX;
            double d7 = d4 - this.lowestRiddenY;
            double d8 = d5 - this.lowestRiddenZ;
            double d9 = entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ;
            double d10 = d6 * d6 + d7 * d7 + d8 * d8;

            if (d10 - d9 > 100.0D && (!this.server.isSinglePlayer() || !this.server.getServerOwner().equals(entity.getName()))) {
                LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName(), player.getName(), d6, d7, d8);
                netManager.sendPacketE(new SPacketMoveVehicle(entity));
                return;
            }

            double d0 = entity.posX,
                    d1 = entity.posY,
                    d2 = entity.posZ;

            float f = packetIn.getYaw(),
                    f1 = packetIn.getPitch();

            WorldServer worldserver = player.getServerWorld();

            boolean flag = worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().shrink(0.0625D)).isEmpty();
            d6 = d3 - this.lowestRiddenX1;
            d7 = d4 - this.lowestRiddenY1 - 1.0E-6D;
            d8 = d5 - this.lowestRiddenZ1;
            entity.move(MoverType.PLAYER, d6, d7, d8);
            double d11 = d7;

            entity.setPositionAndRotation(d3, d4, d5, f, f1);
            player.setPositionAndRotation(d3, d4, d5, player.rotationYaw, player.rotationPitch);

            if (flag) {
                d6 = d3 - entity.posX;
                d7 = d4 - entity.posY;
                if (d7 > -0.5D || d7 < 0.5D) d7 = 0.0D;
                d8 = d5 - entity.posZ;
                if(flag = d6 * d6 + d7 * d7 + d8 * d8 < 0.0625D)
                    flag = !worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().shrink(0.0625D)).isEmpty();
                if(flag) {
                    entity.setPositionAndRotation(d0, d1, d2, f, f1);
                    player.setPositionAndRotation(d0, d1, d2, player.rotationYaw, player.rotationPitch);
                    netManager.sendPacketE(new SPacketMoveVehicle(entity));
                    return;
                }
            }
            worldserver.getPlayerChunkMap().updateMovingPlayer(player);
            //updateMoving();
            player.addMovementStat(player.posX - d0, player.posY - d1, player.posZ - d2);
            this.vehicleFloating = d11 >= -0.03125D && !this.server.isFlightAllowed() && !worldserver.checkBlockCollision(entity.getEntityBoundingBox().grow(0.0625D).expand(0.0D, -0.55D, 0.0D));
            this.lowestRiddenX1 = entity.posX;
            this.lowestRiddenY1 = entity.posY;
            this.lowestRiddenZ1 = entity.posZ;
        }
    }


    @Override
    public void processConfirmTeleport(CPacketConfirmTeleport packetIn) {
        if (packetIn.getTeleportId() == this.teleportId) {
            Runnable task = new Runnable() {
                private boolean flag = true;
                @Override
                public void run() {
                    VecDFD pos = targetPos;
                    if(pos == null){
                        if(flag){
                            flag = false;
                            addToQueue(this);
                        }
                        return;
                    }
                    player.setPositionAndRotation(
                            pos.x, pos.y, pos.z,
                            player.rotationYaw, player.rotationPitch
                    );
                    if (player.isInvulnerableDimensionChange()) {
                        lastGoodX = pos.x;
                        lastGoodY = pos.y;
                        lastGoodZ = pos.z;
                        player.clearInvulnerableDimensionChange();
                    }
                    targetPos = null;
                }
            };
            addToQueue(task);
        }
    }


    /**
     * @author Te4hno
     */
    //async
    public void handleRecipeBookUpdate(@NotNull CPacketRecipeInfo info) {
        final RecipeBookServer book = player.getRecipeBook();
        if (info.getPurpose() == CPacketRecipeInfo.Purpose.SHOWN) {
            addToQueue(() -> book.markSeen(info.getRecipe()));
        } else if (info.getPurpose() == CPacketRecipeInfo.Purpose.SETTINGS) {
            book.setGuiOpen(info.isGuiOpen());
            book.setFilteringCraftable(info.isFilteringCraftable());
        }
    }


    /**
     * @author Te4hno
     */
    //async
    public void handleSeenAdvancements(CPacketSeenAdvancements packet) {
        if (packet.getAction() == CPacketSeenAdvancements.Action.OPENED_TAB) {
            ResourceLocation resourcelocation = packet.getTab();
            Advancement advancement = this.server.getAdvancementManager().getAdvancement(resourcelocation);

            if (advancement != null)
                this.player.getAdvancements().setSelectedTab(advancement);
        }
    }


    /**
     * @author Te4hno
     */
    //async, is safe?
    public void handleAnimation(@NotNull CPacketAnimation packetIn) {
        this.player.markPlayerActive();
        //addToQueue(() -> {
        this.player.swingArm(packetIn.getHand());
        //});
    }


    /**
     * @author Te4hno
     */
    //async
    @Override
    public void processChatMessage(@NotNull CPacketChatMessage packetIn) {
        EntityPlayerMP player = this.player;
        if (player.getChatVisibility() == EntityPlayer.EnumChatVisibility.HIDDEN) {
            TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("chat.cannotSend");
            textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
            this.sendPacket(new SPacketChat(textcomponenttranslation));
        } else {
            String msg = org.apache.commons.lang3.StringUtils.normalizeSpace(packetIn.getMessage());
            char cur;
            for (int i = 0; i < msg.length(); ++i) {
                if((cur = msg.charAt(i)) == 167 || cur < ' ' || cur == 127){
                    this.disconnect(new TextComponentTranslation("multiplayer.disconnect.illegal_characters"));
                    return;
                }
            }
            this.chatSpamThresholdCount += 20;
            if (this.chatSpamThresholdCount > 200 && !this.server.getPlayerList().canSendCommands(this.player.getGameProfile())) {
                this.disconnect(new TextComponentTranslation("disconnect.spam"));
                return;
            }
            player.markPlayerActive();

            if (msg.startsWith("/")) {
                addToQueue(() -> handleSlashCommand(msg));
            } else {
                ITextComponent itextcomponent = new TextComponentTranslation("chat.type.text", this.player.getDisplayName(), ForgeHooks.newChatWithLinks(msg));
                itextcomponent = ForgeHooks.onServerChatEvent(player.connection, msg, itextcomponent);
                if (itextcomponent == null) return;
                this.server.getPlayerList().sendMessage(itextcomponent, false);
            }
        }
    }


    /**
     * @author Te4hno
     */
    @Override
    public void processEntityAction(@NotNull CPacketEntityAction packetIn) {
        EntityPlayerMP lPayer = this.player;
        lPayer.markPlayerActive();
        final CPacketEntityAction.Action action;

        switch (action = packetIn.getAction()){
            case START_SNEAKING:
                lPayer.setSneaking(true);
                return;
            case STOP_SNEAKING:
                lPayer.setSneaking(false);
                return;
            case START_SPRINTING:
                addToQueue(lPayer::setSprintingTrue);
                return;
            case STOP_SPRINTING:
                lPayer.setSprintingFalse();
                return;
        }

        addToQueue(() -> {
            EntityPlayerMP player = this.player;
            switch (action) {
                case STOP_SLEEPING:
                    if (player.isPlayerSleeping()) {
                        player.wakeUpPlayer(false, true, true);
                        this.targetPos = new VecDFD(player.posX, (float) player.posY, player.posZ);
                    }
                    break;
                case START_RIDING_JUMP:
                    if (player.getRidingEntity() instanceof IJumpingMount) {
                        IJumpingMount mount = (IJumpingMount)player.getRidingEntity();
                        int i = packetIn.getAuxData();
                        if (mount.canJump() && i > 0) {
                            mount.handleStartJump(i);
                        }
                    }
                    break;
                case STOP_RIDING_JUMP:
                    if (player.getRidingEntity() instanceof IJumpingMount) {
                        IJumpingMount ijumpingmount = (IJumpingMount)player.getRidingEntity();
                        ijumpingmount.handleStopJump();
                    }
                    break;
                case OPEN_INVENTORY:
                    if (player.getRidingEntity() instanceof AbstractHorse) {
                        ((AbstractHorse)player.getRidingEntity()).openGUI(player);
                    }
                    break;
                case START_FALL_FLYING:
                    if (!player.onGround && player.motionY < 0.0D && !player.isElytraFlying() && !player.isInWater()) {
                        ItemStack itemstack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

                        if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack)) {
                            player.setElytraFlying();
                        }
                    } else {
                        player.clearElytraFlying();
                    }
                    break;
                default:
                    player.getServer().ticker().onError(player.getServerWorld(),
                            new IllegalArgumentException("Invalid client command!")
                    );
            }
        });
    }


    /**
     * @author Te4hno
     */
    @Override
    public void processUseEntity(@NotNull CPacketUseEntity packetIn) {
        EntityPlayerMP lPlayer = this.player;
        WorldServer worldserver = this.server.getWorld(lPlayer.dimension);
        lPlayer.markPlayerActive();
        Entity entity = packetIn.getEntityFromWorld(worldserver);
        if (entity != null) {
            if (lPlayer.getDistanceSq(entity) < (lPlayer.canEntityBeSeen(entity) ? 36 : 9)) {
                addToQueue(() -> {
                    CPacketUseEntity.Action act;
                    if ((act = packetIn.getAction()) == CPacketUseEntity.Action.INTERACT) {
                        EnumHand enumhand = packetIn.getHand();
                        this.player.interactOn(entity, enumhand);
                    } else if (act == CPacketUseEntity.Action.INTERACT_AT) {
                        EnumHand enumhand1 = packetIn.getHand();
                        if (ForgeHooks.onInteractEntityAt(lPlayer, entity, packetIn.getHitVec(), enumhand1) != null)
                            return;
                        entity.applyPlayerInteraction(this.player, packetIn.getHitVec(), enumhand1);
                    } else if (act == CPacketUseEntity.Action.ATTACK) {
                        if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == this.player) {
                            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_entity_attacked"));
                            this.server.logWarning("Player " + this.player.getName() + " tried to attack an invalid entity");
                            return;
                        }
                        this.player.attackTargetEntityWithCurrentItem(entity);
                    }
                });
            }
        }
    }


    /**
     * @author Te4hno
     */
    @Override
    public void processClientStatus(@NotNull CPacketClientStatus packetIn) {
        this.player.markPlayerActive();
        addToQueue(() -> {
            switch (packetIn.getStatus()) {
                case PERFORM_RESPAWN:
                    if (this.player.queuedEndExit) {
                        this.player.queuedEndExit = false;

                        this.player = this.server.getPlayerList().recreatePlayerEntity(this.player, 0, true);

                        CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, DimensionType.THE_END, DimensionType.OVERWORLD);
                    } else {
                        if (this.player.getHealth() > 0.0F) return;

                        this.player = this.server.getPlayerList().recreatePlayerEntity(this.player, player.dimension, false);
                        if (this.server.isHardcore()) {
                            this.player.setGameType(GameType.SPECTATOR);
                        }
                    }
                    break;
                case REQUEST_STATS:
                    this.player.getStatFile().sendStats(this.player);
            }
        });
    }


    /**
     * @author Te4hno
     */
    public void processCloseWindow(@NotNull CPacketCloseWindow packetIn) {
        addToQueue(player::closeContainer);
    }


    /**
     * @author Te4hno
     */
    @Override
    public void processClickWindow(@NotNull CPacketClickWindow packetIn) {
        EntityPlayerMP lPlayer = this.player;
        lPlayer.markPlayerActive();
        if (lPlayer.openContainer.windowId == packetIn.getWindowId() && lPlayer.openContainer.getCanCraft(lPlayer)) {
            if (lPlayer.isSpectator()) {
                NonNullList<ItemStack> list = NonNullList.create();

                final Container openCtn;
                for (int i = 0, s = (openCtn = lPlayer.openContainer).inventorySlots.size(); i < s; ++i) {
                    list.add(openCtn.inventorySlots.get(i).getStack());
                }

                lPlayer.sendAllContents(openCtn, list);
            } else {
                addToQueue(() -> {
                    final EntityPlayerMP player = this.player;
                    final Container openCtn;
                    final CPacketClickWindow packet = packetIn;

                    ItemStack itemStack = (openCtn = player.openContainer)
                            .slotClick(packet.getSlotId(), packet.getUsedButton(), packet.getClickType(), player);
                    if (ItemStack.areItemStacksEqualUsingNBTShareTag(packet.getClickedItem(), itemStack)) {
                        netManager.sendPacketE(new SPacketConfirmTransaction(packet.getWindowId(), packet.getActionNumber(), true));
                        player.isChangingQuantityOnly = true;
                        openCtn.detectAndSendChanges();
                        player.updateHeldItem();
                        player.isChangingQuantityOnly = false;
                    } else {
                        this.pendingTransactions.addKey(openCtn.windowId, packet.getActionNumber());
                        netManager.sendPacketE(new SPacketConfirmTransaction(packet.getWindowId(), packet.getActionNumber(), false));
                        openCtn.setCanCraft(player, false);

                        List<Slot> slots = openCtn.inventorySlots;
                        final int size = slots.size();
                        NonNullList<ItemStack> list = NonNullList.withSize(size, ItemStack.EMPTY);

                        for (int j = 0; j < size; ++j) {
                            ItemStack itemstack = slots.get(j).getStack();
                            if(!itemstack.isEmpty())list.set(j, itemstack);
                        }
                        player.sendAllContents(openCtn, list);
                    }
                });
            }
        }
    }


    /**
     * @author Te4hno
     */
    public void func_194308_a(@NotNull CPacketPlaceRecipe packet) {
        this.player.markPlayerActive();
        if (!this.player.isSpectator() && this.player.openContainer.windowId == packet.func_194318_a() && this.player.openContainer.getCanCraft(this.player)) {
            addToQueue(() -> {
                ServerRecipeBookHelper.placeRecipe(this.player, packet.func_194317_b(), packet.func_194319_c());
            });
        }
    }


    /**
     * @author Te4hno
     */
    public void processEnchantItem(@NotNull CPacketEnchantItem packetIn) {
        this.player.markPlayerActive();
        if (this.player.openContainer.windowId == packetIn.getWindowId() && this.player.openContainer.getCanCraft(this.player) && !this.player.isSpectator()) {
            addToQueue(() -> {
                this.player.openContainer.enchantItem(this.player, packetIn.getButton());
                this.player.openContainer.detectAndSendChanges();
            });
        }
    }


    /**
     * @author Te4hno
     */
    public void processCreativeInventoryAction(@NotNull CPacketCreativeInventoryAction packetIn) {
        if (this.player.interactionManager.isCreative()) {
            addToQueue(() -> {
                boolean flag = packetIn.getSlotId() < 0;
                ItemStack itemstack = packetIn.getStack();

                if (!itemstack.isEmpty() && itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10)) {
                    NBTTagCompound nbttagcompound = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");

                    if (nbttagcompound.hasKey("x") && nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z")) {
                        BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
                        TileEntity tileentity = this.player.world.getTileEntity(blockpos);

                        if (tileentity != null) {
                            NBTTagCompound nbttagcompound1 = tileentity.writeToNBT(new NBTTagCompound());
                            nbttagcompound1.removeTag("x");
                            nbttagcompound1.removeTag("y");
                            nbttagcompound1.removeTag("z");
                            itemstack.setTagInfo("BlockEntityTag", nbttagcompound1);
                        }
                    }
                }
                boolean flag1 = packetIn.getSlotId() >= 1 && packetIn.getSlotId() <= 45;
                boolean flag2 = itemstack.isEmpty() || itemstack.getMetadata() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();

                if (flag1 && flag2) {
                    if (itemstack.isEmpty()) {
                        this.player.inventoryContainer.putStackInSlot(packetIn.getSlotId(), ItemStack.EMPTY);
                    } else {
                        this.player.inventoryContainer.putStackInSlot(packetIn.getSlotId(), itemstack);
                    }
                    this.player.inventoryContainer.setCanCraft(this.player, true);
                }
                else if (flag && flag2 && this.itemDropThreshold < 200) {
                    this.itemDropThreshold += 20;
                    EntityItem entityitem = this.player.dropItem(itemstack, true);

                    if (entityitem != null) {
                        entityitem.setAgeToCreativeDespawnTime();
                    }
                }
            });
        }
    }


    public void removeTransactions(int id){
        pendingTransactions.removeObject(id);
    }


    /**
     * @author Te4hno
     */
    public void processConfirmTransaction(@NotNull CPacketConfirmTransaction packetIn) {
        EntityPlayerMP player;
        short oshort = this.pendingTransactions.lookup((player = this.player).openContainer.windowId);
        if (oshort != Short.MIN_VALUE && packetIn.getUid() == oshort &&
                player.openContainer.windowId == packetIn.getWindowId()
                && !player.openContainer.getCanCraft(player) && !player.isSpectator()) {

            addToQueue(() -> {
                this.player.openContainer.setCanCraft(this.player, true);
            });
        }
    }


    /**
     * @author Te4hno
     */
    //todo
    public void processUpdateSign(@NotNull CPacketUpdateSign packetIn) {
        this.player.markPlayerActive();
        addToQueue(() -> {
            WorldServer worldserver = this.server.getWorld(this.player.dimension);
            BlockPos blockpos = packetIn.getPosition();

            if (worldserver.isBlockLoaded(blockpos)) {
                IBlockState iblockstate = worldserver.getBlockState(blockpos);
                TileEntity tileentity = worldserver.getTileEntity(blockpos);

                if (!(tileentity instanceof TileEntitySign)) return;

                TileEntitySign tileentitysign = (TileEntitySign)tileentity;

                if (!tileentitysign.getIsEditable() || tileentitysign.getPlayer() != this.player) {
                    this.server.logWarning("Player " + this.player.getName() + " just tried to change non-editable sign");
                    return;
                }

                String[] astring = packetIn.getLines();

                for (int i = 0; i < astring.length; ++i) {
                    tileentitysign.signText[i] = new TextComponentString(TextFormatting.getTextWithoutFormattingCodes(astring[i]));
                }
                tileentitysign.markDirty();
                worldserver.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
            }
        });
    }


    /**
     * @author Te4hno
     */
    public void processTryUseItemOnBlock(final CPacketPlayerTryUseItemOnBlock packetIn) {
        EntityPlayerMP lPlayer;
        final WorldServer worldserver = (lPlayer = this.player).getServerWorld();
        final BlockPos blockpos = packetIn.getPos();
        lPlayer.markPlayerActive();
        int buildLim;

        if (blockpos.getY() < (buildLim = server.getBuildLimit()) - 1 || packetIn.getDirection() != EnumFacing.UP && blockpos.getY() < buildLim) {
            double dist = lPlayer.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 3;
            dist *= dist;
            if (targetPos == null && lPlayer.getDistanceSq(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5) < dist &&
                    !worldserver.isBlockModifiable(lPlayer, blockpos)) {

                final BlockPos blockPos2 = blockpos.offset(packetIn.getDirection());

                addToQueue(() -> {
                    EntityPlayerMP player = NetHandlerPlayServer.this.player;
                    WorldServer world = worldserver;
                    CPacketPlayerTryUseItemOnBlock packet = packetIn;

                    player.interactionManager.processRightClickBlock(
                            player, world, player.getHeldItem(packet.getHand()),
                            packet.getHand(), blockpos, packet.getDirection(), packet.getFacingX(),
                            packet.getFacingY(), packet.getFacingZ()
                    );

                    IBlockState state1 = world.localSelector.getState(blockpos); //safe
                    IBlockState state2 = world.localSelector.getState(blockPos2);//safe

                    netManager.exec(() -> {
                        netManager.sendPacketSync(new SPacketBlockChange(state1, blockpos));
                        netManager.sendPacketSync(new SPacketBlockChange(state2, blockPos2));
                    });
                });
            }
        } else {
            TextComponentTranslation msg = new TextComponentTranslation("build.tooHigh", this.server.getBuildLimit());
            msg.getStyle().setColor(TextFormatting.RED);
            sendPacket(new SPacketChat(msg, ChatType.GAME_INFO));

            int cX1 = blockpos.getX()>>4, cZ1 = blockpos.getZ()>>4;
            Chunk chunk = worldserver.getLoadedChunk(cX1, cZ1);
            IBlockState state1 = (chunk == null)?BlockIDIndexes.AIR_STATE:chunk.getBlockState(blockpos);

            BlockPos blockPos2 = blockpos.offset(packetIn.getDirection());
            int cX2 = blockPos2.getX()>>4, cZ2 = blockPos2.getZ()>>4;

            if(cX1 != cX2 || cZ1 != cZ2){
                chunk = worldserver.getLoadedChunk(cX2, cZ2);
            }
            IBlockState state2 = (chunk == null)?BlockIDIndexes.AIR_STATE:chunk.getBlockState(blockpos);

            netManager.exec(() -> {
                netManager.sendPacketSync(new SPacketBlockChange(state1, blockpos));
                netManager.sendPacketSync(new SPacketBlockChange(state2, blockPos2));
            });
        }
    }


    /**
     * @author Te4hno
     */
    public void processTryUseItem(CPacketPlayerTryUseItem packetIn) {
        //todo fix glitch click to unloaded air
        final EnumHand enumhand = packetIn.getHand();
        EntityPlayerMP lPlayer;
        ItemStack itemstack = (lPlayer = player).getHeldItem(enumhand);
        if (!itemstack.isEmpty()) {
            lPlayer.markPlayerActive();
            addToQueue(() -> {
                EntityPlayerMP lPlayer1 = player;
                lPlayer1.interactionManager.processRightClick(lPlayer1, lPlayer1.world, itemstack, enumhand);
            });
        }
    }


    /**
     * @author Te4hno
     */
    public void handleSpectate(@NotNull CPacketSpectate packetIn) {
        if (this.player.isSpectator()) {
            addToQueue(() -> handleSpectate0(packetIn));
        }
    }


    /**
     * @author Te4hno
     */
    public void processSteerBoat(@NotNull CPacketSteerBoat packetIn) {
        Entity entity = this.player.getRidingEntity();
        if (entity instanceof EntityBoat) {
            addToQueue(() -> {
                ((EntityBoat)entity).setPaddleState(packetIn.getLeft(), packetIn.getRight());
            });
        }
    }


    //todo check
    private void handleSpectate0(CPacketSpectate packetIn) {
        Entity entity = null;
        MinecraftServer server = this.server;
        for (WorldServer worldserver : server.worlds) {
            if (worldserver != null) {
                entity = packetIn.getEntity(worldserver);
                if (entity != null) {
                    break;
                }
            }
        }
        if (entity != null) {
            EntityPlayerMP player = this.player;
            player.setSpectatingEntity(player);
            player.dismountRidingEntity();

            if (entity.world == player.world) {
                player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
            } else if (net.minecraftforge.common.ForgeHooks.onTravelToDimension(player, entity.dimension)) {
                int prevDimension = player.dimension;
                WorldServer worldserver1 = player.getServerWorld();
                WorldServer worldserver2 = (WorldServer) entity.world;
                player.dimension = entity.dimension;
                netManager.sendPacketE(new SPacketRespawn(player.dimension, worldserver2.getDifficulty(), worldserver2.getWorldInfo().getTerrainType(), player.interactionManager.getGameType())); // Forge: Use new dimensions information
                server.getPlayerList().updatePermissionLevel(player);
                worldserver1.removeEntityDangerously(player);
                player.isDead = false;
                player.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

                if (player.isEntityAlive()) {
                    worldserver1.updateEntityWithOptionalForce(player, false);
                    worldserver2.spawnEntity(player);
                    worldserver2.updateEntityWithOptionalForce(player, false);
                }

                player.setWorld(worldserver2);
                server.getPlayerList().preparePlayer(player, worldserver1);
                player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
                //this.player.interactionManager.setWorld(worldserver2);
                server.getPlayerList().updateTimeAndWeatherForPlayer(player, worldserver2);
                server.getPlayerList().syncPlayerInventory(player);
                net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, prevDimension, player.dimension);
            }
        }
    }


    /**
     * @author Te4hno
     */
    //async
    public void processPlayerAbilities(CPacketPlayerAbilities packetIn) {
        this.player.capabilities.isFlying = packetIn.isFlying() && this.player.capabilities.allowFlying;
    }


    /**
     * @author Te4hno
     */
    public void processTabComplete(@NotNull CPacketTabComplete packetIn) {
        addToQueue(() -> {
            List<String> list = this.server.getTabCompletions(this.player, packetIn.getMessage(), packetIn.getTargetBlock(), packetIn.hasTargetBlock());
            netManager.sendPacketE(new SPacketTabComplete(list.toArray(new String[0])));
        });
    }


    /**
     * @author Te4hno
     */
    //todo make sync if error
    public void processHeldItemChange(@NotNull CPacketHeldItemChange packetIn) {
        if (packetIn.getSlotId() >= 0 && packetIn.getSlotId() < InventoryPlayer.getHotbarSize()) {
            this.player.inventory.currentItem = packetIn.getSlotId();
            this.player.markPlayerActive();
        } else {
            LOGGER.warn("{} tried to set an invalid carried item", this.player.getName());
        }
    }


    /**
     * @author Te4hno
     */
    // async
    public void processClientSettings(@NotNull CPacketClientSettings packetIn) {
        this.player.handleClientSettings(packetIn);
        //addToQueue(() -> this.player.handleClientSettings(packetIn));
    }


    /**
     * @author Te4hno
     */
    @Override
    public void processCustomPayload(@NotNull CPacketCustomPayload packetIn){
        PacketBuffer buf = packetIn.getAndRemData();
        addToQueue(() -> {
            try {
                processCustomPayload0(packetIn, buf);
            } finally {
                buf.release();
            }
        });
    }


    private void processCustomPayload0(@NotNull CPacketCustomPayload packetIn, PacketBuffer buf) {
        String s = packetIn.getChannelName();
        EntityPlayerMP player = this.player;
        switch (s) {
            case "MC|BEdit":
                try {
                    ItemStack stack = buf.readItemStack();
                    if (stack.isEmpty()) return;
                    if (!ItemWritableBook.isNBTValid(stack.getTagCompound()))
                        throw new IOException("Invalid book tag!");
                    ItemStack itemstack1 = player.getHeldItemMainhand();
                    if (itemstack1.isEmpty()) return;
                    if (stack.getItem() == Items.WRITABLE_BOOK && stack.getItem() == itemstack1.getItem()) {
                        itemstack1.setTagInfo("pages", stack.getTagCompound().getTagList("pages", 8));
                    }
                } catch (Exception exception6) {
                    LOGGER.error("Couldn't handle book info", exception6);
                }
                break;
            case "MC|BSign":
                try {
                    ItemStack stack = buf.readItemStack();
                    if (stack.isEmpty()) return;
                    if (!ItemWrittenBook.validBookTagContents(stack.getTagCompound()))
                        throw new IOException("Invalid book tag!");

                    ItemStack itemstack4 = player.getHeldItemMainhand();
                    if (itemstack4.isEmpty()) {
                        return;
                    }
                    if (stack.getItem() == Items.WRITABLE_BOOK && itemstack4.getItem() == Items.WRITABLE_BOOK) {
                        ItemStack itemstack2 = new ItemStack(Items.WRITTEN_BOOK);
                        itemstack2.setTagInfo("author", new NBTTagString(player.getName()));
                        itemstack2.setTagInfo("title", new NBTTagString(stack.getTagCompound().getString("title")));
                        NBTTagList nbttaglist = stack.getTagCompound().getTagList("pages", 8);

                        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                            String s1 = nbttaglist.getStringTagAt(i);
                            ITextComponent itextcomponent = new TextComponentString(s1);
                            s1 = ITextComponent.Serializer.componentToJson(itextcomponent);
                            nbttaglist.set(i, new NBTTagString(s1));
                        }
                        itemstack2.setTagInfo("pages", nbttaglist);
                        player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemstack2);
                    }
                } catch (Exception exception7) {
                    LOGGER.error("Couldn't sign book", (Throwable) exception7);
                }
                break;
            case "MC|TrSel":
                try {
                    int k = buf.readInt();
                    Container container = this.player.openContainer;

                    if (container instanceof ContainerMerchant) {
                        ((ContainerMerchant) container).setCurrentRecipeIndex(k);
                    }
                } catch (Exception exception5) {
                    LOGGER.error("Couldn't select trade", exception5);
                }
                break;
            case "MC|AdvCmd":
                if (!this.server.isCommandBlockEnabled()) {
                    player.sendMessage(new TextComponentTranslation("advMode.notEnabled"));
                    return;
                }

                if (!player.canUseCommandBlock()) {
                    player.sendMessage(new TextComponentTranslation("advMode.notAllowed"));
                    return;
                }

                try {
                    int l = buf.readByte();
                    CommandBlockBaseLogic commandblockbaselogic1 = null;

                    if (l == 0) {
                        TileEntity tileentity = player.world.getTileEntity(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));

                        if (tileentity instanceof TileEntityCommandBlock) {
                            commandblockbaselogic1 = ((TileEntityCommandBlock) tileentity).getCommandBlockLogic();
                        }
                    } else if (l == 1) {
                        Entity entity = player.world.getEntityByID(buf.readInt());

                        if (entity instanceof EntityMinecartCommandBlock) {
                            commandblockbaselogic1 = ((EntityMinecartCommandBlock) entity).getCommandBlockLogic();
                        }
                    }
                    String s6 = buf.readString(buf.readableBytes());
                    boolean flag2 = buf.readBoolean();

                    if (commandblockbaselogic1 != null) {
                        commandblockbaselogic1.setCommand(s6);
                        commandblockbaselogic1.setTrackOutput(flag2);

                        if (!flag2) {
                            commandblockbaselogic1.setLastOutput(null);
                        }

                        commandblockbaselogic1.updateCommand();
                        player.sendMessage(new TextComponentTranslation("advMode.setCommand.success", s6));
                    }
                } catch (Exception exception4) {
                    LOGGER.error("Couldn't set command block", (Throwable) exception4);
                }
                break;
            case "MC|AutoCmd":
                if (!this.server.isCommandBlockEnabled()) {
                    player.sendMessage(new TextComponentTranslation("advMode.notEnabled"));
                    return;
                }

                if (!this.player.canUseCommandBlock()) {
                    player.sendMessage(new TextComponentTranslation("advMode.notAllowed"));
                    return;
                }

                try {
                    CommandBlockBaseLogic commandblockbaselogic = null;
                    TileEntityCommandBlock tileentitycommandblock = null;
                    BlockPos blockpos1 = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
                    TileEntity tileentity2 = player.world.getTileEntity(blockpos1);

                    if (tileentity2 instanceof TileEntityCommandBlock) {
                        tileentitycommandblock = (TileEntityCommandBlock) tileentity2;
                        commandblockbaselogic = tileentitycommandblock.getCommandBlockLogic();
                    }

                    String s7 = buf.readString(buf.readableBytes());
                    boolean flag3 = buf.readBoolean();
                    TileEntityCommandBlock.Mode mode = TileEntityCommandBlock.Mode.valueOf(buf.readString(16));
                    boolean flag = buf.readBoolean();
                    boolean flag1 = buf.readBoolean();

                    if (commandblockbaselogic != null) {
                        EnumFacing enumfacing = player.world.getBlockState(blockpos1).getValue(BlockCommandBlock.FACING);

                        switch (mode) {
                            case SEQUENCE:
                                IBlockState iblockstate3 = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
                                player.world.setBlockState(blockpos1, iblockstate3.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, flag), 2);
                                break;
                            case AUTO:
                                IBlockState iblockstate2 = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
                                player.world.setBlockState(blockpos1, iblockstate2.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, flag), 2);
                                break;
                            case REDSTONE:
                                IBlockState iblockstate = Blocks.COMMAND_BLOCK.getDefaultState();
                                player.world.setBlockState(blockpos1, iblockstate.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, flag), 2);
                        }

                        tileentity2.validate();
                        player.world.setTileEntity(blockpos1, tileentity2);
                        commandblockbaselogic.setCommand(s7);
                        commandblockbaselogic.setTrackOutput(flag3);

                        if (!flag3) {
                            commandblockbaselogic.setLastOutput((ITextComponent) null);
                        }

                        tileentitycommandblock.setAuto(flag1);
                        commandblockbaselogic.updateCommand();

                        if (!net.minecraft.util.StringUtils.isNullOrEmpty(s7)) {
                            player.sendMessage(new TextComponentTranslation("advMode.setCommand.success", s7));
                        }
                    }
                } catch (Exception exception3) {
                    LOGGER.error("Couldn't set command block", (Throwable) exception3);
                }
                break;
            case "MC|Beacon":
                if (this.player.openContainer instanceof ContainerBeacon) {
                    try {
                        int i1 = buf.readInt();
                        int k1 = buf.readInt();
                        ContainerBeacon containerbeacon = (ContainerBeacon) player.openContainer;
                        Slot slot = containerbeacon.getSlot(0);

                        if (slot.getHasStack()) {
                            slot.decrStackSize(1);
                            IInventory iinventory = containerbeacon.getTileEntity();
                            iinventory.setField(1, i1);
                            iinventory.setField(2, k1);
                            iinventory.markDirty();
                        }
                    } catch (Exception exception2) {
                        LOGGER.error("Couldn't set beacon", exception2);
                    }
                }
                break;
            case "MC|ItemName":
                if (player.openContainer instanceof ContainerRepair) try {
                    ContainerRepair containerrepair = (ContainerRepair) player.openContainer;

                    if (buf != null && buf.readableBytes() >= 1) {
                        String s5 = ChatAllowedCharacters.filterAllowedCharacters(buf.readString(32767));

                        if (s5.length() <= 35) {
                            containerrepair.updateItemName(s5);
                        }
                    } else {
                        containerrepair.updateItemName("");
                    }
                } catch (Exception e){
                    LOGGER.error("Couldn't process [MC|ItemName]", e);
                }
                break;
            case "MC|Struct":
                if (!player.canUseCommandBlock()) return;

                try {
                    BlockPos blockpos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
                    IBlockState iblockstate1 = player.world.getBlockState(blockpos);
                    TileEntity tileentity1 = player.world.getTileEntity(blockpos);

                    if (tileentity1 instanceof TileEntityStructure) {
                        TileEntityStructure structure = (TileEntityStructure) tileentity1;
                        int l1 = buf.readByte();
                        String s8 = buf.readString(32);
                        structure.setMode(TileEntityStructure.Mode.valueOf(s8));
                        structure.setName(buf.readString(64));
                        int i2 = MathHelper.clamp(buf.readInt(), -32, 32);
                        int j2 = MathHelper.clamp(buf.readInt(), -32, 32);
                        int k2 = MathHelper.clamp(buf.readInt(), -32, 32);
                        structure.setPosition(new BlockPos(i2, j2, k2));
                        int l2 = MathHelper.clamp(buf.readInt(), 0, 32);
                        int i3 = MathHelper.clamp(buf.readInt(), 0, 32);
                        int j = MathHelper.clamp(buf.readInt(), 0, 32);
                        structure.setSize(new BlockPos(l2, i3, j));
                        String s2 = buf.readString(32);
                        structure.setMirror(Mirror.valueOf(s2));
                        String s3 = buf.readString(32);
                        structure.setRotation(Rotation.valueOf(s3));
                        structure.setMetadata(buf.readString(128));
                        structure.setIgnoresEntities(buf.readBoolean());
                        structure.setShowAir(buf.readBoolean());
                        structure.setShowBoundingBox(buf.readBoolean());
                        structure.setIntegrity(MathHelper.clamp(buf.readFloat(), 0.0F, 1.0F));
                        structure.setSeed(buf.readVarLong());
                        String s4 = structure.getName();

                        if (l1 == 2) {
                            if (structure.save()) {
                                player.sendStatusMessage(new TextComponentTranslation("structure_block.save_success", s4), false);
                            } else {
                                player.sendStatusMessage(new TextComponentTranslation("structure_block.save_failure", s4), false);
                            }
                        } else if (l1 == 3) {
                            if (!structure.isStructureLoadable()) {
                                player.sendStatusMessage(new TextComponentTranslation("structure_block.load_not_found", s4), false);
                            } else if (structure.load()) {
                                player.sendStatusMessage(new TextComponentTranslation("structure_block.load_success", s4), false);
                            } else {
                                player.sendStatusMessage(new TextComponentTranslation("structure_block.load_prepare", s4), false);
                            }
                        } else if (l1 == 4) {
                            if (structure.detectSize()) {
                                player.sendStatusMessage(new TextComponentTranslation("structure_block.size_success", s4), false);
                            } else {
                                player.sendStatusMessage(new TextComponentTranslation("structure_block.size_failure"), false);
                            }
                        }

                        structure.markDirty();
                        player.world.notifyBlockUpdate(blockpos, iblockstate1, iblockstate1, 3);
                    }
                } catch (Exception exception1) {
                    LOGGER.error("Couldn't set structure block", exception1);
                }
                break;
            case "MC|PickItem":
                try {
                    int j1 = buf.readVarInt();
                    player.inventory.pickItem(j1);
                    netManager.addToQueueN(new SPacketSetSlot(-2, player.inventory.currentItem, player.inventory.getStackInSlot(player.inventory.currentItem)));
                    netManager.addToQueueN(new SPacketSetSlot(-2, j1, player.inventory.getStackInSlot(j1)));
                    netManager.sendPacketE(new SPacketHeldItemChange(player.inventory.currentItem));
                } catch (Exception exception) {
                    LOGGER.error("Couldn't pick item", exception);
                }
                break;
            case "Elca|Check":
                LOGGER.info("User [{}] has Elca client", player.getName());
                break;
        }
    }


    /**
     * @author Te4hno
     */
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<SPacketPlayerPosLook.EnumFlags> relativeSet) {
        EntityPlayerMP player = this.player;
        double d0 = relativeSet.contains(SPacketPlayerPosLook.EnumFlags.X) ? player.posX : 0.0D;
        double d1 = relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Y) ? player.posY : 0.0D;
        double d2 = relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Z) ? player.posZ : 0.0D;
        VecDFD targetPos = this.targetPos = new VecDFD(x + d0, (float) (y + d1), z + d2);
        float f = yaw;
        float f1 = pitch;

        if (relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
            f = yaw + player.rotationYaw;
        }

        if (relativeSet.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
            f1 = pitch + player.rotationPitch;
        }

        if (++this.teleportId == Integer.MAX_VALUE) {
            this.teleportId = 0;
        }

        this.lastPositionUpdate = this.networkTickCount;
        player.setPositionAndRotation(targetPos.x, y + d1, targetPos.z, f, f1);
        player.connection.netManager.sendPacketE(new SPacketPlayerPosLook(x, y, z, yaw, pitch, relativeSet, this.teleportId));
    }


    /**
     * @author Te4hno
     */
    public void sendPacket(final Packet<?> packetIn) {
        if (packetIn instanceof SPacketChat) {
            SPacketChat spacketchat = (SPacketChat)packetIn;
            EntityPlayer.EnumChatVisibility visibility = this.player.getChatVisibility();

            if (visibility == EntityPlayer.EnumChatVisibility.HIDDEN && spacketchat.getType() != ChatType.GAME_INFO) {
                return;
            }

            if (visibility == EntityPlayer.EnumChatVisibility.SYSTEM && !spacketchat.isSystem()) {
                return;
            }
        }
        try {
            this.netManager.sendPacketE(packetIn);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Sending packet");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Packet being sent");
            crashreportcategory.addDetail("Packet class", () -> packetIn.getClass().getCanonicalName());
            throw new ReportedException(crashreport);
        }
    }
}
