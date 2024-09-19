package net.minecraft.entity;

import java.util.*;
import com.google.common.collect.Lists;
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
import plus.fserver.server.network.packet.SEntityPacketPlayBase;
import plus.tson.utl.Te4HashSet;
import javax.annotation.Nullable;
import static plus.fserver.utls.math.MathUtls.F_256_T_360;


public class EntityTrackerEntry {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Entity trackedEntity;
    private final short range;
    private short maxRange;
    private final int updateFrequency;
    private long encodedPosX;
    private long encodedPosY;
    private long encodedPosZ;
    private short encodedRotationYaw;
    private short encodedRotationPitch;
    private short lastHeadMotion;
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
    public final Set<EntityPlayerMP> trackingPlayers = new Te4HashSet<>(3);//not edit this!!!

    public EntityTrackerEntry(Entity entityIn, int rangeIn, int maxRangeIn, int updateFrequencyIn, boolean sendVelocityUpdatesIn) {
        this.trackedEntity = entityIn;
        this.range = (short) rangeIn;
        this.maxRange = (short) Math.min(rangeIn, maxRangeIn);
        this.updateFrequency = updateFrequencyIn;
        this.sendVelocityUpdates = sendVelocityUpdatesIn;
        this.encodedPosX = EntityTracker.getPositionLong(entityIn.posX);
        this.encodedPosY = EntityTracker.getPositionLong(entityIn.posY);
        this.encodedPosZ = EntityTracker.getPositionLong(entityIn.posZ);
        this.encodedRotationYaw   = (short) MathHelper.floor(entityIn.rotationYaw * F_256_T_360);
        this.encodedRotationPitch = (short) MathHelper.floor(entityIn.rotationPitch * F_256_T_360);
        this.lastHeadMotion = (short) MathHelper.floor(entityIn.getRotationYawHead() * F_256_T_360);
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
                            NetworkManager net = playerMP.connection.netManager;
                            net.sendPacketSync(packet);
                            net.sendPacketSync(packet2);
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
                            if(equp == next){
                                playerMP.connection.netManager.sendPacketE(equp);
                            } else {
                                SElcaPacketEntityEquipment finalEqup = equp;
                                playerMP.connection.netManager.exec(() -> {
                                    SElcaPacketEntityEquipment temp = finalEqup;
                                    while (temp != null){
                                        playerMP.connection.netManager.sendPacketSync(temp);
                                        temp = temp.next();
                                    }
                                });
                            }
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
            this.lastHeadMotion = (short) MathHelper.floor(trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
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
        this.maxRange = (short) Math.min(range, maxRangeIn);
    }


    public void resetPlayerVisibility() {
        this.updatedPlayerVisibility = false;
    }


    private void updateDistancesFor(EntityPlayer player) {
        Entity trackedEntity;
        double distance = (trackedEntity = this.trackedEntity).getDistanceSq(player.posX, player.posY, player.posZ);
        if (distance < trackedEntity.elcaMinRange) trackedEntity.elcaMinRange = distance;
    }


    public void updatePlayerList(List<EntityPlayer> players) {
        Entity entity = this.trackedEntity;
        if (this.playerEntitiesUpdated = (!this.updatedPlayerVisibility ||
                entity.getDistanceSq(this.lastTrackedEntityPosX, this.lastTrackedEntityPosY, this.lastTrackedEntityPosZ) > 16.0D)) {
            this.lastTrackedEntityPosX = entity.posX;
            this.lastTrackedEntityPosY = entity.posY;
            this.lastTrackedEntityPosZ = entity.posZ;
            this.updatedPlayerVisibility = true;
            this.updatePlayerEntities(players);
        } else if(this.updateCounter % ElcaConfig.RESET_LOW_LEVEL_MOBS == 0) {
            entity.elcaMinRange = 1073741824d;
            trackingPlayers.forEach(this::updateDistancesFor);
        }

        List<Entity> list = entity.getPassengersRaw();

        if(!(list.isEmpty() && this.passengers.isEmpty()))
            if (!list.equals(this.passengers)) {
                if(list.isEmpty()){
                    this.passengers = Collections.emptyList();
                } else {
                    this.passengers = Lists.newArrayList(list);
                }
                this.sendPacketToTrackedPlayers(new SPacketSetPassengers(entity, list));
            }
        NetworkManager selfManager = (entity instanceof EntityPlayerMP)?
                ((EntityPlayerMP)entity).connection.netManager:null;

        if (selfManager == null && this.updateCounter % 12 == 0 && entity instanceof EntityItemFrame) {
            EntityItemFrame entityitemframe = (EntityItemFrame)entity;
            ItemStack itemstack = entityitemframe.getDisplayedItem();

            if (itemstack.getItem() instanceof ItemMap) {
                MapData mapdata = ((ItemMap) itemstack.getItem()).getMapData(itemstack, entity.world);

                for (EntityPlayer entityplayer : players) {
                    EntityPlayerMP entityplayermp = (EntityPlayerMP)entityplayer;
                    mapdata.updateVisiblePlayers(entityplayermp, itemstack);
                    Packet<?> packet = ((ItemMap) itemstack.getItem()).createMapDataPacket(itemstack, entity.world, entityplayermp);

                    if (packet != null)
                        entityplayermp.connection.netManager.sendPacketE(packet);
                }
            }
            this.sendMetadata0( null);
        }

        if (this.updateCounter % this.updateFrequency == 0 || entity.isAirBorne || entity.getDataManager().isDirty()) {
            if (entity.isRiding()) {
                int j1 = MathHelper.floor(entity.rotationYaw * F_256_T_360);
                int l1 = MathHelper.floor(entity.rotationPitch * F_256_T_360);

                if (Math.abs(j1 - this.encodedRotationYaw) >= 1 || Math.abs(l1 - this.encodedRotationPitch) >= 1) {
                    this.sendPacketToTrackedPlayers(new SPacketEntity.S16PacketEntityLook(entity.getEntityId(), (byte)j1, (byte)l1, entity.onGround));
                    this.encodedRotationYaw = (short) j1;
                    this.encodedRotationPitch = (short) l1;
                }

                this.encodedPosX = EntityTracker.getPositionLong(entity.posX);
                this.encodedPosY = EntityTracker.getPositionLong(entity.posY);
                this.encodedPosZ = EntityTracker.getPositionLong(entity.posZ);
                this.sendMetadata0(selfManager);
                this.ridingEntity = true;
            } else {
                ++this.ticksSinceLastForcedTeleport;
                long i1 = EntityTracker.getPositionLong(entity.posX);
                long i2 = EntityTracker.getPositionLong(entity.posY);
                long j2 = EntityTracker.getPositionLong(entity.posZ);
                int k2 = MathHelper.floor(entity.rotationYaw * F_256_T_360);
                int i = MathHelper.floor(entity.rotationPitch * F_256_T_360);
                long j = i1 - this.encodedPosX;
                long k = i2 - this.encodedPosY;
                long l = j2 - this.encodedPosZ;
                SEntityPacketPlayBase packet1 = null;
                boolean flag = j * j + k * k + l * l >= 128L || this.updateCounter % 60 == 0;
                boolean flag1 = Math.abs(k2 - this.encodedRotationYaw) >= 1 || Math.abs(i - this.encodedRotationPitch) >= 1;

                if (this.updateCounter > 0 || entity instanceof EntityArrow) {
                    if (j >= -32768L && j < 32768L && k >= -32768L && k < 32768L && l >= -32768L && l < 32768L && this.ticksSinceLastForcedTeleport <= 400
                            && !this.ridingEntity && this.onGround == entity.onGround) {

                        if ((!flag || !flag1) && !(entity instanceof EntityArrow)) {
                            if (flag) {
                                packet1 = new SPacketEntity.S15PacketEntityRelMove(entity.getEntityId(), j, k, l, entity.onGround);
                            } else if (flag1) {
                                packet1 = new SPacketEntity.S16PacketEntityLook(entity.getEntityId(), (byte)k2, (byte)i, entity.onGround);
                            }
                        } else {
                            packet1 = new SPacketEntity.S17PacketEntityLookMove(entity.getEntityId(), j, k, l, (byte)k2, (byte)i, entity.onGround);
                        }
                    } else {
                        this.onGround = entity.onGround;
                        this.ticksSinceLastForcedTeleport = 0;
                        this.resetPlayerVisibility();
                        packet1 = new SPacketEntityTeleport(entity);
                    }
                }

                boolean flag2 = this.sendVelocityUpdates || (entity.isLiving() && ((EntityLivingBase)entity).isElytraFlying());


                if (flag2 && this.updateCounter > 0) {
                    double d0 = entity.motionX - this.lastTrackedEntityMotionX;
                    double d1 = entity.motionY - this.lastTrackedEntityMotionY;
                    double d2 = entity.motionZ - this.motionZ;
                    double d4 = d0 * d0 + d1 * d1 + d2 * d2;

                    if (d4 > 4.0E-4D || d4 > 0.0D && entity.motionX == 0.0D && entity.motionY == 0.0D && entity.motionZ == 0.0D) {
                        this.sendPacketToTrackedPlayers(new SPacketEntityVelocity(
                                entity.getEntityId(),
                                this.lastTrackedEntityMotionX = entity.motionX,
                                this.lastTrackedEntityMotionY = entity.motionY,
                                this.motionZ = entity.motionZ
                        ));
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
                    this.encodedRotationYaw = (short) k2;
                    this.encodedRotationPitch = (short) i;
                }
                this.ridingEntity = false;
            }

            int k1 = MathHelper.floor(entity.getRotationYawHead() * F_256_T_360);

            int amount; //5832 = 18*18*18
            if(entity.elcaMinRange > 5832D){
                amount = 5;
            } else {
                amount = 1;
            }
            if (Math.abs(k1 - this.lastHeadMotion) >= amount) {
                this.sendPacketToTrackedPlayers(new SPacketEntityHeadLook(entity, (byte)k1));
                this.lastHeadMotion = (short) k1;
            }
            entity.isAirBorne = false;
        }

        ++this.updateCounter;

        if (entity.velocityChanged) {
            Packet<?> packet = new SPacketEntityVelocity(entity);
            if(selfManager != null){
                sendPacketToTrackedPlayersAndSelf(selfManager, packet);
            } else {
                sendPacketToTrackedPlayers(packet);
            }
            entity.velocityChanged = false;
        }
    }


    public void sendPacketToTrackedPlayersAndSelf(NetworkManager manager, Packet<?> packetIn){
        manager.addToQueueE(packetIn);
        sendPacketToTrackedPlayers(packetIn);
    }


    public void sendPacketToTrackedPlayers(Packet<?> packetIn){
        ((Te4HashSet<EntityPlayerMP>)trackingPlayers).forEachIfCan(playerMP -> playerMP.connection.netManager.addToQueueE(packetIn));
    }


    protected void sendPacketToTrackedPlayers(SEntityPacketPlayBase packetIn){
        ((Te4HashSet<EntityPlayerMP>)trackingPlayers).forEachIfCan(packetIn);
    }


    private void sendMetadata0(@Nullable NetworkManager manager) {
        Entity trackedEntity;
        EntityDataManager entitydatamanager = (trackedEntity = this.trackedEntity).getDataManager();
        SEntityPacketPlayBase packet;
        if (entitydatamanager.isDirty()) {
            packet = new SPacketEntityMetadata(trackedEntity.getEntityId(), entitydatamanager, false);

            if (manager != null) manager.addToQueueN(packet);
            sendPacketToTrackedPlayers(packet);
        }
        if (manager != null || trackedEntity.isLiving()) {
            AttributeMap attributemap = (AttributeMap)((EntityLivingBase)trackedEntity).getAttributeMapOrNull();
            if (attributemap != null) {
                Set<IAttributeInstance> set = attributemap.getDirtyInstances();

                if (!set.isEmpty()) {
                    packet = new SPacketEntityProperties(trackedEntity.getEntityId(), set);

                    if (manager != null) manager.addToQueueN(packet);
                    sendPacketToTrackedPlayers(packet);

                    set.clear();
                }
            }
        }
    }
}