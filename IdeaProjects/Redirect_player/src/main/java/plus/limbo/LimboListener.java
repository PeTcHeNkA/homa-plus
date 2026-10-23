package plus.limbo;

import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.GameMode;
import net.elytrium.limboapi.api.player.LimboPlayer;
import java.util.HashMap;
import java.util.Objects;


public class LimboListener implements LimboSessionHandler {


    protected static HashMap<String, LimboPlayer> players = new HashMap<>();


    public static LimboPlayer getLimboPlayer(String nick){
        return players.get(nick);
    }


    public static LimboPlayer removeLimboPlayer(String nick){
        //LimboPlayer r = players.get(nick);
        //if(r == null)return null;
        //r.disconnect();
        return players.remove(nick);
    }


//    public static void removeLimboPlayer(String nick, RegisteredServer server){
//        LimboPlayer r = players.get(nick);
//        if(r == null)return;
//        r.disconnect(server);
//        players.remove(nick);
//    }


    public final LimboSessionHandler h;
    public final boolean df;
    public final GameMode gm;


    public LimboListener(boolean disableFall, Integer gameMode, LimboSessionHandler handler){
        df = disableFall;
        h = handler;
        if(gameMode == null){
            gm = GameMode.SURVIVAL;
        } else {
            GameMode g = GameMode.getById(gameMode);
            gm = g == null ? GameMode.SURVIVAL : g;
        }
    }


    public LimboListener(boolean disableFall, GameMode gameMode, LimboSessionHandler handler){
        df = disableFall;
        h = handler;
        gm = Objects.requireNonNullElse(gameMode, GameMode.SURVIVAL);
    }


    public void onSpawn(Limbo server, LimboPlayer player) {
        if(df) player.disableFalling();
        player.setGameMode(gm);
        h.onSpawn(server, player);
        players.put(player.getProxyPlayer().getUsername(), player);
    }


    public void onMove(double x, double y, double z) {
        h.onMove(x, y, z);
    }


    public void onMove(double x, double y, double z, float yaw, float pitch) {
        h.onMove(x, y, z, yaw, pitch);
    }


    public void onRotate(float yaw, float pitch) {
        h.onRotate(yaw, pitch);
    }


    public void onGround(boolean onGround) {
        h.onGround(onGround);
    }


    public void onTeleport(int teleportId) {
        h.onTeleport(teleportId);
    }


    public void onChat(String chat) {
        h.onChat(chat);
    }


    public void onGeneric(Object packet) {
        h.onGeneric(packet);
    }


    public void onDisconnect() {

        h.onDisconnect();
    }
}