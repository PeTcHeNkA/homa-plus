package plus.quest;

import org.bukkit.configuration.ConfigurationSection;
import plus.Main;
import plus.block.ScriptBlock;
import plus.quest.script.dungeon.PartyQuest;
import plus.quest.script.dungeon.SimplePartyQuest;
import plus.quest.script.party.PartyScript;
import plus.quest.script.quest.QuestScript;
import plus.quest.script.scene.ScriptFactory;
import plus.quest.script.quest.listener.CommandListener;
import plus.quest.script.quest.listener.RegionListener;
import plus.quest.script.quest.QuestScriptFactory;
import plus.quest.script.quest.trigger.STrigger;
import plus.quest.script.quest.trigger.Trigger;
import plus.util.factory.AbstractFactory;
import plus.util.cfg.Config;
import plus.util.cfg.Sector;
import java.util.ArrayList;
import java.util.HashMap;


public class Quest{

    private static final AbstractFactory<Quest> factory = defineFactory();

    private static AbstractFactory<Quest> defineFactory() {
        AbstractFactory<Quest> f = new AbstractFactory<>() {
            @Override
            protected void onAddLabel(Sector docx, String label) {
                docx.set("quest_types.example_"+label+".type", label);
            }
        };
        f.add("basic", Quest::new);
        f.add("party", PartyQuest::new);
        f.add("s_party", SimplePartyQuest::new);
        return f;
    }

    private static final HashMap<Integer, Quest> questLib = new HashMap<>();

    public static void define(){
        Config s = new QuestConfig(Main.instance, "RPG_QUESTS.yml");
        Main.instance.registerReloadable(s);

        if(!Config.maybeLoaded(Main.instance, "DOCX.yml")) {
            QuestScriptFactory.factory.createDocx();
            ScriptFactory.factory.createDocx();
            PartyScript.factory.createDocx();
            Trigger.factory.createDocx();
            factory.createDocx();
        }

        CommandListener.define();
        RegionListener.define();
        Trigger.define(s);
        loadQuests(s);
        //ScriptBlock.define();
    }


    private static void loadQuests(Sector s){
        System.out.printf("| < Start loading quests >%n");
        questLib.clear();
        ConfigurationSection section = s.getConfigurationSection("quests");
        if(section!=null) {
            for(String key:section.getKeys(false)){
                try {
                    String lPath = "quests."+key;
                    String type;
                    if(s instanceof QuestConfig c){
                        type = c.getScriptType(lPath, "basic");
                    } else type = s.getOrSetString(lPath, "basic");

                    Quest quest = factory.build(s, lPath, type);
                    questLib.put(quest.getId(), quest);
                    //System.out.println("| quest for id "+quest.getId()+" loaded!");
                } catch (Exception e){
                    System.out.printf("| Error in loading quest! [%s]%n", key);
                    e.printStackTrace();
                }
            }
        }
        System.out.printf("| loaded %s quests!%n", questLib.size());
    }


    public static void reload(){
        Config s = Main.instance.getOrCreateYandeReConfig("RPG_QUESTS.yml");
        s.reload();
        Trigger.define(s);
        loadQuests(s);
    }


    public static Quest valueOf(int id){
        return questLib.get(id);
    }


    public final int ID;
    public final int repeatTime;
    public final STrigger Default;
    public final QuestScript onNeedCompete;
    public final int[] needCompleteQuests;

    public Quest(Sector s, String path) {
        int id = s.getOrSetInt(path+".quest_id", questLib.size());
        String data = s.getOrSetString(path+".need_complete", "none");
        if(data.equalsIgnoreCase("none")){
            needCompleteQuests = new int[0];
            onNeedCompete = null;
        } else {
            ArrayList<Integer> questIds = new ArrayList<>();
            for(String str:data.split(",")){
                questIds.add(Integer.parseInt(str.strip()));
            }
            needCompleteQuests = new int[questIds.size()];
            for(int i=0;i<questIds.size();i++){
                needCompleteQuests[i] = questIds.get(i);
            }
            onNeedCompete = QuestScriptFactory.getScript(s, path+".on_need_complete");
        }

        repeatTime = s.getOrSetInt(path+".repeat_time", questLib.size());
        Default = Trigger.getOf(s, path+".triggers.main");

        boolean edited = false;
        while (questLib.containsKey(id)){
            if(!edited)edited = true;
            id+=1;
        }
        if(edited){
            s.set(path+".quest_id", id);
            s.saveChanges();
        }
        ID = id;
    }


    public int repeatTime(){
        return repeatTime;
    }


    public int[] needComplete(){
        return needCompleteQuests;
    }


    public String toString(){
        return String.valueOf(ID);
    }


    public int getId(){
        return ID;
    }


    public void onTake(AbstractQuestSession session){
        Default.load(session);
    }
}