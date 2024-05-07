package net.minecraft.entity;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.world.EntityTrackerFactory;


public class EntityTracker {
    private static final EntityTrackerFactory trackingTable = new EntityTrackerFactory();
    private static final Logger LOGGER = LogManager.getLogger();
    private final WorldServer world;
    //private final Set<EntityTrackerEntry> entries = new IdentityTe4HashSet<>(); //is safe
    private int maxTrackingDistanceThreshold;

    public EntityTracker(WorldServer theWorldIn) {
        this.world = theWorldIn;
        this.maxTrackingDistanceThreshold = theWorldIn.getMinecraftServer().getPlayerList().getEntityViewDistance();
    }


    public static long getPositionLong(double value) {
        return MathHelper.lfloor(value * 4096.0D);
    }


    @SideOnly(Side.CLIENT)
    public static void updateServerPosition(Entity entityIn, double x, double y, double z) {
        entityIn.serverPosX = getPositionLong(x);
        entityIn.serverPosY = getPositionLong(y);
        entityIn.serverPosZ = getPositionLong(z);
    }


    public void track(Entity entityIn, int trackingRange, int updateFrequency) {
        this.track(entityIn, trackingRange, updateFrequency, false);
    }


    //private final AsyncChecker checker = new AsyncChecker();
    public void track(Entity entityIn, int trackingRange, final int updateFrequency, boolean sendVelocityUpdates) {
        try {
            if (entityIn.trackerEntry != null) {
                throw new IllegalStateException("Entity is already tracked!");
            }
            EntityTrackerEntry entry = new EntityTrackerEntry(entityIn, trackingRange, this.maxTrackingDistanceThreshold, updateFrequency, sendVelocityUpdates);

            //this.entries.add(entry);
            entityIn.trackerEntry = entry;

            entry.updatePlayerEntities(this.world.playerEntities);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding entity to track");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity To Track");
            crashreportcategory.addCrashSection("Tracking range", trackingRange + " blocks");
            crashreportcategory.addDetail("Update interval", () -> {
                String s = "Once per " + updateFrequency + " ticks";
                if (updateFrequency == Integer.MAX_VALUE) {
                    s = "Maximum (" + s + ")";
                }
                return s;
            });
            entityIn.addEntityCrashInfo(crashreportcategory);

            try {
                throw new ReportedException(crashreport);
            } catch (ReportedException reportedexception) {
                LOGGER.error("\"Silently\" catching entity tracking error.", reportedexception);
            }
        }
    }


    public void untrack(Entity entityIn) {
        EntityTrackerEntry entry;
        if (entityIn instanceof EntityPlayerMP) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)entityIn;
            for (Entity entity : world.loadedEntityList) {
                if((entry = entity.trackerEntry) != null)
                    entry.removeFromTrackedPlayers(entityplayermp);
                else
                    System.out.println("WARN: TRYING TO REMOVE ENTITY WITH NULL TRACKER");
            }
        }
        entry = entityIn.trackerEntry;
        if (entry != null) {
            entityIn.trackerEntry = null;
            entry.sendDestroyEntityPacketToTrackedPlayers();
        }
    }


    private final ArrayList<EntityPlayerMP> toUpdate = new ArrayList<>();
    public void tick() {
        List<EntityPlayer> players = this.world.playerEntities;
        EntityTrackerEntry entry;
        if((world.getTotalWorldTime()&1)==0){
            for (Entity entity : world.loadedEntityList) {
                if((entry = entity.trackerEntry) != null)
                    entry.updatePlayerList(players);
            }
        } else {
            ArrayList<EntityPlayerMP> list = toUpdate;
            for (Entity entity : world.loadedEntityList) {
                if((entry = entity.trackerEntry) != null) {
                    entry.updatePlayerList(players);
                    if (entry.playerEntitiesUpdated) {

                        if (entity instanceof EntityPlayerMP) list.add((EntityPlayerMP) entity);

                    }
                }
            }
            for (int i = 0; i < list.size(); ++i) {
                EntityPlayerMP entityplayermp = list.get(i);
                for (Entity entity : world.loadedEntityList)
                    if((entry = entity.trackerEntry) != null)
                        if (entry.getTrackedEntity() != entityplayermp)
                            entry.updatePlayerEntity(entityplayermp);
            }
            list.clear();
        }
    }


    public void updateVisibility(EntityPlayerMP player) {
        EntityTrackerEntry entry;
        for (Entity entity : world.loadedEntityList)
            if((entry = entity.trackerEntry) != null) {
                if (entity == player) entry.updatePlayerEntities(this.world.playerEntities);
                else                  entry.updatePlayerEntity(player);
            }
    }


    public void sendToTracking(Entity entity, Packet<?> packetIn) {
        EntityTrackerEntry entry = entity.trackerEntry;
        if (entry != null) entry.sendPacketToTrackedPlayers(packetIn);
    }


    public Set<? extends net.minecraft.entity.player.EntityPlayer> getTrackingPlayers(Entity entity) {
        EntityTrackerEntry entry = entity.trackerEntry;

        if (entry == null) return java.util.Collections.emptySet();
        else               return java.util.Collections.unmodifiableSet(entry.trackingPlayers);
    }


    public void sendToTrackingAndSelf(Entity entity, Packet<?> packetIn) {
        EntityTrackerEntry entry = entity.trackerEntry;
        if (entry != null) entry.sendToTrackingAndSelf(packetIn);
    }


    public void removePlayerFromTrackers(EntityPlayerMP player) {
        EntityTrackerEntry entry;
        for (Entity entity : player.world.loadedEntityList)
            if((entry = entity.trackerEntry) != null) entry.removeTrackedPlayerSymmetric(player);
    }


    public void sendLeashedEntitiesInChunk(EntityPlayerMP player, Chunk chunkIn) {
        List<Entity> list = Lists.newArrayList();
        List<Entity> list1 = Lists.newArrayList();
        for (Entity entity : world.loadedEntityList) {
            EntityTrackerEntry entry = entity.trackerEntry;
            if (entry != null) {
                if (entity != player && entity.chunkCoordX == chunkIn.x && entity.chunkCoordZ == chunkIn.z) {
                    entry.updatePlayerEntity(player);

                    if (entity instanceof EntityLiving && ((EntityLiving) entity).getLeashHolder() != null)
                        list.add(entity);

                    if (!entity.getPassengers().isEmpty()) list1.add(entity);
                }
            }
        }
        if(list.isEmpty()||list1.isEmpty())return;
        NetworkManager manager = player.connection.netManager;
        manager.exec(() -> {
            if (!list.isEmpty())
                for (Entity entity1 : list) manager.sendPacketSync(new SPacketEntityAttach(entity1, ((EntityLiving)entity1).getLeashHolder()));

            if (!list1.isEmpty())
                for (Entity entity2 : list1) manager.sendPacketSync(new SPacketSetPassengers(entity2));
        });
    }


    public void setViewDistance(int distance) {
        this.maxTrackingDistanceThreshold = ((distance - 1) << 4);
        EntityTrackerEntry entry;
        for (Entity entity : world.loadedEntityList)
            if((entry = entity.trackerEntry) != null)
                entry.setMaxRange(this.maxTrackingDistanceThreshold);
    }


    public void track(Entity entityIn) {
        if (EntityRegistry.instance().tryTrackingEntity(this, entityIn)) return;

        if (entityIn instanceof EntityPlayerMP) {
            this.track(entityIn, 512, 2);
            EntityPlayerMP entityplayermp = (EntityPlayerMP)entityIn;
            EntityTrackerEntry entry;
            for (Entity entity : world.loadedEntityList) {
                if((entry = entity.trackerEntry) != null)
                    if (entry.getTrackedEntity() != entityplayermp)
                        entry.updatePlayerEntity(entityplayermp);
            }
        } else {
            EntityTrackerFactory.TrackInfo inf = trackingTable.getFor(entityIn);
            if(inf==null)return;
            track(entityIn, inf.range, inf.updates, inf.sendVelocity);
        }
    }
}