package plus.fserver.world.addon.pvpworld;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.util.ITeleporter;
import plus.fserver.world.addon.EmptyChunkGenerator;
import plus.fserver.world.addon.MemoryChunkProviderServer;
import plus.fserver.world.addon.ScoreboardEX;


public abstract class PvpWorld extends WorldServerMulti {
    public PvpWorld(MinecraftServer server, ISaveHandler handler, int id, WorldServer parent, Profiler profiler) {
        super(server, handler, id, parent, profiler);
    }


    @Override
    public void tick() {
        super.tick();
        getArena().onTick(this);
    }


    @Override
    protected Scoreboard createScoreBoard() {
        return new ScoreboardEX();
    }


    @Override
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return player.isCreative() && getMinecraftServer().getPlayerList().canSendCommands(player.getGameProfile());
    }


    abstract PvpArena getArena();


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
    public void onEntityRemoved(Entity entityIn) {
        super.onEntityRemoved(entityIn);
        if(entityIn instanceof EntityPlayerMP){
            getArena().onDeath((EntityPlayerMP) entityIn);
        }
    }


    @Override
    public boolean canUnload() {
        return playerEntities.isEmpty() && _syncTasks.isEmpty() &&
                !provider.getDimensionType().shouldLoadSpawn();
    }
}