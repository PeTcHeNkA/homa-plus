package plus.fserver.server;

import plus.fserver.server.zstd.Native;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import plus.fserver.ElcaConfig;
import plus.fserver.api.ElcaServerUtls;
import plus.fserver.world.light.api.ILightingEngineProvider;
import java.util.List;
import java.util.concurrent.*;


public final class AsyWorldTickerV2 implements ThreadFactory{
    private static final boolean useSync = false;
    private static final Logger LOGGER = LogManager.getLogger();
    public final ScheduledExecutorService service;
    private final ConcurrentHashMap<Integer, WorldInfo> infs = new ConcurrentHashMap<>();
    private final MinecraftServer server;
    private final int saveAllEvery = ElcaConfig.SAVE_WORLD_RATE;
    private final boolean ignoreErrors;
    private boolean enabled = true;
    private Throwable error;
    private int tickCounter;
    private int countThreads = 0;
    private long lastError = 0;
    private final ScheduledFuture<?> netFuture;

    static{
        System.out.printf("Async ticker V2 initialized\n");
        Native.load(ElcaConfig.NATIVES);
    }


    public AsyWorldTickerV2(MinecraftServer server){
        this.server = server;
        this.ignoreErrors = ElcaConfig.IGNORE_ERRORS;
        service = ElcaServerUtls.getPool(server);
        //service = new AsyPoolTickerExecutor(ElcaConfig.ASY_THREAD_POOL_SIZE, this);
        EnumConnectionState.optimizeAccess();

        if(useSync) {
            netFuture = service.scheduleAtFixedRate(new NetworkTicker(), 0, ElcaConfig.NETWORK_TICK_RATE, TimeUnit.MILLISECONDS);
        } else {
            netFuture = server.getNetworkSystem().makeUpdateTask().scheduleAtFixedRate(service, ElcaConfig.NETWORK_TICK_RATE, TimeUnit.MILLISECONDS);
        }
    }


    class NetworkTicker implements Runnable{
        @Override
        public void run() {
            server.getNetworkSystem().networkTick();
        }
    }


    public void addDedicatedServerToTasks(){
        service.schedule(server, 50, TimeUnit.MILLISECONDS);
    }


    public void runAsync(Runnable task){
        if(enabled) service.execute(task);
        else task.run();
    }


    public void stop(){
        if(!enabled)return;
        enabled = false;
        netFuture.cancel(true);
    }


    public boolean isEnabled() {
        return enabled;
    }


    public void markNeedSave(){
        for(WorldInfo inf:infs.values()){
            inf.save = saveAllEvery;
        }
    }


