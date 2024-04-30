package net.minecraft.entity;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityEvokerFangs;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.tson.utl.Te4HashSet;
import javax.annotation.Nullable;
import static plus.fserver.utls.math.MathUtls.F_256_T_360;


public class EntityTrackerEntry {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Entity trackedEntity;
    private final int range;
    private int maxRange;
    private final int updateFrequency;
    private long encodedPosX;
    private long encodedPosY;
    private long encodedPosZ;
    private int encodedRotationYaw;
    private int encodedRotationPitch;
    private int lastHeadMotion;
    private double lastTrackedEntityMotionX;
    private double lastTrackedEntityMotionY;
    private double motionZ;
    public int updateCounter;
    private double lastTrackedEntityPosX;
    private double lastTrackedEntityPosY;
    private double lastTrackedEntityPosZ;
    private boolean updatedPlayerVisibility;
    private final boolean sendVelocityUpdates;
    private int ticksSinceLastForcedTeleport;
    private List<Entity> passengers = Collections.emptyList();
    private boolean ridingEntity;
    private boolean onGround;
    public boolean playerEntitiesUpdated;
    public final Set<EntityPlayerMP> trackingPlayers = new Te4HashSet<>(4);//not edit this!!!

    public EntityTrackerEntry(Entity entityIn, int rangeIn, int maxRangeIn, int updateFrequencyIn, boolean sendVelocityUpdatesIn) {
        this.trackedEntity = entityIn;
        this.range = rangeIn;
        this.maxRange = maxRangeIn;
        this.updateFrequency = updateFrequencyIn;
        this.sendVelocityUpdates = sendVelocityUpdatesIn;
        this.encodedPosX = EntityTracker.getPositionLong(entityIn.posX);
        this.encodedPosY = EntityTracker.getPositionLong(entityIn.posY);
        this.encodedPosZ = EntityTracker.getPositionLong(entityIn.posZ);
        this.encodedRotationYaw = MathHelper.floor(entityIn.rotationYaw * F_256_T_360);
        this.encodedRotationPitch = MathHelper.floor(entityIn.rotationPitch * F_256_T_360);
        this.lastHeadMotion = MathHelper.floor(entityIn.getRotationYawHead() * F_256_T_360);
        this.onGround = entityIn.onGround;
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof EntityTrackerEntry) {
            return ((EntityTrackerEntry)other).trackedEntity.getEntityId() == this.trackedEntity.getEntityId();
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return this.trackedEntity.getEntityId();
    }


