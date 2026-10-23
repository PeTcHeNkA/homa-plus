package plus.quest.script.scene;

import plus.quest.script.quest.ScriptDamage;
import plus.quest.script.quest.ScriptTakeBattlePass;
import plus.quest.script.system.ScriptClearMobs;
import plus.quest.script.quest.ScriptHeal;
import plus.quest.script.system.ScriptMMSpawn;
import plus.quest.script.quest.ScriptRedirect;
import plus.quest.script.system.ScriptSleep;
import plus.util.cfg.Sector;
import plus.util.factory.AbstractFactory;
import plus.quest.entity.ActorScript;
import plus.quest.motion.BasicPos;
import plus.quest.motion.TpMotion;
import plus.quest.parser.Parser;
import plus.quest.QuestConfig;
import java.util.Arrays;


public class ScriptFactory {

    public static final AbstractFactory<Script> factory = define();

    private static AbstractFactory<Script> define(){
        AbstractFactory<Script> f = new AbstractFactory<>() {
            @Override
            protected void onAddLabel(Sector s, String label) {
                s.set("scenes.example.scripts.example_"+label+".type", label);
                getScript(s, "scenes.example.scripts.example_"+label);
            }
        };

        f.add("none", (c, path) -> (UniversalScript) player -> {});
        f.add("heal", (c, path) -> new ScriptHeal());
        f.add("chat", ScriptChat::new);
        f.add("sleep", ScriptSleep::new);
        f.add("actor", ActorScript::new);
        f.add("title", ScriptTitle::new);
        f.add("title", ScriptTitle::new);
        f.add("damage", ScriptDamage::new);
        f.add("command", ScriptCommand::new);
        f.add("mm_spawn", ScriptMMSpawn::new);
        f.add("redirect", ScriptRedirect::new);
        f.add("title_s", ScriptTitlePlus::new);
        f.add("json_text", ScriptJsonText::new);
        f.add("player_tp", PhysicTeleport::new);
        f.add("container", ScriptContainer::new);
        f.add("clear_mobs", ScriptClearMobs::new);
        f.add("action_bar", ScriptActionBar::new);
        f.add("play_music", ScriptPlayMusic::new);
        f.add("hide_player", ScriptHidePlayer::new);
        f.add("take_bp", ScriptTakeBattlePass::new);
        f.add("edit_scene_pos", ScriptEditRelativePos::new);

        f.add("camera_tp", (c, path) -> {
            if (c.getOrSetBoolean(path + ".relative", true))
                return new PacketTeleport(
                        TpMotion.build(c.getOrSetString(path + ".location", "0;0;0"))
                );
            else
                return new PacketTeleport(
                        TpMotion.build(c.getOrSetString(path + ".location", "0;0;0")), false
                );
        });

        f.add("motion", (c, path) -> new PacketMotions(
                c.getOrSetStringList(path+".data", Arrays.asList("0;0;0", "10;0;0")),
                Parser.valueOf(c.getOrSetString(path+".parser", "linear")),
                new BasicPos(c.getOrSetString(path+".location", "0;0;0")),
                c.getOrSetInt(path+".boost", 10),
                c.getOrSetBoolean(path+".invert", false),
                c.getOrSetInt(path+".delay", 100)
        ));

        f.add("remove_camera", (c, path) -> ScriptRemoveCamera.getInstance());

        f.add("effect_dark", (c, path) -> new ScriptDarkEffect(
                c.getOrSetInt(path+".delay", 500),
                c.getOrSetInt(path+".boost", 10)
        ));

        f.add("create_camera", (c, path) -> {
            if(c.getOrSetBoolean(path+".relative", true))
                return new PacketCreateCamera(
                        TpMotion.build(c.getOrSetString(path+".location", "0;0;0"))
                );
            else
                return new PacketCreateCamera(
                        TpMotion.build(c.getOrSetString(path+".location", "0;0;0")), false
                );
        });

        return f;
    }


    public static Script getScript(Sector s, String path) {
        String scriptType;
        if (s instanceof QuestConfig c) {
            scriptType = c.getScriptType(path, "motion");
        } else scriptType = s.getOrSetString(path + ".type", "motion");
        Script result;
        try {
            result = factory.build(s, path, scriptType);
        } catch (Exception e){
            System.out.printf("| Critical error in script [%s]!%n", path);
            e.printStackTrace();
            result = factory.build(s, path, "none");
        }
        if(result==null){
            System.out.println("| Script ["+scriptType+"] not defined! check DOCX.yml");
        }
        return result;
    }
}