    public void tick() throws Throwable {
        Integer[] ids = DimensionManager.getIDs(this.tickCounter++ % 300 == 0);
        for(int id:ids){
            WorldInfo result = infs.get(id);
            if(result == null){
                WorldServer worldServer = DimensionManager.getWorld(id);
                if(worldServer != null) {
                    result = new WorldInfo(id);
                    infs.put(id, result);
                }
            }
            if(useSync) tickWorld(result);
        }
        if(error != null){
            throw error;
        }
    }


    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("MC World thread#"+(countThreads++));
        thread.setDaemon(true);
        return thread;
    }


    private final class WorldInfo{
        final int id;
        public int checkToUnload;
        public int save;
        int tickCounter = 0;
        //private long lastTicked;
        private ScheduledFuture<?> future;
        private ScheduledFuture<?> light;
        private ScheduledFuture<?> tracker;

        private WorldInfo(int id) {
            this.id = id;
            if(!useSync) active();
        }
        public void active(){
            future = service.scheduleAtFixedRate(
                    () -> tickWorld(WorldInfo.this), 25, 50, TimeUnit.MILLISECONDS
            );
            light = service.scheduleAtFixedRate(
                    () -> updateLight(WorldInfo.this), 25, 50, TimeUnit.MILLISECONDS
            );
        }


        public void stop(){
            if(tickCounter==-1)return;
            System.out.println("STOP TICKING WORLD "+id);
            tickCounter = -1;
            infs.remove(id);
            try {
                future.cancel(true);
            } catch (Exception e){
                e.printStackTrace();
            }
            try {
                light.cancel(true);
            } catch (Exception e){
                e.printStackTrace();
            }
            if(tracker != null){
                try {
                    tracker.cancel(true);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    private void updateLight(WorldInfo inf){
        if(!enabled){
            inf.stop();
            return;
        }
        WorldServer worldserver = DimensionManager.getWorld(inf.id);
        if(worldserver==null || !worldserver.loaded){
            inf.stop();
            ((ILightingEngineProvider)worldserver).getLightingEngine().processLightUpdates();
        }
    }


    private void tickWorld(WorldInfo inf){
        if(!enabled){
            inf.stop();
            return;
        }
        WorldServer worldserver = DimensionManager.getWorld(inf.id);
        if(worldserver == null || !worldserver.loaded){
            inf.stop();
            return;
        }
        //System.out.println("Next tick: "+(System.currentTimeMillis()-inf.lastTicked) );
        ++inf.tickCounter;

        long curNanoTime = System.nanoTime();
        try {
            worldserver.tickSyncTasks();
            List<EntityPlayer> players = worldserver.playerEntities;
            if (inf.tickCounter % ElcaConfig.TIME_PACKET_RATE == 0) {
                SPacketTimeUpdate packet = new SPacketTimeUpdate(
                        worldserver.getTotalWorldTime(), worldserver.getWorldTime(),
                        worldserver.getGameRules().doDaylightCycle.getBoolean());
                EntityPlayer player;
                for (int i = 0, s = players.size(); i < s; i++){
                    player = players.get(i);
                    if(player instanceof EntityPlayerMP) {
                        EntityPlayerMP mp = (EntityPlayerMP) player;
                        mp.connection.onTickWorld();
                        mp.connection.netManager.sendPacketE(packet);
                    }
                }
            } else {
                EntityPlayer player;
                for (int i = 0, s = players.size(); i < s; i++){
                    player = players.get(i);
                    if(player instanceof EntityPlayerMP) {
                        EntityPlayerMP mp = (EntityPlayerMP) player;
                        mp.connection.onTickWorld();
                    }
                }
            }
            if(inf.tickCounter % 200 == 0){
                worldserver.getChunkProvider().chunkGenerator.cleanupCache();
                worldserver.provider.getBiomeProvider().cleanupCache();
            }

            FMLCommonHandler.instance().onPreWorldTick(worldserver);
            if(inf.checkToUnload++> ElcaConfig.CHECK_TO_UNLOAD_RATE) {
                //todo fix join to unloading world
                inf.checkToUnload = 0;
                if (worldserver.canUnload()) {
                    unload(worldserver, inf.id);
                    inf.save = 0;
                    inf.stop();
                    return;
                }
            }
            if(inf.save++ > saveAllEvery){
                try {
                    //todo test async net.minecraft.world.storage.MapStorage.saveData
                    worldserver.saveAllChunks(true,null);
                    server.getPlayerList().saveLocalPlayers(worldserver);
                    inf.save = 0;
                } catch (Exception saveError){
                    inf.save = saveAllEvery - 75;
                    onError(worldserver, saveError);
                }
            }
            worldserver.tick();
            worldserver.updateEntities();
            FMLCommonHandler.instance().onPostWorldTick(worldserver);

            EntityTracker tracker = worldserver.getEntityTracker();
            if(!tracker.isAsync()) {
                if(false && worldserver.meanTickTime() > ElcaConfig.MEAN_OVERLOAD){
                    tracker.tick();
                    AsyEntityTracker asy = tracker.toAsync();
                    worldserver.setTracker(asy);

                    inf.tracker = service.scheduleAtFixedRate(() -> {
                        if(asy.world().meanTickTime() > ElcaConfig.MEAN_OVERLOAD){
                            asy.tick();
                        } else {
                            asy.world().setTracker(asy.toSync());
                            //worldserver.addSyncTask(asy::runTasks);
                            inf.tracker.cancel(false);
                            inf.tracker = null;
                        }
                    }, 50, 50, TimeUnit.MILLISECONDS);
                } else {
                    tracker.tick();
                }
            }

            long spend = System.nanoTime() - curNanoTime;
            long[] times = server.worldTickTimes.get(inf.id);

            int counter = inf.tickCounter;
            if(counter < 0)counter = 0;
            times[counter % 100] = spend;
            worldserver.setMeanTT(mean(times, counter));

        } catch (Exception | StackOverflowError exception){
            onError(worldserver, exception);
        } catch (OutOfMemoryError error){
            long curTime = System.currentTimeMillis();
            if(curTime - lastError < 50){
                stopAnError(error, worldserver);
            }
            lastError = curTime;
            worldserver.cleanCache();
            CraftingManager.index.clear();
        } catch (Throwable throwable){
            stopAnError(throwable, worldserver);
        }
    }


    private static double mean(long[] values, int cur) {
        long sum = 0L;
        int min = cur - 3;
        int max = cur + 3;
        if(min < 0)min = 0;
        if(max >= 100){
            while (min != max){
                sum += values[min++ % 100];
            }
        } else {
            while (min != max){
                sum += values[min++];
            }
        }

        return ((double) sum / 7) * 1.0E-6D;
    }


    private static class ErrorTask implements Runnable{
        private final Throwable exception;

        private ErrorTask(Throwable exception) {
            this.exception = exception;
        }

        @Override
        public void run() {
            LOGGER.error("-------- CRITICAL SERVER ERROR ------------");
            if(exception instanceof ReportedException){
                CrashReport report = ((ReportedException)exception).getCrashReport();
                LOGGER.error("", report.getCrashCause());
                LOGGER.error("", exception);
            } else {
                LOGGER.error("", exception);
            }
            LOGGER.error("-------------------------------------------");
        }
    }


    public void onError(WorldServer world, Throwable throwable){
        if(ignoreErrors){
            service.execute(new ErrorTask(throwable));
        } else {
            stopAnError(throwable, world);
        }
    }


    private void stopAnError(Throwable throwable, WorldServer worldServer){
        enabled = false;
        error = throwable;
        CrashReport report = CrashReport.makeCrashReport(throwable, "Exception ticking world");
        if(worldServer != null) worldServer.addWorldInfoToCrashReport(report);
    }


    private void unload(WorldServer world, int id) {
        try {
            world.onUnload();
            if(world.getChunkProvider().canSave()) {
                world.saveAllChunks(true,null);
                world.getChunkProvider().flushToDisk();
            }
        } catch (Exception e) {
            onError(world, e);
        }
        MinecraftForge.EVENT_BUS.postF(new WorldEvent.Unload(world));
        DimensionManager.setWorld(id, null, this.server);
    }
}