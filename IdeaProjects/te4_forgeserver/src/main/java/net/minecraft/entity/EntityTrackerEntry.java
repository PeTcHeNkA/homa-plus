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
import plus.fserver.ElcaConfig;
import plus.fserver.server.network.packet.SElcaPacketEntityEquipment;
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
    public double lastTrackedEntityPosX;
    public double lastTrackedEntityPosY;
    public double lastTrackedEntityPosZ;
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
        this.maxRange = Math.min(range, maxRangeIn);
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
        if (other instanceof EntityTrackerEntry)
            return ((EntityTrackerEntry)other).trackedEntity.getEntityId() == this.trackedEntity.getEntityId();
        else
            return false;

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
            if (!set.isEmpty())
                this.sendToTrackingAndSelf(new SPacketEntityProperties(this.trackedEntity.getEntityId(), set));

            set.clear();
        }
    }


    public void sendToTrackingAndSelf(Packet<?> packetIn) {
        this.sendPacketToTrackedPlayers(packetIn);
        if (this.trackedEntity instanceof EntityPlayerMP)
            ((EntityPlayerMP)this.trackedEntity).connection.netManager.sendPacketE(packetIn);
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
                Entity trackedEntity = this.trackedEntity;
                if (!this.trackingPlayers.contains(playerMP) && (this.isPlayerWatchingThisChunk(playerMP) || this.trackedEntity.forceSpawn)) {
                    this.trackingPlayers.add(playerMP);
                    Packet<?> packet = this.createSpawnPacket();

                    if (!trackedEntity.getDataManager().isEmpty()) {
                        playerMP.connection.netManager.exec(() -> {
                            SPacketEntityMetadata packet2 = new SPacketEntityMetadata(trackedEntity.getEntityId(), trackedEntity.getDataManager(), true);
                            playerMP.connection.netManager.sendPacketSync(packet);
                            playerMP.connection.netManager.sendPacketSync(packet2);
                        });
                    } else {
                        playerMP.connection.netManager.sendPacketE(packet);
                    }

                    boolean flag = this.sendVelocityUpdates;

                    if (trackedEntity.isLiving()) {
                        AttributeMap attributemap = (AttributeMap)((EntityLivingBase)trackedEntity).getAttributeMap();
                        Collection<IAttributeInstance> collection = attributemap.getWatchedAttributes();

                        if (!collection.isEmpty()) {
                            playerMP.connection.netManager.sendPacketE(new SPacketEntityProperties(trackedEntity.getEntityId(), collection));
                        }

                        if (((EntityLivingBase)trackedEntity).isElytraFlying()) {
                            flag = true;
                        }
                    }

                    this.lastTrackedEntityMotionX = trackedEntity.motionX;
                    this.lastTrackedEntityMotionY = trackedEntity.motionY;
                    this.motionZ = trackedEntity.motionZ;

                    if (flag && !(packet instanceof SPacketSpawnMob)) {
                        playerMP.connection.netManager.sendPacketE(new SPacketEntityVelocity(trackedEntity.getEntityId(), trackedEntity.motionX, trackedEntity.motionY, trackedEntity.motionZ));
                    }

                    if (trackedEntity.isLiving()) {
                        SElcaPacketEntityEquipment equp = null, next = null;

                        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                            ItemStack itemstack = ((EntityLivingBase)trackedEntity).getItemStackFromSlot(slot);

                            if (!itemstack.isEmpty()) {
                                if(equp == null)equp = next = new SElcaPacketEntityEquipment(trackedEntity.getEntityId(), slot, itemstack);
                                else next = next.with(trackedEntity.getEntityId(), slot, itemstack);
                            }
                        }
                        //TODO UNCHECKED
                        if(equp != null){
                            //if(equp == next){
                            playerMP.connection.netManager.sendPacketE(equp);
//                            } else {
//                                SElcaPacketEntityEquipment finalEqup = equp;
//                                playerMP.connection.netManager.exec(() -> {
//                                    SElcaPacketEntityEquipment temp = finalEqup;
//                                    while (temp != null){
//                                        playerMP.connection.netManager.sendPacketSync(temp);
//                                        temp = temp.next();
//                                    }
//                                });
//                            }
                        }

                        if (trackedEntity instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer)trackedEntity;

                            if (entityplayer.isPlayerSleeping()) {
                                playerMP.connection.netManager.sendPacketE(new SPacketUseBed(entityplayer, new BlockPos(trackedEntity)));
                            }
                        }

                        EntityLivingBase entitylivingbase = (EntityLivingBase)trackedEntity;

                        for (PotionEffect potioneffect : entitylivingbase.getActivePotionEffects()) {
                            playerMP.connection.netManager.sendPacketE(new SPacketEntityEffect(trackedEntity.getEntityId(), potioneffect));
                        }
                    }

                    if (!trackedEntity.getPassengers().isEmpty()) {
                        playerMP.connection.netManager.sendPacketE(new SPacketSetPassengers(trackedEntity));
                    }

                    if (trackedEntity.isRiding()) {
                        playerMP.connection.netManager.sendPacketE(new SPacketSetPassengers(trackedEntity.getRidingEntity()));
                    }

                    trackedEntity.addTrackingPlayer(playerMP);
                    playerMP.addEntity(trackedEntity);
                    trackedEntity.world.startTrackingEvent.post(playerMP, trackedEntity);
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

    public boolean isVisibleTo(EntityPlayerMP player) {
        Entity trackedEntity;
        double distance = (trackedEntity = this.trackedEntity).getDistanceSq(player.posX, player.posY, player.posZ);
        if(distance < trackedEntity.elcaMinRange)trackedEntity.elcaMinRange = distance;
        return distance >= -maxRange || distance <= -distance && trackedEntity.isSpectatedByPlayer(player);
        //return distance >= -maxRange && distance <= maxRange && trackedEntity.isSpectatedByPlayer(player);

//        double d0 = player.posX - (double)this.encodedPosX * 0.000244140625; // 1/4096.0D = 0.000244140625
//        double i = Math.min(this.range, this.maxRange);
//        return d0 >= -i && d0 <= i &&
//                (d0=player.posZ - (double)this.encodedPosZ * 0.000244140625) >= -i && d0 <= i
//                && this.trackedEntity.isSpectatedByPlayer(player);
    }


    private boolean isPlayerWatchingThisChunk(EntityPlayerMP playerMP) {
        return playerMP.getServerWorld().getPlayerChunkMap().isPlayerWatchingChunk(playerMP, this.trackedEntity.chunkCoordX, this.trackedEntity.chunkCoordZ);
    }


    public void updatePlayerEntities(List<EntityPlayer> players) {
        this.trackedEntity.elcaMinRange = 1073741824d;
        for (int i = 0; i < players.size(); ++i)
            updatePlayerEntity((EntityPlayerMP)players.get(i));
    }


    private Packet<?> createSpawnPacket() {
        Entity trackedEntity = this.trackedEntity;
        if (trackedEntity.isDead) {
            LOGGER.warn("Fetching addPacket for removed entity");
        }

        Packet pkt = FMLNetworkHandler.getEntitySpawningPacket(trackedEntity);
        if (pkt != null) return pkt;

        if (trackedEntity instanceof EntityPlayerMP) {
            return new SPacketSpawnPlayer((EntityPlayer)trackedEntity);
        } else if (trackedEntity instanceof IAnimals) {
            this.lastHeadMotion = MathHelper.floor(trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
            return new SPacketSpawnMob((EntityLivingBase)trackedEntity);
        } else if (trackedEntity instanceof EntityPainting) {
            return new SPacketSpawnPainting((EntityPainting)trackedEntity);
        } else if (trackedEntity instanceof EntityItem) {
            return new SPacketSpawnObject(trackedEntity, 2, 1);
        } else if (trackedEntity instanceof EntityMinecart) {
            EntityMinecart entityminecart = (EntityMinecart)trackedEntity;
            return new SPacketSpawnObject(trackedEntity, 10, entityminecart.getType().getId());
        } else if (trackedEntity instanceof EntityBoat) {
            return new SPacketSpawnObject(trackedEntity, 1);
        } else if (trackedEntity instanceof EntityXPOrb) {
            return new SPacketSpawnExperienceOrb((EntityXPOrb)trackedEntity);
        } else if (trackedEntity instanceof EntityFishHook) {
            Entity entity2 = ((EntityFishHook)trackedEntity).getAngler();
            return new SPacketSpawnObject(trackedEntity, 90, entity2 == null ? trackedEntity.getEntityId() : entity2.getEntityId());
        } else if (trackedEntity instanceof EntitySpectralArrow) {
            Entity entity1 = ((EntitySpectralArrow)trackedEntity).shootingEntity;
            return new SPacketSpawnObject(trackedEntity, 91, 1 + (entity1 == null ? trackedEntity.getEntityId() : entity1.getEntityId()));
        } else if (trackedEntity instanceof EntityTippedArrow) {
            Entity entity = ((EntityArrow)trackedEntity).shootingEntity;
            return new SPacketSpawnObject(trackedEntity, 60, 1 + (entity == null ? trackedEntity.getEntityId() : entity.getEntityId()));
        } else if (trackedEntity instanceof EntitySnowball) {
            return new SPacketSpawnObject(trackedEntity, 61);
        } else if (trackedEntity instanceof EntityLlamaSpit) {
            return new SPacketSpawnObject(trackedEntity, 68);
        } else if (trackedEntity instanceof EntityPotion) {
            return new SPacketSpawnObject(trackedEntity, 73);
        }
        else if (trackedEntity instanceof EntityExpBottle) {
            return new SPacketSpawnObject(trackedEntity, 75);
        } else if (trackedEntity instanceof EntityEnderPearl) {
            return new SPacketSpawnObject(trackedEntity, 65);
        } else if (trackedEntity instanceof EntityEnderEye) {
            return new SPacketSpawnObject(trackedEntity, 72);
        } else if (trackedEntity instanceof EntityFireworkRocket) {
            return new SPacketSpawnObject(trackedEntity, 76);
        } else if (trackedEntity instanceof EntityFireball) {
            EntityFireball entityfireball = (EntityFireball)trackedEntity;
            SPacketSpawnObject spacketspawnobject = null;
            int i = 63;

            if (trackedEntity instanceof EntitySmallFireball) {
                i = 64;
            }
            else if (trackedEntity instanceof EntityDragonFireball) {
                i = 93;
            }
            else if (trackedEntity instanceof EntityWitherSkull) {
                i = 66;
            }

            if (entityfireball.shootingEntity != null) {
                spacketspawnobject = new SPacketSpawnObject(trackedEntity, i, ((EntityFireball)trackedEntity).shootingEntity.getEntityId());
            } else {
                spacketspawnobject = new SPacketSpawnObject(trackedEntity, i, 0);
            }

            spacketspawnobject.setSpeedX((int)(entityfireball.accelerationX * 8000.0D));
            spacketspawnobject.setSpeedY((int)(entityfireball.accelerationY * 8000.0D));
            spacketspawnobject.setSpeedZ((int)(entityfireball.accelerationZ * 8000.0D));
            return spacketspawnobject;
        }
        else if (trackedEntity instanceof EntityShulkerBullet) {
            SPacketSpawnObject spacketspawnobject1 = new SPacketSpawnObject(trackedEntity, 67, 0);
            spacketspawnobject1.setSpeedX((int)(trackedEntity.motionX * 8000.0D));
            spacketspawnobject1.setSpeedY((int)(trackedEntity.motionY * 8000.0D));
            spacketspawnobject1.setSpeedZ((int)(trackedEntity.motionZ * 8000.0D));
            return spacketspawnobject1;
        } else if (trackedEntity instanceof EntityEgg) {
            return new SPacketSpawnObject(trackedEntity, 62);
        } else if (trackedEntity instanceof EntityEvokerFangs) {
            return new SPacketSpawnObject(trackedEntity, 79);
        } else if (trackedEntity instanceof EntityTNTPrimed) {
            return new SPacketSpawnObject(trackedEntity, 50);
        } else if (trackedEntity instanceof EntityEnderCrystal) {
            return new SPacketSpawnObject(trackedEntity, 51);
        } else if (trackedEntity instanceof EntityFallingBlock) {
            EntityFallingBlock entityfallingblock = (EntityFallingBlock)trackedEntity;
            return new SPacketSpawnObject(
                    trackedEntity, 70, Block.getStateId(entityfallingblock.getBlock())
            );
        } else if (trackedEntity instanceof EntityArmorStand) {
            return new SPacketSpawnObject(trackedEntity, 78);
        } else if (trackedEntity instanceof EntityItemFrame) {
            EntityItemFrame entityitemframe = (EntityItemFrame)trackedEntity;
            return new SPacketSpawnObject(trackedEntity, 71, entityitemframe.facingDirection.getHorizontalIndex(), entityitemframe.getHangingPosition());
        } else if (trackedEntity instanceof EntityLeashKnot) {
            EntityLeashKnot entityleashknot = (EntityLeashKnot)trackedEntity;
            return new SPacketSpawnObject(trackedEntity, 77, 0, entityleashknot.getHangingPosition());
        } else if (trackedEntity instanceof EntityAreaEffectCloud) {
            return new SPacketSpawnObject(trackedEntity, 3);
        } else {
            throw new IllegalArgumentException("Don't know how to add " + trackedEntity.getClass() + "!");
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
        this.maxRange = Math.min(range, maxRangeIn);
    }


    public void resetPlayerVisibility() {
        this.updatedPlayerVisibility = false;
    }


    private void updateDistancesFor(EntityPlayer player) {
        double distance = trackedEntity.getDistanceSq(player.posX, player.posY, player.posZ);
        if (distance < trackedEntity.elcaMinRange) trackedEntity.elcaMinRange = distance;
    }


    public void updatePlayerList(List<EntityPlayer> players) {
        Entity trackedEntity = this.trackedEntity;
        if (this.playerEntitiesUpdated = (!this.updatedPlayerVisibility ||
                trackedEntity.getDistanceSq(this.lastTrackedEntityPosX, this.lastTrackedEntityPosY, this.lastTrackedEntityPosZ) > 16.0D)) {
            this.lastTrackedEntityPosX = trackedEntity.posX;
            this.lastTrackedEntityPosY = trackedEntity.posY;
            this.lastTrackedEntityPosZ = trackedEntity.posZ;
            this.updatedPlayerVisibility = true;
            this.updatePlayerEntities(players);
        } else if(this.updateCounter % ElcaConfig.RESET_LOW_LEVEL_MOBS == 0) {
            trackedEntity.elcaMinRange = 1073741824d;
            trackingPlayers.forEach(this::updateDistancesFor);
        }

        List<Entity> list = trackedEntity.getPassengers();

        if(!(list.isEmpty() && this.passengers.isEmpty()))
            if (!list.equals(this.passengers)) {
                this.passengers = list;
                this.sendPacketToTrackedPlayers(new SPacketSetPassengers(trackedEntity));
            }
        NetworkManager selfManager = (trackedEntity instanceof EntityPlayerMP)?
                ((EntityPlayerMP)trackedEntity).connection.netManager:null;

        if (selfManager == null && this.updateCounter % 12 == 0 && trackedEntity instanceof EntityItemFrame) {
            EntityItemFrame entityitemframe = (EntityItemFrame)trackedEntity;
            ItemStack itemstack = entityitemframe.getDisplayedItem();

            if (itemstack.getItem() instanceof ItemMap) {
                MapData mapdata = ((ItemMap) itemstack.getItem()).getMapData(itemstack, trackedEntity.world);

                for (EntityPlayer entityplayer : players) {
                    EntityPlayerMP entityplayermp = (EntityPlayerMP)entityplayer;
                    mapdata.updateVisiblePlayers(entityplayermp, itemstack);
                    Packet<?> packet = ((ItemMap) itemstack.getItem()).createMapDataPacket(itemstack, trackedEntity.world, entityplayermp);

                    if (packet != null)
                        entityplayermp.connection.netManager.sendPacketE(packet);
                }
            }
            this.sendMetadata0( null);
        }

        if (this.updateCounter % this.updateFrequency == 0 || trackedEntity.isAirBorne || trackedEntity.getDataManager().isDirty()) {
            if (trackedEntity.isRiding()) {
                int j1 = MathHelper.floor(trackedEntity.rotationYaw * F_256_T_360);
                int l1 = MathHelper.floor(trackedEntity.rotationPitch * F_256_T_360);

                if (Math.abs(j1 - this.encodedRotationYaw) >= 1 || Math.abs(l1 - this.encodedRotationPitch) >= 1) {
                    this.sendPacketToTrackedPlayers(new SPacketEntity.S16PacketEntityLook(trackedEntity.getEntityId(), (byte)j1, (byte)l1, trackedEntity.onGround));
                    this.encodedRotationYaw = j1;
                    this.encodedRotationPitch = l1;
                }

                this.encodedPosX = EntityTracker.getPositionLong(trackedEntity.posX);
                this.encodedPosY = EntityTracker.getPositionLong(trackedEntity.posY);
                this.encodedPosZ = EntityTracker.getPositionLong(trackedEntity.posZ);
                this.sendMetadata0(selfManager);
                this.ridingEntity = true;
            } else {
                ++this.ticksSinceLastForcedTeleport;
                long i1 = EntityTracker.getPositionLong(trackedEntity.posX);
                long i2 = EntityTracker.getPositionLong(trackedEntity.posY);
                long j2 = EntityTracker.getPositionLong(trackedEntity.posZ);
                int k2 = MathHelper.floor(trackedEntity.rotationYaw * F_256_T_360);
                int i = MathHelper.floor(trackedEntity.rotationPitch * F_256_T_360);
                long j = i1 - this.encodedPosX;
                long k = i2 - this.encodedPosY;
                long l = j2 - this.encodedPosZ;
                Packet<?> packet1 = null;
                boolean flag = j * j + k * k + l * l >= 128L || this.updateCounter % 60 == 0;
                boolean flag1 = Math.abs(k2 - this.encodedRotationYaw) >= 1 || Math.abs(i - this.encodedRotationPitch) >= 1;

                if (this.updateCounter > 0 || trackedEntity instanceof EntityArrow) {
                    if (j >= -32768L && j < 32768L && k >= -32768L && k < 32768L && l >= -32768L && l < 32768L && this.ticksSinceLastForcedTeleport <= 400
                            && !this.ridingEntity && this.onGround == trackedEntity.onGround) {

                        if ((!flag || !flag1) && !(trackedEntity instanceof EntityArrow)) {
                            if (flag) {
                                packet1 = new SPacketEntity.S15PacketEntityRelMove(trackedEntity.getEntityId(), j, k, l, trackedEntity.onGround);
                            } else if (flag1) {
                                packet1 = new SPacketEntity.S16PacketEntityLook(trackedEntity.getEntityId(), (byte)k2, (byte)i, trackedEntity.onGround);
                            }
                        } else {
                            packet1 = new SPacketEntity.S17PacketEntityLookMove(trackedEntity.getEntityId(), j, k, l, (byte)k2, (byte)i, trackedEntity.onGround);
                        }
                    } else {
                        this.onGround = trackedEntity.onGround;
                        this.ticksSinceLastForcedTeleport = 0;
                        this.resetPlayerVisibility();
                        packet1 = new SPacketEntityTeleport(trackedEntity);
                    }
                }

                boolean flag2 = this.sendVelocityUpdates || (trackedEntity.isLiving() && ((EntityLivingBase)trackedEntity).isElytraFlying());


                if (flag2 && this.updateCounter > 0) {
                    double d0 = trackedEntity.motionX - this.lastTrackedEntityMotionX;
                    double d1 = trackedEntity.motionY - this.lastTrackedEntityMotionY;
                    double d2 = trackedEntity.motionZ - this.motionZ;
                    double d4 = d0 * d0 + d1 * d1 + d2 * d2;

                    if (d4 > 4.0E-4D || d4 > 0.0D && trackedEntity.motionX == 0.0D && trackedEntity.motionY == 0.0D && trackedEntity.motionZ == 0.0D) {
                        this.lastTrackedEntityMotionX = trackedEntity.motionX;
                        this.lastTrackedEntityMotionY = trackedEntity.motionY;
                        this.motionZ = trackedEntity.motionZ;
                        this.sendPacketToTrackedPlayers(new SPacketEntityVelocity(
                                        trackedEntity.getEntityId(),
                                        this.lastTrackedEntityMotionX,
                                        this.lastTrackedEntityMotionY,
                                        this.motionZ)
                        );
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

            int k1 = MathHelper.floor(trackedEntity.getRotationYawHead() * F_256_T_360);

            if (Math.abs(k1 - this.lastHeadMotion) >= 1) {
                this.sendPacketToTrackedPlayers(new SPacketEntityHeadLook(trackedEntity, (byte)k1));
                this.lastHeadMotion = k1;
            }
            trackedEntity.isAirBorne = false;
        }

        ++this.updateCounter;

        if (trackedEntity.velocityChanged) {
            Packet<?> packet = new SPacketEntityVelocity(trackedEntity);
            if(selfManager != null){
                sendPacketToTrackedPlayersAndSelf(selfManager, packet);
            } else {
                sendPacketToTrackedPlayers(packet);
            }
            trackedEntity.velocityChanged = false;
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
        if (manager != null || this.trackedEntity.isLiving()) {
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