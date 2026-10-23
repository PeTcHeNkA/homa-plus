package plus.system;

import org.bukkit.Bukkit;
import plus.*;
import plus.block.ScriptBlock;
import plus.command.*;
import plus.util.Reloadable;
import plus.util.YandeRePlugin;
import plus.util.chat.NPCProfile;
import plus.item.IItemGenerator;
import plus.valcirias.avatar.AvatarFloader;
import plus.chest.ChestFolder;
//import plus.chest.reward.RewardBoxFolder;
import plus.util.ConfigHelper;
import plus.util.NextLvlTable;
import plus.block.teleport.GuiTeleport;
import plus.mythic.MSLoad;
import plus.mythic.MythicExpBridge;
import plus.block.AirBoxManager;
import plus.block.PowerEvents;
import plus.block.StationManager;
import plus.util.cfg.Config;
import plus.util.packet.PLManager;
import plus.util.packet.PacketManager;
import plus.quest.QuestConfig;
import plus.quest.Scene;
import plus.mythic.VESkills;
import plus.valcirias.data.ClassStats;


public class ModuleLoader {

    public static ChestFolder ChestC;

    public ModuleLoader(YandeRePlugin p){
        try {
            PacketManager.setGlobal(new PLManager());
            Scene.setWorkConfig(new QuestConfig(Main.instance, "scenes.yml"));
            new ViewEngineCommands();
            Bukkit.getPluginManager().registerEvents(new VESkills(), Main.instance);
            System.out.printf("| View engine enabled!%n");
        } catch (Error err){
            err.printStackTrace();
        }


        Config c = p.getOrCreateYandeReConfig("floader_settings.yml");
        boolean enableLootBoxes = c.getOrSetBoolean("main.enable_loot_chests", true);
        boolean enableMythicExp = c.getOrSetBoolean("main.enable_mythic_exp", true);
        boolean enableAirBoxes = c.getOrSetBoolean("main.enable_air_boxes", true);
        //boolean enableTeleportServer = c.getOrSetBoolean("main.enable_teleport_server", false);

//        if(enableTeleportServer){
//            new TeleportServer(c.getOrSetInt("main.teleport_server_port", 7216));
//        }
        //public final TeleportServer tpServer;
        ClassStats.define();
        ConfigHelper.Initialize();
        GuiTeleport.defineStatic();
        Main.generator = new IItemGenerator();
        NextLvlTable.Initialize();
        AvatarFloader.Initialize();
        Reloadable.add(() -> {
            ConfigHelper.Initialize();
            GuiTeleport.defineStatic();
            AvatarFloader.Initialize();
        });

        if(c.getOrSetBoolean("main.enable_craft_module", true)) CraftModule.Init();


        new AvatarCommands();


        new ChatCommand();
        new ResendCommand();

        if(enableLootBoxes){
            ChestC = new ChestFolder(p);
            StationManager.Initialize(p.getOrCreateYandeReConfig("charging.yml"));
            Bukkit.getPluginManager().registerEvents(new PowerEvents(), p);
            if(enableAirBoxes){
                AirBoxManager airBoxManager = new AirBoxManager(p);
            }
        }
        if(c.getOrSetBoolean("main.enable_script_blocks", true)){
            ScriptBlock.define();
        }

        if(enableMythicExp) {
            try {
                Bukkit.getPluginManager().registerEvents(new MythicExpBridge(), p);
            } catch(Exception err){
                System.out.println("MythicMobs is exist! provider Mythic experience not enabled.");
            }
        }

        int delayAutoSave = c.getOrSetInt("main.auto_save_delay", -1);
        if(delayAutoSave > 0){
            new AutoSaveModule(delayAutoSave);
        }

        if(c.getOrSetBoolean("main.enable_party", true)){
            new PartyCommands();
        }

        //if(enableQuests){
        NPCProfile.Initialize(p.getOrCreateYandeReConfig("npcs.yml"));
            //QuestLib.Initialize(p.getOrCreateYandeReConfig("quests.yml"));
            //Bukkit.getPluginManager().registerEvents(new questInteract(floader), p);
            //p.getCommand("quest").setExecutor(new questCommand(floader));

        ControlCommands ct = new ControlCommands(c);
//        boolean enableRewards = c.getOrSetBoolean("main.enable_quest_engine", true);
//
//        if(enableRewards){
//            RewardBoxFolder f = new RewardBoxFolder(p);
//        }
        Bukkit.getPluginManager().registerEvents(new MSLoad(), p);
    }
}
