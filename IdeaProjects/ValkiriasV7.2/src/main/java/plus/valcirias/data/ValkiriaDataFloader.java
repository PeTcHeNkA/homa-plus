package plus.valcirias.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import plus.Main;
import plus.system.*;
import plus.valcirias.BaseValkiria;
import plus.valcirias.data.coder.*;
import plus.util.NextLvlTable;
import plus.valcirias.data.loader.ASyncLoader;
import plus.valcirias.data.loader.AbstractValkiriaLoader;
import plus.valcirias.data.loader.SyncLoader;
import plus.valcirias.data.loader.ValkiriaLoader;
import plus.system.teleporter.TeleportClient;
import plus.system.teleporter.TeleportServer;
import plus.valcirias.Valkiria;
import plus.util.YandeRePlugin;
import plus.util.cfg.Config;
import plus.util.sql.DataFloader;
import plus.util.sql.SqlSector;
import plus.util.sql.YandereDBSQLServer;
import java.util.Arrays;
import java.util.List;


public class ValkiriaDataFloader {

    private final DataFloader sql;
    public final ValkiriaEncoder encoder;
    public final ItemCoder itemCoder;
    public final QuerryClient redirector;
    public final TeleportServer tpServer;
    public final TeleportClient tpSender;
    public static String LobbyServer;
    private final int redirectorRate;
    public final boolean dungeonMode;

    public ValkiriaDataFloader(YandeRePlugin plugin, UserFloader f){
        Config c = plugin.getOrCreateYandeReConfig("floader_settings.yml");
        boolean useClient = c.getOrSetBoolean("main.use_client", false);
        boolean enableSqlHost = c.getOrSetBoolean("main.enable_sql_server", false);
        dungeonMode = c.getOrSetBoolean("main.dungeon_mode", false);
        boolean useLowLoader = c.getOrSetBoolean("main.use_lowloader", false);


        boolean enableTpServer = c.getOrSetBoolean("network.enable_tp_server", false);

        if(enableTpServer){
            tpServer = new TeleportServer(
                    c.getOrSetInt("network.tp_server_port", 7118)
            );
        } else tpServer = null;

        boolean enableTpSender = c.getOrSetBoolean("network.enable_tp_sender", false);
        redirectorRate = c.getOrSetInt("network.redirector_rate", 100);
        if(enableTpSender){
            tpSender = new TeleportClient(
                    c.getOrSetString("network.tp_server_name", "server"),
                    c.getOrSetInt("network.tp_sender_port", 7118)
            );
            try {
                for(String key:c.getConfigurationSection("network.tp_servers").getKeys(false)){
                    tpSender.addTeleportConnection(key, c.getInt("network.tp_servers."+key+".port", 7119));
                }
            } catch (NullPointerException ignored){}
        } else tpSender = null;

        LobbyServer = c.getOrSetString("main.lobby_name","lobby");


        int velocityPort = c.getOrSetInt("main.velocity_port",0);
        int port = c.getOrSetInt("main.port", 4971);
        List<String> collums = Arrays.asList("valkiria;weapons;artifacts;backpack;inventory;rewards".split(";"));
        if(useClient){
            System.out.println("| Used Client SQL!");
            //sql = new YandereDBClient("localhost", port);
            sql = new SqlClient1("localhost", port);
        } else {
            if(enableSqlHost){
                System.out.printf("| Host Sql Enabled! port: [%s]%n",port);
                sql = new YandereDBSQLServer("VALKIRIAS_DATA", "MAIN", collums, port);
            } else {
                System.out.println("| Used offline SQL");
                sql = new SqlSector("VALKIRIAS_DATA", "MAIN", collums);
            }
        }
        AbstractItemCoder ac = new VItemCoder();
        itemCoder = new ItemCoder(ac);


        if(dungeonMode) encoder = new DungeonValkiriaEncoder(itemCoder);
        else encoder = new StandartValkiriaEncoder(itemCoder);

        if(dungeonMode) {
            Bukkit.getPluginManager().registerEvents(new DungeonListeners(), Main.instance);
        }


        AbstractValkiriaLoader l;
        if(useLowLoader)l = new SyncLoader(f);
        else l = new ASyncLoader(f);
        new ValkiriaLoader(plugin,l);

        redirector = new QuerryClient("localhost",velocityPort);
    }


    public void Redirect(String player, String server){
        Valkiria v = Main.floader.getValkiria(player);
        if(v.getState()!= BaseValkiria.VState.REDIRECTING) {
            Thread t = new Thread(() -> {
                v.setState(BaseValkiria.VState.REDIRECTING);
                redirector.redirectPlayer(player, LobbyServer);
                try {
                    Thread.sleep(redirectorRate);
                } catch (Exception ignored) {
                }
                Main.floader.saveValkiria(player);
                try {
                    Thread.sleep(redirectorRate);
                } catch (Exception ignored) {
                }
                Main.floader.clearValkiriaCacheOf(player);
                redirector.redirectPlayer(player, server);
            });
            t.start();
        }
    }


    public Valkiria changeClassOfValkiria(Valkiria old, String newClass){
        Valkiria v = encoder.getGenerator().ofString(newClass);

        if(old.isPremium()){
            int experience = v.getExperience();
            int lvl = NextLvlTable.getLvl(experience);
            v.setExperience(experience);
            v.setAvailablePoints(v.getAvailablePoints()+lvl);
            v.setPremium(true);
        }

        v.setShield(old.getShield()*0.3);
        v.setArtifacts(old.getArtifacts());
        v.setBackPack(old.getBackPck());
        //v.setQuestHistory(old.getQuestHistory());
        //v.setHideQuest(old.getQuest());
        v.setQuestSession(old.getQuestSession());
        v.setPlayerName(old.toPlayer());
        v.updateAll();
        return v;
    }


    public Valkiria getValkiria(Player player){
        return encoder.encode(player, sql.getData(player.getName()));
    }


    public void putValkiria(Valkiria v){
        sql.putData(v.toPlayerName(), encoder.code(v));
    }


}