    private void sendMetadata() {
        EntityDataManager entitydatamanager = this.trackedEntity.getDataManager();
        if (entitydatamanager.isDirty()) {
            this.sendToTrackingAndSelf(new SPacketEntityMetadata(this.trackedEntity.getEntityId(), entitydatamanager, false));
        }

        if (this.trackedEntity instanceof EntityLivingBase) {
            AttributeMap attributemap = (AttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMap();
            Set<IAttributeInstance> set = attributemap.getDirtyInstances();
            if (!set.isEmpty()) {
                this.sendToTrackingAndSelf(new SPacketEntityProperties(this.trackedEntity.getEntityId(), set));
            }
            set.clear();
        }
    }


    public void sendToTrackingAndSelf(Packet<?> packetIn) {
        this.sendPacketToTrackedPlayers(packetIn);
        if (this.trackedEntity instanceof EntityPlayerMP) {
            ((EntityPlayerMP)this.trackedEntity).connection.netManager.sendPacketE(packetIn);
        }
    }


    public void sendDestroyEntityPacketToTrackedPlayers() {
        for (EntityPlayerMP entityplayermp : this.trackingPlayers) {
            this.trackedEntity.removeTrackingPlayer(entityplayermp);
            entityplayermp.removeEntity(this.trackedEntity);
        }
    }


    public void removeFromTrackedPlayers(EntityPlayerMP playerMP) {
        if (this.trackingPlayers.remove(playerMP)) {
            this.trackedEntity.removeTrackingPlayer(playerMP);
            playerMP.removeEntity(this.trackedEntity);
        }
    }


    public void updatePlayerEntity(EntityPlayerMP playerMP) {
        if (playerMP != this.trackedEntity) {
            if (this.isVisibleTo(playerMP)) {
                if (!this.trackingPlayers.contains(playerMP) && (this.isPlayerWatchingThisChunk(playerMP) || this.trackedEntity.forceSpawn)) {
                    this.trackingPlayers.add(playerMP);
                    Packet<?> packet = this.createSpawnPacket();

                    if (!this.trackedEntity.getDataManager().isEmpty()) {
                        playerMP.connection.netManager.exec(() -> {
                            SPacketEntityMetadata packet2 = new SPacketEntityMetadata(trackedEntity.getEntityId(), trackedEntity.getDataManager(), true);
                            playerMP.connection.netManager.sendPacketSync(packet);
                            playerMP.connection.netManager.sendPacketSync(packet2);
                        });
                    } else {
                        playerMP.connection.netManager.sendPacketE(packet);
                    }

                    boolean flag = this.sendVelocityUpdates;
                    boolean isBase = this.trackedEntity instanceof EntityLivingBase;

                    if (isBase) {
                        AttributeMap attributemap = (AttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMap();
                        Collection<IAttributeInstance> collection = attributemap.getWatchedAttributes();

                        if (!collection.isEmpty()) {
                            playerMP.connection.netManager.sendPacketE(new SPacketEntityProperties(this.trackedEntity.getEntityId(), collection));
                        }

                        if (((EntityLivingBase)this.trackedEntity).isElytraFlying()) {
                            flag = true;
                        }
                    }

                    this.lastTrackedEntityMotionX = this.trackedEntity.motionX;
                    this.lastTrackedEntityMotionY = this.trackedEntity.motionY;
                    this.motionZ = this.trackedEntity.motionZ;

                    if (flag && !(packet instanceof SPacketSpawnMob)) {
                        playerMP.connection.netManager.sendPacketE(new SPacketEntityVelocity(this.trackedEntity.getEntityId(), this.trackedEntity.motionX, this.trackedEntity.motionY, this.trackedEntity.motionZ));
                    }

                    if (isBase) {
                        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                            ItemStack itemstack = ((EntityLivingBase)this.trackedEntity).getItemStackFromSlot(slot);

                            if (!itemstack.isEmpty()) {
                                playerMP.connection.netManager.sendPacketE(new SPacketEntityEquipment(this.trackedEntity.getEntityId(), slot, itemstack));
                            }
                        }

                        if (this.trackedEntity instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer)this.trackedEntity;

                            if (entityplayer.isPlayerSleeping()) {
                                playerMP.connection.netManager.sendPacketE(new SPacketUseBed(entityplayer, new BlockPos(this.trackedEntity)));
                            }
                        }

                        EntityLivingBase entitylivingbase = (EntityLivingBase)this.trackedEntity;

                        for (PotionEffect potioneffect : entitylivingbase.getActivePotionEffects()) {
                            playerMP.connection.netManager.sendPacketE(new SPacketEntityEffect(this.trackedEntity.getEntityId(), potioneffect));
                        }
                    }

                    if (!this.trackedEntity.getPassengers().isEmpty()) {
                        playerMP.connection.netManager.sendPacketE(new SPacketSetPassengers(this.trackedEntity));
                    }

                    if (this.trackedEntity.isRiding()) {
                        playerMP.connection.netManager.sendPacketE(new SPacketSetPassengers(this.trackedEntity.getRidingEntity()));
                    }

                    this.trackedEntity.addTrackingPlayer(playerMP);
                    playerMP.addEntity(this.trackedEntity);
                    this.trackedEntity.world.startTrackingEvent.post(playerMP, trackedEntity);
                    //ForgeEventFactory.onStartEntityTracking(trackedEntity, playerMP);
                }
            } else if (this.trackingPlayers.remove(playerMP)) {
                this.trackedEntity.removeTrackingPlayer(playerMP);
                playerMP.removeEntity(this.trackedEntity);
                this.trackedEntity.world.stopTrackingEvent.post(playerMP, trackedEntity);
                //ForgeEventFactory.onStopEntityTracking(trackedEntity, playerMP);
            }
        }
    }


    public boolean isVisibleTo(EntityPlayerMP playerMP) {
        double d0 = playerMP.posX - (double)this.encodedPosX * 0.000244140625; // 1/4096.0D = 0.000244140625
        double i = Math.min(this.range, this.maxRange);
        return d0 >= -i && d0 <= i &&
                (d0=playerMP.posZ - (double)this.encodedPosZ * 0.000244140625) >= -i && d0 <= i
                && this.trackedEntity.isSpectatedByPlayer(playerMP);
    }


    private boolean isPlayerWatchingThisChunk(EntityPlayerMP playerMP) {
        return playerMP.getServerWorld().getPlayerChunkMap().isPlayerWatchingChunk(playerMP, this.trackedEntity.chunkCoordX, this.trackedEntity.chunkCoordZ);
    }


