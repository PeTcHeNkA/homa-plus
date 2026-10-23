package plus.timidblocks;


import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import plus.tson.*;
import java.io.File;
import java.util.Collections;
import static plus.tson.ext.TsonAccessUtils.*;


/**
 * Плагин на пугливые блоки
 * Если сказать название блока в слух - он пропадет (необходимо установить связанный мод на сервер)
 * @author HomaPlus
 * Разработано по заказу Nuko02. Не подлежит перепродаже
 */
public final class TimidBlocks extends JavaPlugin implements PluginMessageListener, Listener {
    private UserSoundManager userSoundManager;
    private boolean debug;
    private boolean allPlayers;
    private int remRange;

    @Override
    public void onEnable() {
        TsonMap map = readConfig("config.tson");
        if(map == null){
            onConfigError(null);
            return;
        }

        userSoundManager = new UserSoundManager(this,
                getR(map, "main.model-dir", TsonObj.Of("model")).getStr(),
                getR(map, "main.max-words", 4),
                getR(map, "main.audio-rate", 22050),
                getR(map, "pool.threads", 1),
                getR(map, "pool.max-threads", 3)
        );
        reloadConfig(map);

        getServer().getMessenger().registerIncomingPluginChannel(this, "timidblocks:sound_chan", this);
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("[TimidBlocks] Plugin Enabled");
    }


    @Override
    public void reloadConfig() {
        TsonMap map = readConfig("config.tson");
        if(map == null){
            onConfigError(null);
        }
        reloadConfig(map);
    }


    private void reloadConfig(TsonMap map) {
        debug = getR(map, "main.debug", false);
        allPlayers = getR(map, "main.all-players", false);
        if(userSoundManager != null){
            userSoundManager.loadWordData(map.getMap("words"));
        }
        remRange = getR(map, "main.rem-range", 0);
    }


    /**
     * Прочитать конфиг Tson 2.0 (с поддержкой рефлектора и скриптов)
     * @param path путь относительно папки плагина
     */
    public TsonMap readConfig(String path){
        try {
            return new STsonParser(TsonFile.remBadChars(TsonFile.read(new File(getDataFolder(), path).getPath()))).getMap();
        } catch (Exception e){
            onConfigError(e);
        }
        return null;
    }


    /**
     * Писать ли отладочную информацию в консоль?
     */
    public boolean isDebug(){
        return debug;
    }


    /**
     * Ломать ли блоки вокруг каждого игрока?
     */
    public boolean isAllPlayers(){
        return allPlayers;
    }


    public  int getRemRange(){
        return remRange;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("reload-words-data")) {
            reloadConfig();
            if(args.length == 2 && args[0].equals("test")) {
                userSoundManager.onSayBlock(((Player)sender).getUniqueId(), Collections.singleton(Material.getMaterial(args[1])));
            }
        }
        return false;
    }


    private void onConfigError(Exception e){
        getLogger().severe("Error: config.tson could not be loaded");
        if(e != null)
            getLogger().severe(e.getMessage());
    }


    @Override
    public void onDisable() {
        userSoundManager.clearAll();
    }


    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        //чтение модового аудио-канала
        if(s.equals("timidblocks:sound_chan"))
            onSoundReceived(player, bytes);
    }


    /**
     * Очистка сессии после отключения игрока
     */
    @EventHandler
    public void onLogout(PlayerQuitEvent event){
        userSoundManager.clearSession(event.getPlayer().getUniqueId());
    }


    private void onSoundReceived(Player player, byte[] bytes) {
        userSoundManager.onSound(player, bytes);
    }
}