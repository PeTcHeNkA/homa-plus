package plus.timidblocks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import plus.tson.*;
import java.io.File;
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
                getR(map, "main.words-queue", 2),
                getR(map, "main.audio-rate", 22050),
                getR(map, "pool.threads", 1),
                getR(map, "pool.max-threads", 3)
        );

        debug = getR(map, "main.debug", false);
        userSoundManager.loadWordData(map.getMap("words"));

        getServer().getMessenger().registerIncomingPluginChannel(this, "timidblocks:sound_chan", this);
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("[TimidBlocks] Plugin Enabled");
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


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(command.getName().equals("reload-words-data")) {
            TsonMap map = readConfig("config.tson");
            if(map == null){
                onConfigError(null);
            }
            debug = getR(map, "main.debug", false);
            userSoundManager.loadWordData(map.getMap("words"));
            return true;
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
    public void onPluginMessageReceived(@NotNull String s, @NotNull Player player, @NotNull byte[] bytes) {
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


    private void onSoundReceived(@NotNull Player player, byte[] bytes) {
        userSoundManager.onSound(player, bytes);
    }
}