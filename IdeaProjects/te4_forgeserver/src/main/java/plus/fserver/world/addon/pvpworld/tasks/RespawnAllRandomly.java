package plus.fserver.world.addon.pvpworld.tasks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import plus.fserver.utls.math.FastRandom;
import plus.fserver.world.addon.CleanLobbyTeleporter;
import plus.fserver.world.addon.LobbyTeleporter;
import plus.fserver.world.addon.pvpworld.MinigameWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class RespawnAllRandomly implements Consumer<WorldServer> {
    private final LobbyTeleporter[] teleporters;

    public RespawnAllRandomly(LobbyTeleporter[] teleporters) {
        this.teleporters = teleporters;
    }


    @Override
    public void accept(WorldServer world) {
        ArrayList<EntityPlayer> players = new ArrayList<>(world.playerEntities);

        LobbyTeleporter[] arr = this.teleporters.clone();
        FastRandom.shuffle(world.rand, arr, arr.length);
        int i = 0;

        for (EntityPlayer player:players){
            if(player instanceof EntityPlayerMP){
                placeEntity(arr[i], (EntityPlayerMP) player);
                ++i;
                if(i > arr.length)i = 0;
            }
        }
    }


    public void respawn(WorldServer world, List<EntityPlayerMP> players){
        if(players.isEmpty())return;
        LobbyTeleporter[] arr = this.teleporters.clone();
        FastRandom.shuffle(world.rand, arr, arr.length);
        int i = 0;

        for (EntityPlayerMP player:players){
            placeEntity(arr[i], player);
            ++i;
            if(i > arr.length)i = 0;
        }
    }


    public void placeEntity(LobbyTeleporter teleporter, EntityPlayerMP player){
        teleporter.placeEntity(player.world, player, player.rotationYaw);
        CleanLobbyTeleporter.cleanPlayer(player);
        ((MinigameWorld)player.world).controller().onAddPlayer(player);
    }
}