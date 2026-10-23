package plus.fserver.world.addon;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;


public class CleanLobbyTeleporter extends LobbyTeleporter{
    @Override
    public boolean onPreTransfer(EntityPlayerMP player, int dimId) {
        l1: try {
            MinecraftServer server = player.getServer();

            int oldId = player.dimension;
            WorldServer worldserver = server.getWorld(oldId);
            if(worldserver.getDefaultTeleporterRaw().onRemoveOnTransfer(player, oldId, dimId))break l1;

            WorldServer newWorld = PlayerList.procTransferPhase1(worldserver, player, dimId);

            server.ticker().runAsync(() -> {
                server.getPlayerList().writePlayerData(player);
                player.setNeedSave(false);

                newWorld.addSyncTask(() -> {
                    player.setNeedSave(false);
                    cleanPlayer(player);
                    onCleanInventory(player);

                    PlayerList.procTransferPhase2(worldserver, newWorld, player, oldId, dimId, CleanLobbyTeleporter.this);

                    CleanLobbyTeleporter.this.onPostTransfer(player, dimId);
                });
            });
        } catch (Throwable e){
            e.printStackTrace();
        }
        return true;
    }


    public static void cleanPlayer(EntityPlayerMP player){
        player.inventory.clear();
        player.clearActivePotions();
        player.extinguish();
        player.setGlowing(false);
        player.setInvisible(false);
        player.setHealth(player.getMaxHealth());
    }


    public void onCleanInventory(EntityPlayerMP player){}


    public void onRollbackInventory(WorldServer prev, EntityPlayerMP player){}


    @Override
    public boolean onRemoveOnTransfer(EntityPlayerMP player, int oldId, int dimId) {
        try {
            MinecraftServer server = player.getServer();
            WorldServer worldserver = server.getWorld(oldId);

            WorldServer newWorld = PlayerList.procTransferPhase1(worldserver, player, dimId);

            server.ticker().runAsync(() -> {
                server.getPlayerList().readPlayerDataFromFile(player);
                player.setNeedSave(true);

                newWorld.addSyncTask(() -> {
                    player.setNeedSave(true);
                    onRollbackInventory(worldserver, player);

                    PlayerList.procTransferPhase2(worldserver, newWorld, player, oldId, dimId, CleanLobbyTeleporter.this);
                });
            });
        } catch (Throwable e){
            e.printStackTrace();
        }
        return true;
    }
}