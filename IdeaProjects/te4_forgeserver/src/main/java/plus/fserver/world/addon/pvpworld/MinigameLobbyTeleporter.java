package plus.fserver.world.addon.pvpworld;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import plus.fserver.world.addon.CleanLobbyTeleporter;


public class MinigameLobbyTeleporter extends CleanLobbyTeleporter {
    private final MinigameController arena;

    public MinigameLobbyTeleporter(MinigameController arena) {
        this.arena = arena;
    }


    @Override
    public void onPostTransfer(EntityPlayerMP player, int dimId) {
        arena.onAddPlayer(player);
    }


    @Override
    public void onRollbackInventory(WorldServer prev, EntityPlayerMP player) {
        arena.onRemPlayer((MinigameWorld) prev, player);
    }


    @Override
    public boolean onRemoveOnTransfer(EntityPlayerMP player, int oldId, int dimId) {
        try {
            arena.onPreRemPlayer(player);
        } catch (Exception e){
            e.printStackTrace();
        }
        return super.onRemoveOnTransfer(player, oldId, dimId);
    }
}