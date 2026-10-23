package plus.quest.script.quest;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.block.teleport.GuiTeleport;
import plus.quest.*;
import plus.quest.script.dungeon.ScriptJoinToSingleScript;
import plus.quest.script.dungeon.ScriptRunForSelector;
import plus.quest.script.quest.logic.Mather;
import plus.quest.script.quest.trigger.ScriptBlockedProtectedContainer;
import plus.quest.script.system.ScriptClearMobs;
import plus.quest.script.system.ScriptMMSpawn;
import plus.quest.script.system.ScriptQuestContainer;
import plus.quest.script.system.ScriptSleep;
import plus.util.cfg.Sector;
import plus.util.factory.AbstractFactory;
import plus.quest.entity.ActorScript;
import plus.quest.motion.TpMotion;
import plus.quest.script.quest.logic.Conduction;
import plus.quest.script.scene.*;
import plus.valcirias.DungeonValkiria;
import plus.valcirias.Valkiria;


public class QuestScriptFactory {
    public static final AbstractFactory<QuestScript> factory = define();

    private static AbstractFactory<QuestScript> define() {
        AbstractFactory<QuestScript> f = new AbstractFactory<>() {
            @Override
            protected void onAddLabel(Sector s, String label) {
                s.set("quests.example.triggers.main.scripts.example_"+label+".type", label);
            }
            @Override
            protected void onFinish(Sector docx){
                new Quest(docx, "quests.example");
            }
        };

        f.add("none", (c, path) -> (UniversalScript) player -> {});
        f.add("heal", (c, path) -> new ScriptHeal());
        f.add("chat", ScriptChat::new);
        f.add("js", ScriptJSModule::new);
        f.add("sleep", ScriptSleep::new);
        f.add("actor", ActorScript::new);
        f.add("scene", ScriptScene::new);
        f.add("title", ScriptTitle::new);
        f.add("reward", ScriptReward::new);
        f.add("damage", ScriptDamage::new);
        f.add("command", ScriptCommand::new);
        f.add("mm_spawn", ScriptMMSpawn::new);
        f.add("title_s", ScriptTitlePlus::new);
        f.add("redirect", ScriptRedirect::new);
        f.add("player_tp", PhysicTeleport::new);
        f.add("json_text", ScriptJsonText::new);
        f.add("mem_edit", ScriptEditMemory::new);
        f.add("clear_mobs", ScriptClearMobs::new);
        f.add("action_bar", ScriptActionBar::new);
        f.add("take_bp", ScriptTakeBattlePass::new);
        f.add("hide_player", ScriptHidePlayer::new);
        f.add("container", ScriptQuestContainer::new);
        f.add("load_trigger", ScriptLoadTrigger::new);
        f.add("p_container", ScriptProtectedContainer::new);
        f.add("run_for_selector", ScriptRunForSelector::new);
        f.add("bp_container", ScriptBlockedProtectedContainer::new);
        f.add("join_to_single_script", ScriptJoinToSingleScript::new);

        f.add("exec", (c, path) -> {
            String newPath = c.getOrSetString(path + ".script", path + ".example");
            if(path.equals(newPath))return null;

            return factory.build(
                    c,
                    newPath,
                    c.getOrSetString(newPath+".type", "chat")
            );
        });

        f.add("exec_container", (c, path) -> {
            String newPath = c.getOrSetString(path + ".script", path + ".example");
            if(path.equals(newPath))return null;
            return new ScriptQuestContainer(c, newPath);
        });

        f.add("set_max_regen_health", (s, path) -> {
                    String data;
                    if(s instanceof QuestConfig c){
                        data = c.getSingleString(path, ".amount", "NUM:-1");
                    } else data = s.getOrSetString(path+".amount", "NUM:-1");
                    return new QuestScript() {
                        private final Mather m = Mather.build(data);
                        @Override
                        public void run(AbstractQuestSession session) {
                            Valkiria v = Main.floader.getValkiria(session.getPlayer());
                            if(v instanceof DungeonValkiria dv){
                                dv.setMaxRegenShield(m.getValue(session).doubleValue());
                            }
                        }
                    };
                }
        );

        f.add("add_quest_in_history", (s, path) -> {
                    int quest;
                    if(s instanceof QuestConfig c){
                        quest = c.getSingleInt(path, ".id", -1);
                    } else quest = s.getOrSetInt(path+".id", -1);
                    return session -> session.getQuestHistory().put(quest, System.currentTimeMillis());
                }
        );

        f.add("complete", (s, path) -> {
                    int nestQuest;
                    if(s instanceof QuestConfig c){
                        nestQuest = c.getSingleInt(path, ".next", -1);
                    } else nestQuest = s.getOrSetInt(path+".next", -1);
                    return session -> {
                        session.completeActiveQuest();
                        if(nestQuest>-1){
                            Quest quest = Quest.valueOf(nestQuest);
                            if(quest==null)return;
                            session.attemptTakeQuest(quest);
                        }
                    };
                }
        );

        f.add("open_redirect_gui", (s, path) -> {
                String network;
                if(s instanceof QuestConfig c){
                    network = c.getSingleString(path, ".network", "example");
                } else network = s.getOrSetString(path+".network", "example");
                return session -> GuiTeleport.get(network).open(session.getPlayer(), 0,0);
                }
        );

        f.add("exec_scene", (s, path) -> {
            String sceneName;
            if(s instanceof QuestConfig c){
                sceneName = c.getSingleString(path, "scene", "scene");
            } else sceneName = s.getOrSetString(path+".scene", "scene");
             return new QuestScript() {
                        private final String scene = sceneName;
                        @Override
                        public void run(AbstractQuestSession session) {
                            Scene.run(scene, session.getPlayer(), TpMotion.build(session.getPlayer().getLocation()));
                        }
                    };
                }
        );

        f.add("break_if_offline", (c, path) -> new QuestScript() {
            @Override
            public void run(AbstractQuestSession session) {}

            @Override
            public boolean runScript(AbstractQuestSession session) {
                return !session.getPlayer().isOnline();
            }
        });

        f.add("spawn_point", (c, path) -> new QuestScript() {
            private final Location loc = c.getLocation(path+".location");
            @Override
            public void run(AbstractQuestSession session) {
                new BukkitRunnable() {
                    @Override
                    public void run() { session.getPlayer().setBedSpawnLocation(loc, true); }
                }.runTaskLater(Main.instance, 0);
            }
        });

        f.add("set_quest", (c, path) -> new QuestScript() {
            private final int quest = c.getOrSetInt(path+".quest_id", 0);
            private final boolean ignoreCancel = c.getOrSetBoolean(path+".ignore_cancel", true);

            @Override
            public void run(AbstractQuestSession session) {
                if(session.getActiveQuest() != null){
                    if(ignoreCancel) session.completeActiveQuest();
                    else return;
                }
                session.attemptTakeQuest(Quest.valueOf(quest));
            }
        });

        f.add("break", (c, path) -> {
            String parentType = c.getOrSetString(path + ".parent_type", "none");
            if(parentType.equalsIgnoreCase("none")){
                return new QuestScript() {
                    @Override
                    public void run(AbstractQuestSession session) {}

                    @Override
                    public boolean runScript(AbstractQuestSession session) {return true;}
                };
            } else {
                return new QuestScript() {
                    private final QuestScript parent = factory.build(c, path, parentType);

                    @Override
                    public void run(AbstractQuestSession session) {
                        parent.run(session);
                    }

                    @Override
                    public boolean runScript(AbstractQuestSession session) {
                        parent.runScript(session);
                        return true;
                    }
                };
            }
        });

        f.add("logic", (c, path) -> {
            Conduction conduction = Conduction.build(
                    c.getOrSetString(path+".if",
                            "MEMORY:count !== NUM:10 && RANGE_XZ:10,15 < DUB:21.5 && " +
                                 "RANGE:10,15,10 > NUM:10 || COUNT_BP IN STR:10,11,12 || " +
                                 "QUEST_IS_COMPLETED:10 == BOOL:True && QUEST_TIME:10 >= MAX_SHIELD || " +
                                 "QUEST_ID <= V_SHIELD && HAS_BP == BOOL:False"
                    ));
            if(c.contains(path+".else")){
                return new QuestScript() {
                    private final Conduction l = conduction;
                    private final QuestScript scriptIf = getScript(c, path+".then");
                    private final QuestScript scriptElse = getScript(c, path+".else");

                    @Override
                    public void run(AbstractQuestSession session) {
                        if(l.checkFor(session)){
                            scriptIf.runScript(session);
                        } else {
                            scriptElse.runScript(session);
                        }
                    }
                };
            } else {
                return new QuestScript() {
                    private final Conduction l = conduction;
                    private final QuestScript scriptIf = getScript(c, path+".then");

                    @Override
                    public void run(AbstractQuestSession session) {
                        if(l.checkFor(session)){
                            scriptIf.runScript(session);
                        }
                    }
                };
            }
        });

        return f;
    }


    public static QuestScript getScript(Sector s, String path){
        String scriptType;
        if(s instanceof QuestConfig c){
            scriptType = c.getScriptType(path, "none");
        } else {
            scriptType = s.getOrSetString(path + ".type", "none");
        }
        QuestScript result;
        try {
            result = factory.build(s, path, scriptType);
        } catch (Exception e){
            System.out.printf("| Critical error in script [%s]!%n", path);
            e.printStackTrace();
            result = factory.build(s, path, "none");
        }
        if(result==null){
            System.out.printf("| Script [%s] not defined! check DOCX.yml%n", path);
            return factory.build(s, path, "none");
        }
        return result;
    }
}