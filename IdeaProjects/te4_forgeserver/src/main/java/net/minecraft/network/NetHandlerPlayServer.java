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
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.ServerRecipeBookHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;


public class NetHandlerPlayServer implements INetHandlerPlayServer, ITickable {
    private static final Logger LOGGER = LogManager.getLogger();
    public final NetworkManager netManager;
    private final MinecraftServer server;
    public EntityPlayerMP player;
    private int networkTickCount;
    private long field_194402_f;
    private boolean field_194403_g;
    private long field_194404_h;
    private int chatSpamThresholdCount;
    private int itemDropThreshold;
    private final IntHashMap<Short> pendingTransactions = new IntHashMap<>();
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
    private Vec3d targetPos;
    private int teleportId;
    private int lastPositionUpdate;
    private boolean floating;
    private int floatingTickCount;
    private boolean vehicleFloating;
    private int vehicleFloatingTickCount;
    private int movePacketCounter;
    private int lastMovePacketCounter;
    private ServerRecipeBookHelper field_194309_H = new ServerRecipeBookHelper();

    public NetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
        this.server = server;
        this.netManager = networkManagerIn;
        networkManagerIn.setNetHandler(this);
        this.player = playerIn;
        playerIn.connection = this;
    }


    private void captureCurrentPosition() {
        this.firstGoodX = this.player.posX;
        this.firstGoodY = this.player.posY;
        this.firstGoodZ = this.player.posZ;
        this.lastGoodX = this.player.posX;
        this.lastGoodY = this.player.posY;
        this.lastGoodZ = this.player.posZ;
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


    public void onDisconnect(ITextComponent reason) {
        LOGGER.info("{} lost connection: {}", this.player.getName(), reason.getUnformattedText());
        this.server.refreshStatusNextTick();
        TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("multiplayer.player.left", this.player.getDisplayName());
        textcomponenttranslation.getStyle().setColor(TextFormatting.YELLOW);
        this.server.getPlayerList().sendMessage(textcomponenttranslation);
        this.player.mountEntityAndWakeUp();
        this.server.getPlayerList().playerLoggedOut(this.player);

        if (this.server.isSinglePlayer() && this.player.getName().equals(this.server.getServerOwner())) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.server.initiateShutdown();
        }
    }


    private void handleSlashCommand(String command) {
        this.server.getCommandManager().executeCommand(this.player, command);
    }


    public void processKeepAlive(CPacketKeepAlive packetIn) {
        if (this.field_194403_g && packetIn.getKey() == this.field_194404_h) {
            int i = (int)(this.currentTimeMillis() - this.field_194402_f);
            this.player.ping = (this.player.ping * 3 + i) / 4;
            this.field_194403_g = false;
        } else if (!this.player.getName().equals(this.server.getServerOwner()))
        {
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

        this.captureCurrentPosition();
        this.player.onUpdateEntity();
        this.player.setPositionAndRotation(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.rotationYaw, this.player.rotationPitch);
        ++this.networkTickCount;
        this.lastMovePacketCounter = this.movePacketCounter;
        if (this.floating) {
            if (++this.floatingTickCount > 80) {
                LOGGER.warn("{} was kicked for floating too long!", this.player.getName());
                this.disconnect(new TextComponentTranslation("multiplayer.disconnect.flying"));
                return;
            }
        } else {
            this.floatingTickCount = 0;
        }
        this.lowestRiddenEnt = this.player.getLowestRidingEntity();

        if (this.lowestRiddenEnt != this.player && this.lowestRiddenEnt.getControllingPassenger() == this.player) {
            this.lowestRiddenX = this.lowestRiddenEnt.posX;
            this.lowestRiddenY = this.lowestRiddenEnt.posY;
            this.lowestRiddenZ = this.lowestRiddenEnt.posZ;
            this.lowestRiddenX1 = this.lowestRiddenEnt.posX;
            this.lowestRiddenY1 = this.lowestRiddenEnt.posY;
            this.lowestRiddenZ1 = this.lowestRiddenEnt.posZ;

            if (this.vehicleFloating && this.player.getLowestRidingEntity().getControllingPassenger() == this.player) {
                if (++this.vehicleFloatingTickCount > 80) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", this.player.getName());
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


        if (this.player.getLastActiveTime() > 0L && this.server.getMaxPlayerIdleMinutes() > 0 && MinecraftServer.getCurrentTimeMillis() - this.player.getLastActiveTime() > (long)(this.server.getMaxPlayerIdleMinutes() * 1000 * 60)) {
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


    /**
     * @author Te4hno
     */
    public void processPlayer(@NotNull CPacketPlayer packetIn) {
        if (isMovePlayerPacketInvalid(packetIn)) {
            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_player_movement"));
        } else {
            if (!this.player.queuedEndExit)
                addToQueue(() -> processPacket0(packetIn));
        }
    }


    private void processPacket0(CPacketPlayer packetIn){
        if (this.networkTickCount == 0) {
            this.captureCurrentPosition();
        }
        if (this.targetPos != null) {
            if (this.networkTickCount - this.lastPositionUpdate > 20) {
                this.lastPositionUpdate = this.networkTickCount;
                this.setPlayerLocation(this.targetPos.x, this.targetPos.y, this.targetPos.z, this.player.rotationYaw, this.player.rotationPitch);
            }
        } else {
            WorldServer worldserver = this.server.getWorld(this.player.dimension);

            this.lastPositionUpdate = this.networkTickCount;

            if (this.player.isRiding()) {
                this.player.setPositionAndRotation(this.player.posX, this.player.posY, this.player.posZ, packetIn.getYaw(this.player.rotationYaw), packetIn.getPitch(this.player.rotationPitch));

                worldserver.getPlayerChunkMap().updateMovingPlayer(this.player);
            } else {
                double d4 = packetIn.getX(this.player.posX);
                double d5 = packetIn.getY(this.player.posY);
                double d6 = packetIn.getZ(this.player.posZ);
                float f = packetIn.getYaw(this.player.rotationYaw);
                float f1 = packetIn.getPitch(this.player.rotationPitch);
                double d7 = d4 - this.firstGoodX;
                double d8 = d5 - this.firstGoodY;
                double d9 = d6 - this.firstGoodZ;
                double d10 = this.player.motionX * this.player.motionX + this.player.motionY * this.player.motionY + this.player.motionZ * this.player.motionZ;
                double d11 = d7 * d7 + d8 * d8 + d9 * d9;

                if (this.player.isPlayerSleeping()) {
                    if (d11 > 1.0D) {
                        this.setPlayerLocation(this.player.posX, this.player.posY, this.player.posZ, packetIn.getYaw(this.player.rotationYaw), packetIn.getPitch(this.player.rotationPitch));
                    }
                } else {
                    ++this.movePacketCounter;
                    int i = this.movePacketCounter - this.lastMovePacketCounter;

                    if (i > 5) {
                        LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName(), i);
                        i = 1;
                    }

                    if (!this.player.isInvulnerableDimensionChange()) {
                        if (!this.player.isCreative() && d11 - d10 > ((this.player.isElytraFlying() ? 300.0F : 100.0F) * i)) {
                            LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName(), d7, d8, d9);
                            this.setPlayerLocation(this.player.posX, this.player.posY, this.player.posZ, this.player.rotationYaw, this.player.rotationPitch);
                            return;
                        }
                    }
                    boolean flag2 = worldserver.getCollisionBoxes(this.player, this.player.getEntityBoundingBox().shrink(0.0625D)).isEmpty();

                    double d0 = this.player.posX,
                            d1 = this.player.posY,
                            d2 = this.player.posZ;

                    d7 = d4 - this.lastGoodX;
                    d8 = d5 - this.lastGoodY;
                    d9 = d6 - this.lastGoodZ;

                    if (this.player.onGround && !packetIn.isOnGround() && d8 > 0.0D) {
                        this.player.jump();
                    }

                    this.player.move(MoverType.PLAYER, d7, d8, d9);
                    this.player.onGround = packetIn.isOnGround();
                    double d12 = d8;
                    d7 = d4 - this.player.posX;
                    d8 = d5 - this.player.posY;

                    if (d8 > -0.5D || d8 < 0.5D) {
                        d8 = 0.0D;
                    }

                    d9 = d6 - this.player.posZ;
                    d11 = d7 * d7 + d8 * d8 + d9 * d9;
                    boolean flag = false;

                    if (!this.player.isInvulnerableDimensionChange() && d11 > 0.0625D && !this.player.isPlayerSleeping() && !this.player.interactionManager.isCreative() && this.player.interactionManager.getGameType() != GameType.SPECTATOR) {
                        flag = true;
                        LOGGER.warn("{} moved wrongly!", this.player.getName());
                    }

                    this.player.setPositionAndRotation(d4, d5, d6, f, f1);
                    this.player.addMovementStat(this.player.posX - d0, this.player.posY - d1, this.player.posZ - d2);

                    if (!this.player.noClip && !this.player.isPlayerSleeping()) {
                        boolean flag1 = worldserver.getCollisionBoxes(this.player, this.player.getEntityBoundingBox().shrink(0.0625D)).isEmpty();

                        if (flag2 && (flag || !flag1)) {
                            this.setPlayerLocation(d0, d1, d2, f, f1);
                            return;
                        }
                    }

                    this.floating = d12 >= -0.03125D;
                    this.floating &= !this.server.isFlightAllowed() && !this.player.capabilities.allowFlying;
                    this.floating &= !this.player.isPotionActive(MobEffects.LEVITATION) && !this.player.isElytraFlying()
                            && !worldserver.checkBlockCollision(
                            this.player.getEntityBoundingBox().grow(0.0625D).expand(0.0D, -0.55D, 0.0D)
                    );
                    this.player.onGround = packetIn.isOnGround();
                    updateMoving();
                    this.player.handleFalling(this.player.posY - d1, packetIn.isOnGround());
                    this.lastGoodX = this.player.posX;
                    this.lastGoodY = this.player.posY;
                    this.lastGoodZ = this.player.posZ;
                }
            }
        }
    }


    private void updateMoving(){
        this.player.getServerWorld().getPlayerChunkMap().updateMovingPlayer(this.player);
    }


    /**
     * @author Te4hno
     */
    public void processPlayerDigging(@NotNull CPacketPlayerDigging packetIn) {
        WorldServer worldserver = this.server.getWorld(this.player.dimension);
        addToQueue(() -> processPlayerDigging0(packetIn, worldserver));
    }


    private void processPlayerDigging0(CPacketPlayerDigging packetIn, WorldServer worldserver) {
        BlockPos blockpos = packetIn.getPosition();
        this.player.markPlayerActive();

        switch (packetIn.getAction()) {
            case SWAP_HELD_ITEMS:
                if (!this.player.isSpectator()) {
                    ItemStack itemstack = this.player.getHeldItem(EnumHand.OFF_HAND);
                    this.player.setHeldItem(EnumHand.OFF_HAND, this.player.getHeldItem(EnumHand.MAIN_HAND));
                    this.player.setHeldItem(EnumHand.MAIN_HAND, itemstack);
                }
                return;
            case DROP_ITEM:
                if (!this.player.isSpectator()) {
                    this.player.dropItem(false);
                }
                return;
            case DROP_ALL_ITEMS:
                if (!this.player.isSpectator()) {
                    this.player.dropItem(true);
                }
                return;
            case RELEASE_USE_ITEM:
                this.player.stopActiveHand();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                if(blockpos.getY() >= this.server.getBuildLimit())return;
                double d0 = this.player.posX - ((double)blockpos.getX() + 0.5D);
                double d1 = this.player.posY - ((double)blockpos.getY() + 0.5D) + 1.5D;
                double d2 = this.player.posZ - ((double)blockpos.getZ() + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                double dist = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1;

                if (d3 < dist * dist) {
                    if (packetIn.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        if (worldserver.getWorldBorder().contains(blockpos))
                            this.player.interactionManager.onBlockClicked(blockpos, packetIn.getFacing());
                        else
                            this.player.connection.netManager.sendPacketE(new SPacketBlockChange(worldserver, blockpos));
                    } else {
                        if (packetIn.getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)
                            this.player.interactionManager.blockRemoving(blockpos);
                        else
                            //if(packetIn.getAction() == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK)
                            this.player.interactionManager.cancelDestroyingBlock();
                        if (worldserver.getBlockState(blockpos).getMaterial() != Material.AIR)
                            this.player.connection.netManager.sendPacketE(new SPacketBlockChange(worldserver, blockpos));
                    }
                }
        }
    }


    /**
     * @author Te4hno
     */
    public void processVehicleMove(@NotNull CPacketVehicleMove packetIn) {
        if (isMoveVehiclePacketInvalid(packetIn)) {
            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_vehicle_movement"));
        } else {
            addToQueue(() -> processVehicleMove0(packetIn));
        }
    }


    private void processVehicleMove0(CPacketVehicleMove packetIn){
        Entity entity = this.player.getLowestRidingEntity();

        if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lowestRiddenEnt) {
            double d3 = packetIn.getX();
            double d4 = packetIn.getY();
            double d5 = packetIn.getZ();

            double d6 = d3 - this.lowestRiddenX;
            double d7 = d4 - this.lowestRiddenY;
            double d8 = d5 - this.lowestRiddenZ;
            double d9 = entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ;
            double d10 = d6 * d6 + d7 * d7 + d8 * d8;

            if (d10 - d9 > 100.0D && (!this.server.isSinglePlayer() || !this.server.getServerOwner().equals(entity.getName()))) {
                LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName(), this.player.getName(), d6, d7, d8);
                netManager.sendPacketE(new SPacketMoveVehicle(entity));
                return;
            }

            double d0 = entity.posX,
                    d1 = entity.posY,
                    d2 = entity.posZ;

            float f = packetIn.getYaw(),
                    f1 = packetIn.getPitch();

            WorldServer worldserver = this.player.getServerWorld();

            boolean flag = worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().shrink(0.0625D)).isEmpty();
            d6 = d3 - this.lowestRiddenX1;
            d7 = d4 - this.lowestRiddenY1 - 1.0E-6D;
            d8 = d5 - this.lowestRiddenZ1;
            entity.move(MoverType.PLAYER, d6, d7, d8);
            double d11 = d7;

            entity.setPositionAndRotation(d3, d4, d5, f, f1);
            this.player.setPositionAndRotation(d3, d4, d5, this.player.rotationYaw, this.player.rotationPitch);

            if (flag) {
                d6 = d3 - entity.posX;
                d7 = d4 - entity.posY;
                if (d7 > -0.5D || d7 < 0.5D) d7 = 0.0D;
                d8 = d5 - entity.posZ;
                if(flag = d6 * d6 + d7 * d7 + d8 * d8 < 0.0625D)
                    flag = !worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().shrink(0.0625D)).isEmpty();
                if(flag) {
                    entity.setPositionAndRotation(d0, d1, d2, f, f1);
                    this.player.setPositionAndRotation(d0, d1, d2, this.player.rotationYaw, this.player.rotationPitch);
                    netManager.sendPacketE(new SPacketMoveVehicle(entity));
                    return;
                }
            }
            updateMoving();
            this.player.addMovementStat(this.player.posX - d0, this.player.posY - d1, this.player.posZ - d2);
            this.vehicleFloating = d11 >= -0.03125D && !this.server.isFlightAllowed() && !worldserver.checkBlockCollision(entity.getEntityBoundingBox().grow(0.0625D).expand(0.0D, -0.55D, 0.0D));
            this.lowestRiddenX1 = entity.posX;
            this.lowestRiddenY1 = entity.posY;
            this.lowestRiddenZ1 = entity.posZ;
        }
    }


    /**
     * @author Te4hno
     */
    public void processConfirmTeleport(CPacketConfirmTeleport packetIn) {
        if (packetIn.getTeleportId() == this.teleportId) {
            Runnable task = new Runnable() {
                private boolean flag = true;
                @Override
                public void run() {
                    Vec3d pos = targetPos;
                    if(pos==null){
                        if(flag){
                            flag = false;
                            addToQueue(this);
                        }
                        return;
                    }
                    player.setPositionAndRotation(
                            targetPos.x, targetPos.y, targetPos.z,
                            player.rotationYaw, player.rotationPitch
                    );
                    if (player.isInvulnerableDimensionChange()) {
                        lastGoodX = targetPos.x;
                        lastGoodY = targetPos.y;
                        lastGoodZ = targetPos.z;
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
        if (info.getPurpose() == CPacketRecipeInfo.Purpose.SHOWN) {
            addToQueue(() -> {
                this.player.getRecipeBook().markSeen(info.getRecipe());
            });
        } else if (info.getPurpose() == CPacketRecipeInfo.Purpose.SETTINGS) {
            this.player.getRecipeBook().setGuiOpen(info.isGuiOpen());
            this.player.getRecipeBook().setFilteringCraftable(info.isFilteringCraftable());
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

            if (advancement != null) {
                this.player.getAdvancements().setSelectedTab(advancement);
            }
        }
    }


    /**
     * @author Te4hno
     */
    public void handleAnimation(@NotNull CPacketAnimation packetIn) {
        this.player.markPlayerActive();
        addToQueue(() -> {
            this.player.swingArm(packetIn.getHand());
        });
    }


    /**
     * @author Te4hno
     */
    //async
    public void processChatMessage(@NotNull CPacketChatMessage packetIn) {
        processChatMessage0(packetIn);
        //packetQueue.add(() -> processChatMessage0(packetIn));
    }


    private void processChatMessage0(CPacketChatMessage packetIn) {
        if (this.player.getChatVisibility() == EntityPlayer.EnumChatVisibility.HIDDEN) {
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
            this.player.markPlayerActive();

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
    public void processEntityAction(@NotNull CPacketEntityAction packetIn) {
        this.player.markPlayerActive();
        addToQueue(() -> {
            switch (packetIn.getAction()) {
                case START_SNEAKING:
                    this.player.setSneaking(true);
                    break;
                case STOP_SNEAKING:
                    this.player.setSneaking(false);
                    break;
                case START_SPRINTING:
                    this.player.setSprinting(true);
                    break;
                case STOP_SPRINTING:
                    this.player.setSprinting(false);
                    break;
                case STOP_SLEEPING:
                    if (this.player.isPlayerSleeping()) {
                        this.player.wakeUpPlayer(false, true, true);
                        this.targetPos = new Vec3d(this.player.posX, this.player.posY, this.player.posZ);
                    }
                    break;
                case START_RIDING_JUMP:
                    if (this.player.getRidingEntity() instanceof IJumpingMount) {
                        IJumpingMount mount = (IJumpingMount)this.player.getRidingEntity();
                        int i = packetIn.getAuxData();
                        if (mount.canJump() && i > 0) {
                            mount.handleStartJump(i);
                        }
                    }
                    break;
                case STOP_RIDING_JUMP:
                    if (this.player.getRidingEntity() instanceof IJumpingMount) {
                        IJumpingMount ijumpingmount = (IJumpingMount)this.player.getRidingEntity();
                        ijumpingmount.handleStopJump();
                    }
                    break;
                case OPEN_INVENTORY:
                    if (this.player.getRidingEntity() instanceof AbstractHorse) {
                        ((AbstractHorse)this.player.getRidingEntity()).openGUI(this.player);
                    }
                    break;
                case START_FALL_FLYING:
                    if (!this.player.onGround && this.player.motionY < 0.0D && !this.player.isElytraFlying() && !this.player.isInWater()) {
                        ItemStack itemstack = this.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

                        if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack)) {
                            this.player.setElytraFlying();
                        }
                    } else {
                        this.player.clearElytraFlying();
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid client command!");
            }
        });
    }


    /**
     * @author Te4hno
     */
    public void processUseEntity(@NotNull CPacketUseEntity packetIn) {
        WorldServer worldserver = this.server.getWorld(this.player.dimension);
        this.player.markPlayerActive();
        Entity entity = packetIn.getEntityFromWorld(worldserver);
        if (entity != null) {
            if (this.player.getDistanceSq(entity) < (this.player.canEntityBeSeen(entity) ? 36 : 9)) {
                addToQueue(() -> {
                    if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT) {
                        EnumHand enumhand = packetIn.getHand();
                        this.player.interactOn(entity, enumhand);
                    } else if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                        EnumHand enumhand1 = packetIn.getHand();
                        if (ForgeHooks.onInteractEntityAt(player, entity, packetIn.getHitVec(), enumhand1) != null)
                            return;
                        entity.applyPlayerInteraction(this.player, packetIn.getHitVec(), enumhand1);
                    } else if (packetIn.getAction() == CPacketUseEntity.Action.ATTACK) {
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
    public void processClickWindow(@NotNull CPacketClickWindow packetIn) {
        this.player.markPlayerActive();
        if (this.player.openContainer.windowId == packetIn.getWindowId() && this.player.openContainer.getCanCraft(this.player)) {
            if (this.player.isSpectator()) {
                NonNullList<ItemStack> list = NonNullList.create();

                for (int i = 0; i < this.player.openContainer.inventorySlots.size(); ++i) {
                    list.add(this.player.openContainer.inventorySlots.get(i).getStack());
                }

                this.player.sendAllContents(this.player.openContainer, list);
            } else {
                addToQueue(() -> {
                    ItemStack itemStack = this.player.openContainer.slotClick(packetIn.getSlotId(), packetIn.getUsedButton(), packetIn.getClickType(), this.player);

                    if (ItemStack.areItemStacksEqualUsingNBTShareTag(packetIn.getClickedItem(), itemStack)) {
                        netManager.sendPacketE(new SPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
                        this.player.isChangingQuantityOnly = true;
                        this.player.openContainer.detectAndSendChanges();
                        this.player.updateHeldItem();
                        this.player.isChangingQuantityOnly = false;
                    } else {
                        this.pendingTransactions.addKey(this.player.openContainer.windowId, packetIn.getActionNumber());
                        netManager.sendPacketE(new SPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), false));
                        this.player.openContainer.setCanCraft(this.player, false);
                        NonNullList<ItemStack> list = NonNullList.create();

                        for (int j = 0; j < this.player.openContainer.inventorySlots.size(); ++j) {
                            ItemStack itemstack = this.player.openContainer.inventorySlots.get(j).getStack();
                            ItemStack itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
                            list.add(itemstack1);
                        }
                        this.player.sendAllContents(this.player.openContainer, list);
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
        addToQueue(() -> {
            if (!this.player.isSpectator() && this.player.openContainer.windowId == packet.func_194318_a() && this.player.openContainer.getCanCraft(this.player)) {
                this.field_194309_H.func_194327_a(this.player, packet.func_194317_b(), packet.func_194319_c());
            }
        });
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


    /**
     * @author Te4hno
     */
    public void processConfirmTransaction(@NotNull CPacketConfirmTransaction packetIn) {
        Short oshort = this.pendingTransactions.lookup(this.player.openContainer.windowId);
        if (oshort != null && packetIn.getUid() == oshort.shortValue() &&
                this.player.openContainer.windowId == packetIn.getWindowId()
                && !this.player.openContainer.getCanCraft(this.player) && !this.player.isSpectator()) {

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
    public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn) {
        WorldServer worldserver = server.getWorld(player.dimension);
        BlockPos blockpos = packetIn.getPos();
        player.markPlayerActive();

        if (blockpos.getY() < this.server.getBuildLimit() - 1 || packetIn.getDirection() != EnumFacing.UP && blockpos.getY() < server.getBuildLimit()) {
            double dist = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 3;
            dist *= dist;
            if (targetPos == null && player.getDistanceSq(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5) < dist &&
                    !server.isBlockProtected(worldserver, blockpos, player)
                    && worldserver.getWorldBorder().contains(blockpos)) {

                addToQueue(() -> {
                    player.interactionManager.processRightClickBlock(
                            player, worldserver, player.getHeldItem(packetIn.getHand()),
                            packetIn.getHand(), blockpos, packetIn.getDirection(), packetIn.getFacingX(),
                            packetIn.getFacingY(), packetIn.getFacingZ()
                    );
                    netManager.exec(() -> afterPlace(worldserver, blockpos, packetIn.getDirection()));
                });
            }
        } else {
            TextComponentTranslation msg = new TextComponentTranslation("build.tooHigh", this.server.getBuildLimit());
            msg.getStyle().setColor(TextFormatting.RED);
            sendPacket(new SPacketChat(msg, ChatType.GAME_INFO));

            afterPlace(worldserver, blockpos, packetIn.getDirection());
        }
    }


    private void afterPlace(WorldServer worldserver, BlockPos blockpos, EnumFacing facing){
        netManager.sendPacketSync(new SPacketBlockChange(worldserver, blockpos));
        netManager.sendPacketSync(new SPacketBlockChange(worldserver, blockpos.offset(facing)));
    }


    /**
     * @author Te4hno
     */
    public void processTryUseItem(CPacketPlayerTryUseItem packetIn) {
        //todo fix glitch click to unloaded air
        EnumHand enumhand = packetIn.getHand();
        ItemStack itemstack = this.player.getHeldItem(enumhand);
        if (!itemstack.isEmpty()) {
            this.player.markPlayerActive();
            addToQueue(() -> {
                this.player.interactionManager.processRightClick(
                        this.player, this.server.getWorld(this.player.dimension),
                        itemstack, enumhand
                );
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
        for (WorldServer worldserver : this.server.worlds) {
            if (worldserver != null) {
                entity = packetIn.getEntity(worldserver);
                if (entity != null) {
                    break;
                }
            }
        }
        if (entity != null) {
            this.player.setSpectatingEntity(this.player);
            this.player.dismountRidingEntity();

            if (entity.world == this.player.world) {
                this.player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
            } else if (net.minecraftforge.common.ForgeHooks.onTravelToDimension(this.player, entity.dimension)) {
                int prevDimension = this.player.dimension;
                WorldServer worldserver1 = this.player.getServerWorld();
                WorldServer worldserver2 = (WorldServer) entity.world;
                this.player.dimension = entity.dimension;
                netManager.sendPacketE(new SPacketRespawn(this.player.dimension, worldserver2.getDifficulty(), worldserver2.getWorldInfo().getTerrainType(), this.player.interactionManager.getGameType())); // Forge: Use new dimensions information
                this.server.getPlayerList().updatePermissionLevel(this.player);
                worldserver1.removeEntityDangerously(this.player);
                this.player.isDead = false;
                this.player.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

                if (this.player.isEntityAlive()) {
                    worldserver1.updateEntityWithOptionalForce(this.player, false);
                    worldserver2.spawnEntity(this.player);
                    worldserver2.updateEntityWithOptionalForce(this.player, false);
                }

                this.player.setWorld(worldserver2);
                this.server.getPlayerList().preparePlayer(this.player, worldserver1);
                this.player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
                this.player.interactionManager.setWorld(worldserver2);
                this.server.getPlayerList().updateTimeAndWeatherForPlayer(this.player, worldserver2);
                this.server.getPlayerList().syncPlayerInventory(this.player);
                net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(this.player, prevDimension, this.player.dimension);
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
    public void processCustomPayload(@NotNull CPacketCustomPayload packetIn){
        addToQueue(() -> processCustomPayload0(packetIn));
    }


    private void processCustomPayload0(@NotNull CPacketCustomPayload packetIn) {
        String s = packetIn.getChannelName();
        switch (s) {
            case "MC|BEdit":
                PacketBuffer packetbuffer = packetIn.getBufferData();
                try {
                    ItemStack itemstack = packetbuffer.readItemStack();
                    if (itemstack.isEmpty()) return;
                    if (!ItemWritableBook.isNBTValid(itemstack.getTagCompound()))
                        throw new IOException("Invalid book tag!");
                    ItemStack itemstack1 = this.player.getHeldItemMainhand();
                    if (itemstack1.isEmpty()) return;
                    if (itemstack.getItem() == Items.WRITABLE_BOOK && itemstack.getItem() == itemstack1.getItem()) {
                        itemstack1.setTagInfo("pages", itemstack.getTagCompound().getTagList("pages", 8));
                    }
                } catch (Exception exception6) {
                    LOGGER.error("Couldn't handle book info", (Throwable) exception6);
                }
                break;
            case "MC|BSign":
                PacketBuffer packetbuffer1 = packetIn.getBufferData();
                try {
                    ItemStack itemstack3 = packetbuffer1.readItemStack();
                    if (itemstack3.isEmpty()) return;
                    if (!ItemWrittenBook.validBookTagContents(itemstack3.getTagCompound()))
                        throw new IOException("Invalid book tag!");

                    ItemStack itemstack4 = this.player.getHeldItemMainhand();
                    if (itemstack4.isEmpty()) {
                        return;
                    }
                    if (itemstack3.getItem() == Items.WRITABLE_BOOK && itemstack4.getItem() == Items.WRITABLE_BOOK) {
                        ItemStack itemstack2 = new ItemStack(Items.WRITTEN_BOOK);
                        itemstack2.setTagInfo("author", new NBTTagString(this.player.getName()));
                        itemstack2.setTagInfo("title", new NBTTagString(itemstack3.getTagCompound().getString("title")));
                        NBTTagList nbttaglist = itemstack3.getTagCompound().getTagList("pages", 8);

                        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                            String s1 = nbttaglist.getStringTagAt(i);
                            ITextComponent itextcomponent = new TextComponentString(s1);
                            s1 = ITextComponent.Serializer.componentToJson(itextcomponent);
                            nbttaglist.set(i, new NBTTagString(s1));
                        }
                        itemstack2.setTagInfo("pages", nbttaglist);
                        this.player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemstack2);
                    }
                } catch (Exception exception7) {
                    LOGGER.error("Couldn't sign book", (Throwable) exception7);
                }
                break;
            case "MC|TrSel":
                try {
                    int k = packetIn.getBufferData().readInt();
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
                    this.player.sendMessage(new TextComponentTranslation("advMode.notEnabled"));
                    return;
                }

                if (!this.player.canUseCommandBlock()) {
                    this.player.sendMessage(new TextComponentTranslation("advMode.notAllowed"));
                    return;
                }

                PacketBuffer buf = packetIn.getBufferData();

                try {
                    int l = buf.readByte();
                    CommandBlockBaseLogic commandblockbaselogic1 = null;

                    if (l == 0) {
                        TileEntity tileentity = this.player.world.getTileEntity(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));

                        if (tileentity instanceof TileEntityCommandBlock) {
                            commandblockbaselogic1 = ((TileEntityCommandBlock) tileentity).getCommandBlockLogic();
                        }
                    } else if (l == 1) {
                        Entity entity = this.player.world.getEntityByID(buf.readInt());

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
                            commandblockbaselogic1.setLastOutput((ITextComponent) null);
                        }

                        commandblockbaselogic1.updateCommand();
                        this.player.sendMessage(new TextComponentTranslation("advMode.setCommand.success", s6));
                    }
                } catch (Exception exception4) {
                    LOGGER.error("Couldn't set command block", (Throwable) exception4);
                }
                break;
            case "MC|AutoCmd":
                if (!this.server.isCommandBlockEnabled()) {
                    this.player.sendMessage(new TextComponentTranslation("advMode.notEnabled"));
                    return;
                }

                if (!this.player.canUseCommandBlock()) {
                    this.player.sendMessage(new TextComponentTranslation("advMode.notAllowed"));
                    return;
                }

                PacketBuffer packetbuffer3 = packetIn.getBufferData();

                try {
                    CommandBlockBaseLogic commandblockbaselogic = null;
                    TileEntityCommandBlock tileentitycommandblock = null;
                    BlockPos blockpos1 = new BlockPos(packetbuffer3.readInt(), packetbuffer3.readInt(), packetbuffer3.readInt());
                    TileEntity tileentity2 = this.player.world.getTileEntity(blockpos1);

                    if (tileentity2 instanceof TileEntityCommandBlock) {
                        tileentitycommandblock = (TileEntityCommandBlock) tileentity2;
                        commandblockbaselogic = tileentitycommandblock.getCommandBlockLogic();
                    }

                    String s7 = packetbuffer3.readString(packetbuffer3.readableBytes());
                    boolean flag3 = packetbuffer3.readBoolean();
                    TileEntityCommandBlock.Mode mode = TileEntityCommandBlock.Mode.valueOf(packetbuffer3.readString(16));
                    boolean flag = packetbuffer3.readBoolean();
                    boolean flag1 = packetbuffer3.readBoolean();

                    if (commandblockbaselogic != null) {
                        EnumFacing enumfacing = this.player.world.getBlockState(blockpos1).getValue(BlockCommandBlock.FACING);

                        switch (mode) {
                            case SEQUENCE:
                                IBlockState iblockstate3 = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
                                this.player.world.setBlockState(blockpos1, iblockstate3.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, flag), 2);
                                break;
                            case AUTO:
                                IBlockState iblockstate2 = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
                                this.player.world.setBlockState(blockpos1, iblockstate2.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, flag), 2);
                                break;
                            case REDSTONE:
                                IBlockState iblockstate = Blocks.COMMAND_BLOCK.getDefaultState();
                                this.player.world.setBlockState(blockpos1, iblockstate.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, flag), 2);
                        }

                        tileentity2.validate();
                        this.player.world.setTileEntity(blockpos1, tileentity2);
                        commandblockbaselogic.setCommand(s7);
                        commandblockbaselogic.setTrackOutput(flag3);

                        if (!flag3) {
                            commandblockbaselogic.setLastOutput((ITextComponent) null);
                        }

                        tileentitycommandblock.setAuto(flag1);
                        commandblockbaselogic.updateCommand();

                        if (!net.minecraft.util.StringUtils.isNullOrEmpty(s7)) {
                            this.player.sendMessage(new TextComponentTranslation("advMode.setCommand.success", s7));
                        }
                    }
                } catch (Exception exception3) {
                    LOGGER.error("Couldn't set command block", (Throwable) exception3);
                }
                break;
            case "MC|Beacon":
                if (this.player.openContainer instanceof ContainerBeacon) {
                    try {
                        PacketBuffer packetbuffer4 = packetIn.getBufferData();
                        int i1 = packetbuffer4.readInt();
                        int k1 = packetbuffer4.readInt();
                        ContainerBeacon containerbeacon = (ContainerBeacon) this.player.openContainer;
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
                if (this.player.openContainer instanceof ContainerRepair) {
                    ContainerRepair containerrepair = (ContainerRepair) this.player.openContainer;

                    if (packetIn.getBufferData() != null && packetIn.getBufferData().readableBytes() >= 1) {
                        String s5 = ChatAllowedCharacters.filterAllowedCharacters(packetIn.getBufferData().readString(32767));

                        if (s5.length() <= 35) {
                            containerrepair.updateItemName(s5);
                        }
                    } else {
                        containerrepair.updateItemName("");
                    }
                }
                break;
            case "MC|Struct":
                if (!this.player.canUseCommandBlock()) {
                    return;
                }
                PacketBuffer packetbuffer5 = packetIn.getBufferData();
                try {
                    BlockPos blockpos = new BlockPos(packetbuffer5.readInt(), packetbuffer5.readInt(), packetbuffer5.readInt());
                    IBlockState iblockstate1 = this.player.world.getBlockState(blockpos);
                    TileEntity tileentity1 = this.player.world.getTileEntity(blockpos);

                    if (tileentity1 instanceof TileEntityStructure) {
                        TileEntityStructure tileentitystructure = (TileEntityStructure) tileentity1;
                        int l1 = packetbuffer5.readByte();
                        String s8 = packetbuffer5.readString(32);
                        tileentitystructure.setMode(TileEntityStructure.Mode.valueOf(s8));
                        tileentitystructure.setName(packetbuffer5.readString(64));
                        int i2 = MathHelper.clamp(packetbuffer5.readInt(), -32, 32);
                        int j2 = MathHelper.clamp(packetbuffer5.readInt(), -32, 32);
                        int k2 = MathHelper.clamp(packetbuffer5.readInt(), -32, 32);
                        tileentitystructure.setPosition(new BlockPos(i2, j2, k2));
                        int l2 = MathHelper.clamp(packetbuffer5.readInt(), 0, 32);
                        int i3 = MathHelper.clamp(packetbuffer5.readInt(), 0, 32);
                        int j = MathHelper.clamp(packetbuffer5.readInt(), 0, 32);
                        tileentitystructure.setSize(new BlockPos(l2, i3, j));
                        String s2 = packetbuffer5.readString(32);
                        tileentitystructure.setMirror(Mirror.valueOf(s2));
                        String s3 = packetbuffer5.readString(32);
                        tileentitystructure.setRotation(Rotation.valueOf(s3));
                        tileentitystructure.setMetadata(packetbuffer5.readString(128));
                        tileentitystructure.setIgnoresEntities(packetbuffer5.readBoolean());
                        tileentitystructure.setShowAir(packetbuffer5.readBoolean());
                        tileentitystructure.setShowBoundingBox(packetbuffer5.readBoolean());
                        tileentitystructure.setIntegrity(MathHelper.clamp(packetbuffer5.readFloat(), 0.0F, 1.0F));
                        tileentitystructure.setSeed(packetbuffer5.readVarLong());
                        String s4 = tileentitystructure.getName();

                        if (l1 == 2) {
                            if (tileentitystructure.save()) {
                                this.player.sendStatusMessage(new TextComponentTranslation("structure_block.save_success", s4), false);
                            } else {
                                this.player.sendStatusMessage(new TextComponentTranslation("structure_block.save_failure", s4), false);
                            }
                        } else if (l1 == 3) {
                            if (!tileentitystructure.isStructureLoadable()) {
                                this.player.sendStatusMessage(new TextComponentTranslation("structure_block.load_not_found", s4), false);
                            } else if (tileentitystructure.load()) {
                                this.player.sendStatusMessage(new TextComponentTranslation("structure_block.load_success", s4), false);
                            } else {
                                this.player.sendStatusMessage(new TextComponentTranslation("structure_block.load_prepare", s4), false);
                            }
                        } else if (l1 == 4) {
                            if (tileentitystructure.detectSize()) {
                                this.player.sendStatusMessage(new TextComponentTranslation("structure_block.size_success", s4), false);
                            } else {
                                this.player.sendStatusMessage(new TextComponentTranslation("structure_block.size_failure"), false);
                            }
                        }

                        tileentitystructure.markDirty();
                        this.player.world.notifyBlockUpdate(blockpos, iblockstate1, iblockstate1, 3);
                    }
                } catch (Exception exception1) {
                    LOGGER.error("Couldn't set structure block", (Throwable) exception1);
                }
                break;
            case "MC|PickItem":
                PacketBuffer packetbuffer6 = packetIn.getBufferData();
                try {
                    int j1 = packetbuffer6.readVarInt();
                    this.player.inventory.pickItem(j1);
                    netManager.sendPacket(new SPacketSetSlot(-2, this.player.inventory.currentItem, this.player.inventory.getStackInSlot(this.player.inventory.currentItem)));
                    netManager.sendPacket(new SPacketSetSlot(-2, j1, this.player.inventory.getStackInSlot(j1)));
                    netManager.sendPacket(new SPacketHeldItemChange(this.player.inventory.currentItem));
                } catch (Exception exception) {
                    LOGGER.error("Couldn't pick item", exception);
                }
                break;
        }
    }


    /**
     * @author Te4hno
     */
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<SPacketPlayerPosLook.EnumFlags> relativeSet) {
        double d0 = relativeSet.contains(SPacketPlayerPosLook.EnumFlags.X) ? this.player.posX : 0.0D;
        double d1 = relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Y) ? this.player.posY : 0.0D;
        double d2 = relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Z) ? this.player.posZ : 0.0D;
        this.targetPos = new Vec3d(x + d0, y + d1, z + d2);
        float f = yaw;
        float f1 = pitch;

        if (relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
            f = yaw + this.player.rotationYaw;
        }

        if (relativeSet.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
            f1 = pitch + this.player.rotationPitch;
        }

        if (++this.teleportId == Integer.MAX_VALUE) {
            this.teleportId = 0;
        }

        this.lastPositionUpdate = this.networkTickCount;
        this.player.setPositionAndRotation(this.targetPos.x, this.targetPos.y, this.targetPos.z, f, f1);
        this.player.connection.netManager.sendPacketE(new SPacketPlayerPosLook(x, y, z, yaw, pitch, relativeSet, this.teleportId));
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
