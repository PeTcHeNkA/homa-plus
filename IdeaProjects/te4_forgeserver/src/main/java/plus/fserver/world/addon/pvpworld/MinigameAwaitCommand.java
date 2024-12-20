package plus.fserver.world.addon.pvpworld;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import plus.fserver.api.ServerCommand;
import java.util.UUID;


public class MinigameAwaitCommand extends ServerCommand {
    private final Object2ObjectOpenHashMap<String, MinigameAwaitRoom> rooms = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<UUID  , MinigameAwaitRoom> uuids = new Object2ObjectOpenHashMap<>();

    public MinigameAwaitCommand(String name, String usage, int permLvl) {
        super(name, usage, permLvl);
    }


    public void addGame(String game, String... ids){
        MinigameProvider.Map.Info[] infs = new MinigameProvider.Map.Info[ids.length];
        int i = 0;
        for (String id:ids){
            MinigameProvider.Map.Info inf = MinigameProvider.instance().map(id).info();
            infs[i] = inf;
            ++i;
        }
        rooms.put(game, new MinigameAwaitRoom(infs));
    }


    public void remUUID(UUID uuid){
        Object2ObjectOpenHashMap<UUID, MinigameAwaitRoom> uuids = this.uuids;
        synchronized (uuids) {
            MinigameAwaitRoom room = uuids.remove(uuid);
            if (room != null) synchronized (room){
                room.remove(uuid);
            }
        }
    }


    private void addUUID(UUID uuid, MinigameAwaitRoom room){
        Object2ObjectOpenHashMap<UUID, MinigameAwaitRoom> uuids = this.uuids;
        synchronized (uuids) {
            uuids.put(uuid, room);
        }
    }


    @Override
    public void execute(MinecraftServer src, ICommandSender sender, String[] args)   {
        src.ticker().runAsync(() -> {
            try {
                execAsync(src, sender, args);
            } catch (CommandException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void execAsync(MinecraftServer src, ICommandSender sender, String[] args) throws CommandException{
        String cmd1 = args[0];
        switch (cmd1){
            case "join":{
                join(src, (EntityPlayerMP) sender, args);
                return;
            }
        }
    }


    private void join(MinecraftServer serv, EntityPlayerMP player, String[] args){
        MinigameAwaitRoom room = rooms.get(args[1]);
        if(room == null){
            reply(player, "&cRoom not founded!");
            return;
        }
        synchronized (room){
            onRoomCmd(room, serv, player, args);
        }
    }


    protected void onRoomCmd(MinigameAwaitRoom room, MinecraftServer serv, EntityPlayerMP player, String[] args){
        UUID uuid;
        if(room.tryAdd(uuid = player.getUniqueID())){
            addUUID(uuid, room);
            room.remOfflinePlayers(serv);

            if(room.canStart()){
                if(!startRoom(serv, room)){
                    room.remOfflinePlayers(serv);
                } else {
                    return;
                }
            }
            notifyAddPlayer(serv, room, player);
        } else {
            reply(player, "&cCant join to room! try again later");
        }
    }


    protected void notifyAddPlayer(MinecraftServer serv, MinigameAwaitRoom room, EntityPlayerMP player){
        reply(player, "&eRoom founded! %s/%s players", room.getCurPlayers(), room.getNeedPlayers());
        room.forPlayers(serv, cur -> {
            if(cur == player)return;
            reply(cur, "Players to start: %s/%s", room.getCurPlayers(), room.getNeedPlayers());
        });
    }


    protected boolean startRoom(MinecraftServer serv, MinigameAwaitRoom room){
        MinigameProvider.Map.Info info = room.nextInfo();
        UUID[][] teams = room.dumpPlayersAndClean();

        EntityPlayerMP[][] playerMPS = playersOf(serv, teams);
        if(playerMPS == null)return false;

        info.controller().startNewGame(serv, playerMPS, null);
        return true;
    }


    protected static EntityPlayerMP[][] playersOf(MinecraftServer server, UUID[][] teams){
        PlayerList pl = server.getPlayerList();
        EntityPlayerMP[][] players = new EntityPlayerMP[teams.length][teams[0].length];

        for (int i = 0, l2 = teams[0].length; i < teams.length; i++){
            for (int j = 0; j < l2; j++){
                EntityPlayerMP player = pl.getPlayerByUUID(teams[i][j]);
                if(player == null)return null;
                players[i][j] = player;
            }
        }
        return players;
    }
}