package plus.fserver.world.addon.pvpworld;

import net.minecraft.entity.player.EntityPlayerMP;
import plus.fserver.world.addon.CleanLobbyTeleporter;


public class PvpLobbyTeleporter extends CleanLobbyTeleporter {
    private final PvpArena arena;

    public PvpLobbyTeleporter(PvpArena arena) {
        this.arena = arena;
    }

    @Override
    public void onCleanInventory(EntityPlayerMP player) {
        arena.onAddPlayer(player);
    }


    @Override
    public void onRollbackInventory(EntityPlayerMP player) {
        arena.onRemPlayer(player);
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