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
import plus.tson.utl.Te4HashMap;
import plus.tson.utl.Te4HashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public abstract class PvpWorld extends WorldServerMulti {
    private Te4HashMap<String,?> data;
    private Te4HashSet<UUID> awaitPlayers;

    public PvpWorld(MinecraftServer server, ISaveHandler handler, int id, WorldServer parent, Profiler profiler) {
        super(server, handler, id, parent, profiler);
    }

    public void onNewGame(EntityPlayerMP[] players){
        data = new Te4HashMap<>();
        Te4HashSet<UUID> awaitPlayers = this.awaitPlayers = new Te4HashSet<>();
        for (EntityPlayerMP player : players) {
            awaitPlayers.add(player.getUniqueID());
        }
    }


    @Override
    public void tick() {
        super.tick();
        getArena().onTick(this);
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
                getArena().onStartNewGame(this, mpPlayers.toArray(new EntityPlayerMP[0]));
            }
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


    public abstract PvpArena getArena();


    @Override
    public IChunkProvider createChunkProvider() {
        return new MemoryChunkProviderServer(this, new PvpChunkLoader(getArena()), createChunkGenerator());
    }


    @Override
    public IChunkGenerator createChunkGenerator() {
        return new EmptyChunkGenerator(this);
    }


    @Override
    public ITeleporter getDefaultTeleporterRaw() {
        return getArena().getTeleporter();
    }


    @Override
    public boolean onLivingDeath(EntityLivingBase entity, DamageSource src) {
        LivingDeathEvent event = livingDeathEvent;
        event.init(entity, src);
        MinecraftForge.EVENT_BUS.post(event);
        getArena().onDeath(event);
        return event.isCanceled();
    }


    @Override
    public boolean canUnload() {
        return playerEntities.isEmpty() && _syncTasks.isEmpty() &&
                !provider.getDimensionType().shouldLoadSpawn();
    }
}