package plus.fserver.world.addon.pvpworld;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plus.fserver.ElcaCV;
import plus.fserver.utls.FastValidators;
import plus.fserver.utls.collections.EntityRemSwapList;
import plus.fserver.world.addon.DumpChunkLoader;
import plus.fserver.world.addon.MemoryChunkProviderServer;
import plus.fserver.world.addon.MemoryWorld;
import plus.fserver.world.addon.pvpworld.stats.WorldData;
import plus.tson.utl.Te4HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;


public abstract class MinigameWorld extends MemoryWorld {
    private ConcurrentLinkedQueue<DelayTask> futureTasks = new ConcurrentLinkedQueue<>();
    private WorldData data;
    private Te4HashSet<UUID> awaitPlayers;
    private boolean finished = true;
    private WorldData.Results results;

    public MinigameWorld(MinecraftServer server, ISaveHandler handler, int id, WorldServer parent, Profiler profiler) {
        super(server, handler, id, parent, profiler);
    }


    public void cleanEntities() {
        cleanEntities(FastValidators.ALWAYS_TRUE);
    }


    public void cleanEntities(Predicate<Entity> func) {
        ChunkProviderServer cps = chunkProviderServer;
        for (EntityRemSwapList.E15<Entity> list:realEntityList.getRaw()) {
            Entity[] raw = list.raw();

            int size = list.sizeC();
            for (int cursor = 0; cursor < size; ++cursor) {
                Entity entity = raw[cursor];
                Entity ridding;

                if ((ridding = entity.getRidingEntity()) != null) {
                    if (!(ridding instanceof EntityPlayerMP && func.test(ridding)) && ridding.isPassenger(entity))
                        continue;
                    entity.dismountRidingEntity();
                }


                if (!(entity instanceof EntityPlayerMP) && func.test(ridding)) {
                    if (entity.addedToChunk) {
                        Chunk chunk = cps.getLoadedChunk(entity.chunkCoordX, entity.chunkCoordZ);
                        if(chunk != null) {
                            if(ElcaCV.ENABLE_CHUNK_ADD_LOCKS){
                                synchronized (chunk){
                                    chunk.removeEntity(entity); //sync
                                }
                            } else {
                                chunk.removeEntity(entity); //def
                            }
                        }
                    }
                    list.removeSwap(cursor--);
                    --size;
                    this.onEntityRemoved(entity);
                }
            }
        }
    }


    @Override
    protected boolean needMobSpawn() {
        return false;
    }


    @Override
    protected WorldEntitySpawner makeWorldSpawner() {
        return null;
    }


    public void onNewGame(EntityPlayerMP[][] players){
        data = controller().createData(this);
        futureTasks = new ConcurrentLinkedQueue<>();
        finished = false;
        Te4HashSet<UUID> awaitPlayers = this.awaitPlayers = new Te4HashSet<>();
        for (EntityPlayerMP[] team : players) {
            for (EntityPlayerMP player : team) {
                awaitPlayers.add(player.getUniqueID());
            }
        }
    }


    public void finish(){
        finished = true;
    }


    public boolean finished(){
        return finished;
    }


    public WorldData.Results results(){
        if(finished()){
            WorldData.Results results = this.results;
            if(results == null)return this.results = data().dumpResults();
        }
        return data().dumpResults();
    }


    public boolean isGameMember(EntityPlayerMP player){
        return data.containsKey(player.getUniqueID());
    }


    public List<EntityPlayerMP> getMembers(){
        WorldData data = this.data;
        List<EntityPlayerMP> players = new ArrayList<>();
        for (EntityPlayer player: this.playerEntities){
            if(player instanceof EntityPlayerMP && data.contains(player)){
                players.add((EntityPlayerMP) player);
            }
        }
        return players;
    }


    private static class DelayTask{
        private final Consumer<WorldServer> task;
        private final Consumer<WorldServer> onTick;
        private int delay;

        private DelayTask(Consumer<WorldServer> task, Consumer<WorldServer> onTick, int delay) {
            this.task = task;
            this.delay = delay;
            this.onTick = onTick;
        }

        public boolean run(WorldServer world){
            if(--delay <= 0){
                task.accept(world);
                return true;
            } else {
                onTick.accept(world);
            }
            return false;
        }
    }
//
//
//    public Te4HashMap<UUID, Te4HashMap> userData(){
//        return userData;
//    }