    public void updatePlayerEntities(List<EntityPlayer> players) {
        for (int i = 0; i < players.size(); ++i) {
            this.updatePlayerEntity((EntityPlayerMP)players.get(i));
        }
    }


    private Packet<?> createSpawnPacket() {
        if (this.trackedEntity.isDead) {
            LOGGER.warn("Fetching addPacket for removed entity");
        }

        Packet pkt = FMLNetworkHandler.getEntitySpawningPacket(this.trackedEntity);
        if (pkt != null) return pkt;

        if (this.trackedEntity instanceof EntityPlayerMP) {
            return new SPacketSpawnPlayer((EntityPlayer)this.trackedEntity);
        } else if (this.trackedEntity instanceof IAnimals) {
            this.lastHeadMotion = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
            return new SPacketSpawnMob((EntityLivingBase)this.trackedEntity);
        } else if (this.trackedEntity instanceof EntityPainting) {
            return new SPacketSpawnPainting((EntityPainting)this.trackedEntity);
        } else if (this.trackedEntity instanceof EntityItem) {
            return new SPacketSpawnObject(this.trackedEntity, 2, 1);
        } else if (this.trackedEntity instanceof EntityMinecart) {
            EntityMinecart entityminecart = (EntityMinecart)this.trackedEntity;
            return new SPacketSpawnObject(this.trackedEntity, 10, entityminecart.getType().getId());
        } else if (this.trackedEntity instanceof EntityBoat) {
            return new SPacketSpawnObject(this.trackedEntity, 1);
        } else if (this.trackedEntity instanceof EntityXPOrb) {
            return new SPacketSpawnExperienceOrb((EntityXPOrb)this.trackedEntity);
        } else if (this.trackedEntity instanceof EntityFishHook) {
            Entity entity2 = ((EntityFishHook)this.trackedEntity).getAngler();
            return new SPacketSpawnObject(this.trackedEntity, 90, entity2 == null ? this.trackedEntity.getEntityId() : entity2.getEntityId());
        } else if (this.trackedEntity instanceof EntitySpectralArrow) {
            Entity entity1 = ((EntitySpectralArrow)this.trackedEntity).shootingEntity;
            return new SPacketSpawnObject(this.trackedEntity, 91, 1 + (entity1 == null ? this.trackedEntity.getEntityId() : entity1.getEntityId()));
        } else if (this.trackedEntity instanceof EntityTippedArrow) {
            Entity entity = ((EntityArrow)this.trackedEntity).shootingEntity;
            return new SPacketSpawnObject(this.trackedEntity, 60, 1 + (entity == null ? this.trackedEntity.getEntityId() : entity.getEntityId()));
        } else if (this.trackedEntity instanceof EntitySnowball) {
            return new SPacketSpawnObject(this.trackedEntity, 61);
        } else if (this.trackedEntity instanceof EntityLlamaSpit) {
            return new SPacketSpawnObject(this.trackedEntity, 68);
        } else if (this.trackedEntity instanceof EntityPotion) {
            return new SPacketSpawnObject(this.trackedEntity, 73);
        }
        else if (this.trackedEntity instanceof EntityExpBottle) {
            return new SPacketSpawnObject(this.trackedEntity, 75);
        } else if (this.trackedEntity instanceof EntityEnderPearl) {
            return new SPacketSpawnObject(this.trackedEntity, 65);
        } else if (this.trackedEntity instanceof EntityEnderEye) {
            return new SPacketSpawnObject(this.trackedEntity, 72);
        } else if (this.trackedEntity instanceof EntityFireworkRocket) {
            return new SPacketSpawnObject(this.trackedEntity, 76);
        } else if (this.trackedEntity instanceof EntityFireball) {
            EntityFireball entityfireball = (EntityFireball)this.trackedEntity;
            SPacketSpawnObject spacketspawnobject = null;
            int i = 63;

            if (this.trackedEntity instanceof EntitySmallFireball) {
                i = 64;
            }
            else if (this.trackedEntity instanceof EntityDragonFireball) {
                i = 93;
            }
            else if (this.trackedEntity instanceof EntityWitherSkull) {
                i = 66;
            }

            if (entityfireball.shootingEntity != null) {
                spacketspawnobject = new SPacketSpawnObject(this.trackedEntity, i, ((EntityFireball)this.trackedEntity).shootingEntity.getEntityId());
            } else {
                spacketspawnobject = new SPacketSpawnObject(this.trackedEntity, i, 0);
            }

            spacketspawnobject.setSpeedX((int)(entityfireball.accelerationX * 8000.0D));
            spacketspawnobject.setSpeedY((int)(entityfireball.accelerationY * 8000.0D));
            spacketspawnobject.setSpeedZ((int)(entityfireball.accelerationZ * 8000.0D));
            return spacketspawnobject;
        }
        else if (this.trackedEntity instanceof EntityShulkerBullet) {
            SPacketSpawnObject spacketspawnobject1 = new SPacketSpawnObject(this.trackedEntity, 67, 0);
            spacketspawnobject1.setSpeedX((int)(this.trackedEntity.motionX * 8000.0D));
            spacketspawnobject1.setSpeedY((int)(this.trackedEntity.motionY * 8000.0D));
            spacketspawnobject1.setSpeedZ((int)(this.trackedEntity.motionZ * 8000.0D));
            return spacketspawnobject1;
        } else if (this.trackedEntity instanceof EntityEgg) {
            return new SPacketSpawnObject(this.trackedEntity, 62);
        } else if (this.trackedEntity instanceof EntityEvokerFangs) {
            return new SPacketSpawnObject(this.trackedEntity, 79);
        } else if (this.trackedEntity instanceof EntityTNTPrimed) {
            return new SPacketSpawnObject(this.trackedEntity, 50);
        } else if (this.trackedEntity instanceof EntityEnderCrystal) {
            return new SPacketSpawnObject(this.trackedEntity, 51);
        } else if (this.trackedEntity instanceof EntityFallingBlock) {
            EntityFallingBlock entityfallingblock = (EntityFallingBlock)this.trackedEntity;
            return new SPacketSpawnObject(
                    this.trackedEntity, 70, Block.getStateId(entityfallingblock.getBlock())
            );
        } else if (this.trackedEntity instanceof EntityArmorStand) {
            return new SPacketSpawnObject(this.trackedEntity, 78);
        } else if (this.trackedEntity instanceof EntityItemFrame) {
            EntityItemFrame entityitemframe = (EntityItemFrame)this.trackedEntity;
            return new SPacketSpawnObject(this.trackedEntity, 71, entityitemframe.facingDirection.getHorizontalIndex(), entityitemframe.getHangingPosition());
        } else if (this.trackedEntity instanceof EntityLeashKnot) {
            EntityLeashKnot entityleashknot = (EntityLeashKnot)this.trackedEntity;
            return new SPacketSpawnObject(this.trackedEntity, 77, 0, entityleashknot.getHangingPosition());
        } else if (this.trackedEntity instanceof EntityAreaEffectCloud) {
            return new SPacketSpawnObject(this.trackedEntity, 3);
        } else {
            throw new IllegalArgumentException("Don't know how to add " + this.trackedEntity.getClass() + "!");
        }
    }


