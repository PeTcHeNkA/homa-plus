package plus.fserver.world.addon.pvpworld;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import plus.fserver.world.addon.EmptyChunkGenerator;
import plus.fserver.world.addon.MemoryChunkProviderServer;
import plus.fserver.world.addon.ScoreboardEX;
import plus.fserver.world.addon.pvpworld.stats.CombatData;
import plus.tson.utl.Te4HashMap;
import plus.tson.utl.Te4HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;


public abstract class MinigameWorld extends WorldServerMulti {
    private ConcurrentLinkedQueue<DelayTask> futureTasks = new ConcurrentLinkedQueue<>();
    private CombatData data;
    private Te4HashSet<UUID> awaitPlayers;
    //private Te4HashMap<UUID,Te4HashMap> userData;
    //private Te4HashMap<UUID,Integer> prevDims = new Te4HashMap<>(4);
    private boolean finished = true;

    public MinigameWorld(MinecraftServer server, ISaveHandler handler, int id, WorldServer parent, Profiler profiler) {
        super(server, handler, id, parent, profiler);
    }


    public void onNewGame(EntityPlayerMP[][] players){
        data = controller().createData(this);
        //data     = new Te4HashMap<>();
        //userData = new Te4HashMap<>();
        futureTasks = new ConcurrentLinkedQueue<>();
        //prevDims = new Te4HashMap<>();
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


    public Te4HashMap<UUID,Integer> prevDimMap(){
        return prevDims;
    }


    public boolean isGameMember(EntityPlayerMP player){
        return prevDims.containsKey(player.getUniqueID());
    }


    public List<EntityPlayerMP> getMembers(){
        Te4HashMap<UUID, Integer> pd = prevDims;
        List<EntityPlayerMP> players = new ArrayList<>();
        for (EntityPlayer player: this.playerEntities){
            if(player instanceof EntityPlayerMP && pd.containsKey(player.getUniqueID())){
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


    public <T extends CombatData> T data(){
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
        return new ScoreboardEX();
    }


    @Override
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return player.isCreative() && getMinecraftServer().getPlayerList().canSendCommands(player.getGameProfile());
    }


    public abstract MinigameController controller();


    @Override
    public IChunkProvider createChunkProvider() {
        return new MemoryChunkProviderServer(this, new MinigameChunkLoader(controller()), createChunkGenerator());
    }


    @Override
    public IChunkGenerator createChunkGenerator() {
        return new EmptyChunkGenerator(this);
    }


    @Override
    public ITeleporter getDefaultTeleporterRaw() {
        return controller().getTeleporter();
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