    public <T extends WorldData> T data(){
        return (T) data;
    }
//
//
//    public Te4HashMap data(EntityPlayerMP player){
//        UUID uuid = player.getUniqueID();
//        Te4HashMap<UUID, Te4HashMap> data = this.userData;
//        Te4HashMap result = data.get(uuid);
//        if(result == null){
//            data.fput(uuid, result = new Te4HashMap<>(8));
//        }
//        return result;
//    }
//
//
//    public Te4HashMap getDataIfExists(EntityPlayerMP player){
//        return userData.get(player.getUniqueID());
//    }
//
//
//    public void setWinner(EntityPlayerMP player){
//        data.fput(Vars.WINNER, player.getUniqueID());
//    }
//
//
//    public void setVar(Object key, Object value){
//        data.fput(key, value);
//    }
//
//
//    public <T> T getVar(Object key){
//        Object res = data.get(key);
//        if(res == null)return null;
//        return (T) res;
//    }
//
//
//    @Nullable
//    public EntityPlayerMP getWinner(){
//        UUID uuid = (UUID) data.get(Vars.WINNER);
//        if(uuid == null)return null;
//        Entity entity = getEntityFromUuid(uuid);
//        if(entity instanceof EntityPlayerMP){
//            return (EntityPlayerMP) entity;
//        }
//        return null;
//    }


    public void addDelayedTask(Consumer<WorldServer> task, int delay){
        addDelayedTask(task, TaskRegistry.empty(), delay);
    }


    public void addDelayedTask(Consumer<WorldServer> task, Consumer<WorldServer> onTick, int delay){
        futureTasks.add(new DelayTask(task, onTick, delay));
    }


    public void clearDelaydTasks(){
        futureTasks = new ConcurrentLinkedQueue<>();
    }


    @Override
    public void tick() {
        super.tick();

        Te4HashSet<UUID> awaitPlayers = this.awaitPlayers;
        if(awaitPlayers != null){
            if(awaitPlayers.isEmpty()){
                this.awaitPlayers = null;

                ArrayList<EntityPlayerMP> mpPlayers = new ArrayList<>(playerEntities.size());
                for (EntityPlayer player:playerEntities){
                    if(player instanceof EntityPlayerMP){
                        mpPlayers.add((EntityPlayerMP) player);
                    }
                }
                controller().onNewGameStarted(this, mpPlayers.toArray(new EntityPlayerMP[0]));
            } else {
                return;
            }
        }

        controller().onTick(this);

        Iterator<DelayTask> iter = futureTasks.iterator();
        while (iter.hasNext()){
            DelayTask next = iter.next();
            if(next == null)continue;
            if(next.run(this)) iter.remove();
        }
    }


    @Override
    public void onEntityAdded(Entity entityIn) {
        super.onEntityAdded(entityIn);
        Te4HashSet<UUID> awaitPlayers = this.awaitPlayers;
        if(awaitPlayers != null && entityIn instanceof EntityPlayerMP){
            awaitPlayers.remove(entityIn.getUniqueID());
        }
    }


    @Override
    protected Scoreboard createScoreBoard() {
        return controller().createScoreboard(this);
    }


    @Override
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return player.isCreative() && getMinecraftServer().getPlayerList().canSendCommands(player.getGameProfile());
    }


    public abstract MinigameController controller();


    @Override
    public IChunkProvider createChunkProvider() {
        MinigameController ctrl = controller();
        DumpChunkLoader loader = new DumpChunkLoader(ctrl.getMapNbt(), ctrl.getMapCache());
        return new MemoryChunkProviderServer(this, loader, createChunkGenerator());
    }


    @Override
    protected ITeleporter initTeleporter() {
        return controller().getTeleporter();
    }


    @Override
    public boolean onCommand(ICommandSender sender, @Nullable ICommand command, String cmdName, String[] args) {
        return controller().onCommand(this, sender, command, cmdName, args);
    }


    @Nullable
    @Override
    public List<String> onCompleteCommand(ICommandSender sender, @Nullable ICommand icommand, String[] args, @Nullable BlockPos pos) {
        return controller().onCompleteCommand(this, sender, icommand, args, pos);
    }


    @Override
    public boolean onDropItem(@NotNull ItemStack item, @NotNull EntityPlayer player) {
        return controller().onDropItem(this, item, player);
    }


    @Override
    public boolean onLivingDeath(EntityLivingBase entity, DamageSource src) {
        LivingDeathEvent event = livingDeathEvent;
        event.init(entity, src);
        MinecraftForge.EVENT_BUS.post(event);
        controller().onDeath(event);
        return event.isCanceled();
    }


    @Override
    public boolean canUnload() {
        return playerEntities.isEmpty() && _syncTasks.isEmpty() &&
                !provider.getDimensionType().shouldLoadSpawn();
    }
}