    public void removeTrackedPlayerSymmetric(EntityPlayerMP playerMP) {
        if (this.trackingPlayers.remove(playerMP)) {
            this.trackedEntity.removeTrackingPlayer(playerMP);
            playerMP.removeEntity(this.trackedEntity);
        }
    }


    public Entity getTrackedEntity() {
        return this.trackedEntity;
    }


    public void setMaxRange(int maxRangeIn) {
        this.maxRange = maxRangeIn;
    }


    public void resetPlayerVisibility() {
        this.updatedPlayerVisibility = false;
    }


    public void updatePlayerList(List<EntityPlayer> players) {
        if (this.playerEntitiesUpdated = (!this.updatedPlayerVisibility || this.trackedEntity.getDistanceSq(this.lastTrackedEntityPosX, this.lastTrackedEntityPosY, this.lastTrackedEntityPosZ) > 16.0D)) {
            this.lastTrackedEntityPosX = this.trackedEntity.posX;
            this.lastTrackedEntityPosY = this.trackedEntity.posY;
            this.lastTrackedEntityPosZ = this.trackedEntity.posZ;
            this.updatedPlayerVisibility = true;
            this.updatePlayerEntities(players);
        }

        List<Entity> list = this.trackedEntity.getPassengers();

        if(!(list.isEmpty() && this.passengers.isEmpty()))
            if (!list.equals(this.passengers)) {
                this.passengers = list;
                this.sendPacketToTrackedPlayers(new SPacketSetPassengers(this.trackedEntity));
            }
        NetworkManager selfManager = (this.trackedEntity instanceof EntityPlayerMP)?
                ((EntityPlayerMP)trackedEntity).connection.netManager:null;

        if (selfManager == null && this.trackedEntity instanceof EntityItemFrame && this.updateCounter % 10 == 0) {
            EntityItemFrame entityitemframe = (EntityItemFrame)this.trackedEntity;
            ItemStack itemstack = entityitemframe.getDisplayedItem();

            if (itemstack.getItem() instanceof ItemMap) {
                MapData mapdata = ((ItemMap) itemstack.getItem()).getMapData(itemstack, this.trackedEntity.world);

                for (EntityPlayer entityplayer : players) {
                    EntityPlayerMP entityplayermp = (EntityPlayerMP)entityplayer;
                    mapdata.updateVisiblePlayers(entityplayermp, itemstack);
                    Packet<?> packet = ((ItemMap) itemstack.getItem()).createMapDataPacket(itemstack, this.trackedEntity.world, entityplayermp);

                    if (packet != null)
                        entityplayermp.connection.netManager.sendPacketE(packet);
                }
            }
            this.sendMetadata0( null);
        }

        if (this.updateCounter % this.updateFrequency == 0 || this.trackedEntity.isAirBorne || this.trackedEntity.getDataManager().isDirty()) {
            if (this.trackedEntity.isRiding()) {
                int j1 = MathHelper.floor(this.trackedEntity.rotationYaw * F_256_T_360);
                int l1 = MathHelper.floor(this.trackedEntity.rotationPitch * F_256_T_360);

                if (Math.abs(j1 - this.encodedRotationYaw) >= 1 || Math.abs(l1 - this.encodedRotationPitch) >= 1) {
                    this.sendPacketToTrackedPlayers(new SPacketEntity.S16PacketEntityLook(this.trackedEntity.getEntityId(), (byte)j1, (byte)l1, this.trackedEntity.onGround));
                    this.encodedRotationYaw = j1;
                    this.encodedRotationPitch = l1;
                }

                this.encodedPosX = EntityTracker.getPositionLong(this.trackedEntity.posX);
                this.encodedPosY = EntityTracker.getPositionLong(this.trackedEntity.posY);
                this.encodedPosZ = EntityTracker.getPositionLong(this.trackedEntity.posZ);
                this.sendMetadata0(selfManager);
                this.ridingEntity = true;
            } else {
                ++this.ticksSinceLastForcedTeleport;
                long i1 = EntityTracker.getPositionLong(this.trackedEntity.posX);
                long i2 = EntityTracker.getPositionLong(this.trackedEntity.posY);
                long j2 = EntityTracker.getPositionLong(this.trackedEntity.posZ);
                int k2 = MathHelper.floor(this.trackedEntity.rotationYaw * F_256_T_360);
                int i = MathHelper.floor(this.trackedEntity.rotationPitch * F_256_T_360);
                long j = i1 - this.encodedPosX;
                long k = i2 - this.encodedPosY;
                long l = j2 - this.encodedPosZ;
                Packet<?> packet1 = null;
                boolean flag = j * j + k * k + l * l >= 128L || this.updateCounter % 60 == 0;
                boolean flag1 = Math.abs(k2 - this.encodedRotationYaw) >= 1 || Math.abs(i - this.encodedRotationPitch) >= 1;

                if (this.updateCounter > 0 || this.trackedEntity instanceof EntityArrow) {
                    if (j >= -32768L && j < 32768L && k >= -32768L && k < 32768L && l >= -32768L && l < 32768L && this.ticksSinceLastForcedTeleport <= 400 && !this.ridingEntity && this.onGround == this.trackedEntity.onGround) {
                        if ((!flag || !flag1) && !(this.trackedEntity instanceof EntityArrow)) {
                            if (flag) {
                                packet1 = new SPacketEntity.S15PacketEntityRelMove(this.trackedEntity.getEntityId(), j, k, l, this.trackedEntity.onGround);
                            } else if (flag1) {
                                packet1 = new SPacketEntity.S16PacketEntityLook(this.trackedEntity.getEntityId(), (byte)k2, (byte)i, this.trackedEntity.onGround);
                            }
                        } else {
                            packet1 = new SPacketEntity.S17PacketEntityLookMove(this.trackedEntity.getEntityId(), j, k, l, (byte)k2, (byte)i, this.trackedEntity.onGround);
                        }
                    } else {
                        this.onGround = this.trackedEntity.onGround;
                        this.ticksSinceLastForcedTeleport = 0;
                        this.resetPlayerVisibility();
                        packet1 = new SPacketEntityTeleport(this.trackedEntity);
                    }
                }

                boolean flag2 = this.sendVelocityUpdates || (this.trackedEntity instanceof EntityLivingBase && ((EntityLivingBase)this.trackedEntity).isElytraFlying());


                if (flag2 && this.updateCounter > 0) {
                    double d0 = this.trackedEntity.motionX - this.lastTrackedEntityMotionX;
                    double d1 = this.trackedEntity.motionY - this.lastTrackedEntityMotionY;
                    double d2 = this.trackedEntity.motionZ - this.motionZ;
                    double d4 = d0 * d0 + d1 * d1 + d2 * d2;

                    if (d4 > 4.0E-4D || d4 > 0.0D && this.trackedEntity.motionX == 0.0D && this.trackedEntity.motionY == 0.0D && this.trackedEntity.motionZ == 0.0D) {
                        this.lastTrackedEntityMotionX = this.trackedEntity.motionX;
                        this.lastTrackedEntityMotionY = this.trackedEntity.motionY;
                        this.motionZ = this.trackedEntity.motionZ;
                        this.sendPacketToTrackedPlayers(new SPacketEntityVelocity(this.trackedEntity.getEntityId(), this.lastTrackedEntityMotionX, this.lastTrackedEntityMotionY, this.motionZ));
                    }
                }
                if (packet1 != null) {
                    this.sendPacketToTrackedPlayers(packet1);
                }

                this.sendMetadata0(selfManager);

                if (flag) {
                    this.encodedPosX = i1;
                    this.encodedPosY = i2;
                    this.encodedPosZ = j2;
                }

                if (flag1) {
                    this.encodedRotationYaw = k2;
                    this.encodedRotationPitch = i;
                }
                this.ridingEntity = false;
            }

            int k1 = MathHelper.floor(this.trackedEntity.getRotationYawHead() * F_256_T_360);

            if (Math.abs(k1 - this.lastHeadMotion) >= 1) {
                this.sendPacketToTrackedPlayers(new SPacketEntityHeadLook(this.trackedEntity, (byte)k1));
                this.lastHeadMotion = k1;
            }
            this.trackedEntity.isAirBorne = false;
        }

        ++this.updateCounter;

        if (this.trackedEntity.velocityChanged) {
            Packet<?> packet = new SPacketEntityVelocity(this.trackedEntity);
            if(selfManager != null){
                sendPacketToTrackedPlayersAndSelf(selfManager, packet);
            } else {
                sendPacketToTrackedPlayers(packet);
            }
            this.trackedEntity.velocityChanged = false;
        }
    }


