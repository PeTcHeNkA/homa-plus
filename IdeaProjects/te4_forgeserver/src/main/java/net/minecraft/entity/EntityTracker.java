package net.minecraft.entity;

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
import plus.fserver.server.AsyEntityTracker;
import plus.fserver.server.network.packet.SEntityPacketPlayBase;
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.collections.EntityChunkIter;
import plus.fserver.utls.collections.EntityList;
import plus.fserver.utls.collections.EntityRemSwapList;
import plus.fserver.world.EntityTrackerFactory;


public class EntityTracker {
    private static final EntityTrackerFactory trackingTable = new EntityTrackerFactory();
    private static final Logger LOGGER = LogManager.getLogger();
    protected final WorldServer world;
    public int maxTrackingDistanceThreshold;

    public EntityTracker(WorldServer world) {
        this.world = world;
    }


    public final EntityTracker init(){
        this.maxTrackingDistanceThreshold = world.getMinecraftServer().getPlayerList().getEntityViewDistance();
        return this;
    }


    public WorldServer world(){
        return world;
    }


    public boolean isAsync(){
        return false;
    }


    public AsyEntityTracker toAsync(){
        AsyEntityTracker asy = new AsyEntityTracker(world);
        asy.maxTrackingDistanceThreshold = maxTrackingDistanceThreshold;
        return asy;
    }


