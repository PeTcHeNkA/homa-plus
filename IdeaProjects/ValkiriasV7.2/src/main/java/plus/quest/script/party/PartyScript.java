package plus.quest.script.party;

import plus.quest.script.dungeon.PartyQuest;
import plus.quest.script.dungeon.SimplePartyQuest;
import plus.quest.script.quest.QuestScriptFactory;
import plus.quest.script.quest.QuestScript;
import plus.quest.QuestConfig;
import plus.util.factory.AbstractFactory;
import plus.util.cfg.Sector;
import plus.valcirias.DungeonValkiria;


public class PartyScript implements AbstractPartyScript{

    public static final AbstractFactory<AbstractPartyScript> factory = define();

    private static AbstractFactory<AbstractPartyScript> define() {
        AbstractFactory<AbstractPartyScript> f = new AbstractFactory<>() {
            @Override
            protected void onCreate(Sector docx){
                docx.set("quests.example_party_quest.on_complete.type", "container");
            }

            @Override
            protected void onAddLabel(Sector s, String label) {
                s.set("quests.example_party_quest.on_complete.scripts.example_"+label+".type", label);
            }
            @Override
            protected void onFinish(Sector docx){
                new SimplePartyQuest(docx, "quests.example_party_quest");
                new PartyQuest(docx, "quests.example2_party_quest");
            }
        };
        f.add("script", PartyScript::new);
        f.add("sleep", ScriptPartySleep::new);
        f.add("scene", ScriptPartyScene::new);
        f.add("memory", ScriptPartyMemory::new);
        f.add("container", PartyScriptContiner::new);

        f.add("sync_script", (s, path) -> new AbstractPartyScript() {
            private final QuestScript script = QuestScriptFactory.getScript(s, path + ".script");
            @Override
            public void run(PartySelector selector) {
                script.runScript(null);
            }
        });

        f.add("complete", (s, path) -> selector -> selector.getQuest().complete());
        return f;
    }


    public static AbstractPartyScript getScript(Sector s, String path){
        String type;
        if(s instanceof QuestConfig c)type = c.getScriptType(path, "script");
        else type = s.getOrSetString(path+".type", "script");
        return factory.build(s, path, type);
    }


    private final QuestScript script;

    public PartyScript(Sector s, String path){
        script = QuestScriptFactory.getScript(s, path+".script");
    }


    @Override
    public void run(PartySelector selector) {
        for(DungeonValkiria v:selector.getSessions()){
            script.runScript(v.getQuestSession());
        }
    }
}