    public void sendPacketToTrackedPlayersAndSelf(NetworkManager manager, Packet<?> packetIn){
        manager.addToQueueE(packetIn);
        sendPacketToTrackedPlayers(packetIn);
    }


    public void sendPacketToTrackedPlayers(Packet<?> packetIn){
        if(trackingPlayers.isEmpty())return;
        Te4HashSet<EntityPlayerMP>.KeyIterator iter = (Te4HashSet<EntityPlayerMP>.KeyIterator)
                this.trackingPlayers.iterator();

        EntityPlayerMP mp = iter.nextIfCan();
        while (mp != null){
            mp.connection.netManager.addToQueueE(packetIn);
            mp = iter.nextIfCan();
        }
    }


    private void sendMetadata0(@Nullable NetworkManager manager) {
        EntityDataManager entitydatamanager = this.trackedEntity.getDataManager();
        Packet<?> packet;
        if (entitydatamanager.isDirty()) {
            packet = new SPacketEntityMetadata(this.trackedEntity.getEntityId(), entitydatamanager, false);

            if(manager == null) sendPacketToTrackedPlayers(packet);
            else sendPacketToTrackedPlayersAndSelf(manager, packet);
        }
        if (manager != null || this.trackedEntity instanceof EntityLivingBase) {
            AttributeMap attributemap = (AttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMapOrNull();
            if (attributemap != null) {
                Set<IAttributeInstance> set = attributemap.getDirtyInstances();

                if (!set.isEmpty()) {
                    packet = new SPacketEntityProperties(this.trackedEntity.getEntityId(), set);

                    if (manager == null) sendPacketToTrackedPlayers(packet);
                    else sendPacketToTrackedPlayersAndSelf(manager, packet);

                    set.clear();
                }
            }
        }
    }
}