    public EntityTracker toSync(){
        return this;
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
            onTrack(entityIn.trackerEntry = entry);
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


    public void onTrack(EntityTrackerEntry entry){
        entry.updatePlayerEntities(this.world.playerEntities);
    }


    public void untrack(Entity entityIn) {
        EntityTrackerEntry entry;
        if (entityIn instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)entityIn;

            for (EntityRemSwapList.E15<Entity> list: world.realEntityList.getRaw()) {
                for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                    if((entry = list.getC(cursor).trackerEntry) != null)
                        entry.removeFromTrackedPlayers(player);
                    else
                        System.out.println("WARN: TRYING TO REMOVE ENTITY WITH NULL TRACKER");
                }
            }
        }
        entry = entityIn.trackerEntry;
        if (entry != null) {
            entityIn.trackerEntry = null;
            entry.sendDestroyEntityPacketToTrackedPlayers();
        }
    }


    private final ArrayList<EntityPlayerMP> toUpdate = new ArrayList<>();
    protected boolean fastUpdate = false;

    public void tick() {
        List<EntityPlayer> players = this.world.playerEntities;
        EntityTrackerEntry entry;
        if(fastUpdate){
            fastUpdate = false;
            for (EntityRemSwapList.E15<Entity> list: world.realEntityList.getRaw()) {
                for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                    if((entry = list.getC(cursor).trackerEntry) != null)
                        entry.updatePlayerList(players);
                }
            }
        } else {
            fastUpdate = true;
            ArrayList<EntityPlayerMP> toUpdate = this.toUpdate;

            for (EntityRemSwapList.E15<Entity> list: world.realEntityList.getRaw())
                for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                    Entity entity = list.getC(cursor);
                    if ((entry = entity.trackerEntry) != null) {
                        entry.updatePlayerList(players);
                        if (entry.playerEntitiesUpdated) {
                            if (entity instanceof EntityPlayerMP) toUpdate.add((EntityPlayerMP) entity);
                        }
                    }
                }

            for (int i = 0, s = toUpdate.size(); i < s; ++i) {
                updatePlayer1(toUpdate.get(i));
            }
            toUpdate.clear();
        }
    }


    public void updateVisibility(EntityPlayerMP player) {
        EntityTrackerEntry entry;
        for (EntityRemSwapList.E15<Entity> list: world.realEntityList.getRaw()) {
            for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                Entity entity = list.getC(cursor);
                if ((entry = entity.trackerEntry) != null) {
                    if (entity == player) entry.updatePlayerEntities(this.world.playerEntities);
                    else entry.updatePlayerEntity(player);
                }
            }
        }
    }


    public void sendToTracking(Entity entity, Packet<?> packetIn) {
        EntityTrackerEntry entry = entity.trackerEntry;
        if (entry != null) entry.sendPacketToTrackedPlayers(packetIn);
    }


    public void sendToTracking(Entity entity, SEntityPacketPlayBase packetIn) {
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
        for (EntityRemSwapList.E15<Entity> list: world.realEntityList.getRaw()) {
            for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                if ((entry = list.getC(cursor).trackerEntry) != null) entry.removeTrackedPlayerSymmetric(player);
            }
        }
    }


    public void sendLeashedEntitiesInChunk(final EntityList<EntityPlayerMP> players, final Chunk chunk) {
        final ArrayListE6<Entity> list = new ArrayListE6<>(), list1 = new ArrayListE6<>();
        final int size = players.sizeC();
        for (Entity entity : new EntityChunkIter(chunk)) {
            final EntityTrackerEntry entry;
            if ((entry = entity.trackerEntry) != null) {
                for (int i = 0; i < size; ++i) {
                    final EntityPlayerMP player;
                    if (entity != (player = players.get(i))) {
                        entry.updatePlayerEntity(player);

                        if (entity instanceof EntityLiving && ((EntityLiving) entity).getLeashHolder() != null){
                            list.addF(entity);
                        }

                        if (!entity.getPassengers().isEmpty()) {
                            list1.addF(entity);
                        }
                    }
                }
            }
        }
        if(list.isEmptyC() && list1.isEmptyC())return;
        for (int i = 0; i < size; ++i) {
            EntityPlayerMP player = players.get(i);
            NetworkManager manager = player.connection.netManager;
            manager.exec(() -> {
                if (!list.isEmptyC())
                    for (Entity entity1 : list)
                        manager.sendPacketSync(new SPacketEntityAttach(entity1, ((EntityLiving) entity1).getLeashHolder()));

                if (!list1.isEmptyC())
                    for (Entity entity2 : list1) manager.sendPacketSync(new SPacketSetPassengers(entity2));
            });
        }
    }


    public void sendLeashedEntitiesInChunk(EntityPlayerMP player, Chunk chunk) {
        ArrayListE6<Entity> leashed  = new ArrayListE6<>();
        ArrayListE6<Entity> passengers = new ArrayListE6<>();

        for (EntityRemSwapList.E15<Entity> list: world.realEntityList.getRaw()) {
            for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                Entity entity = list.getC(cursor);
                EntityTrackerEntry entry = entity.trackerEntry;
                if (entry != null) {
                    if (entity != player && entity.chunkCoordX == chunk.x && entity.chunkCoordZ == chunk.z) {
                        entry.updatePlayerEntity(player);

                        if (entity instanceof EntityLiving && ((EntityLiving) entity).getLeashHolder() != null)
                            leashed.addF(entity);

                        if (!entity.getPassengers().isEmpty()) passengers.addF(entity);
                    }
                }
            }
        }

        if(leashed.isEmptyC() && passengers.isEmptyC())return;
        NetworkManager manager = player.connection.netManager;
        manager.exec(() -> {
            if (!leashed.isEmptyC())
                for (Entity entity1 : leashed) manager.sendPacketSync(new SPacketEntityAttach(entity1, ((EntityLiving)entity1).getLeashHolder()));

            if (!passengers.isEmptyC())
                for (Entity entity2 : passengers) manager.sendPacketSync(new SPacketSetPassengers(entity2));
        });
    }


    public void setViewDistance(int distance) {
        this.maxTrackingDistanceThreshold = ((distance - 1) << 4);
        EntityTrackerEntry entry;
        for (EntityRemSwapList.E15<Entity> list: world.realEntityList.getRaw()) {
            for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                if ((entry = list.getC(cursor).trackerEntry) != null)
                    entry.setMaxRange(this.maxTrackingDistanceThreshold);
            }
        }
    }


    public void track(Entity entityIn) {
        if (EntityRegistry.instance().tryTrackingEntity(this, entityIn)) return;

        if (entityIn instanceof EntityPlayerMP) {
            this.track(entityIn, 512, 2);
            EntityPlayerMP entityplayermp = (EntityPlayerMP)entityIn;
            updatePlayer1(entityplayermp);
        } else {
            EntityTrackerFactory.TrackInfo inf = trackingTable.getFor(entityIn);
            if(inf==null)return;
            track(entityIn, inf.range, inf.updates, inf.sendVelocity);
        }
    }


    private void updatePlayer1(EntityPlayerMP player) {
        EntityTrackerEntry entry;
        for (EntityRemSwapList.E15<Entity> list: world.realEntityList.getRaw()) {
            for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                if ((entry = list.getC(cursor).trackerEntry) != null)
                    if (entry.getTrackedEntity() != player)
                        entry.updatePlayerEntity(player);
            }
        }
    }
}