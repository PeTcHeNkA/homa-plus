package plus.quest.script.quest;

import org.bukkit.entity.Player;
import plus.Main;
import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.memory.QuestMemory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ScriptJSModule implements QuestScript{

    private static ScriptEngine engine;
    private static String prefix;
    private static String variables;
    private final String JavaScriptApplication;
    private final boolean importVariables;
    private final String[] vars;
    private final HashMap<String, QuestScript> cache = new HashMap<>();

    public ScriptJSModule(Sector c, String path){
        if(engine==null) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            engine = mgr.getEngineByName("JavaScript");
            prefix =
                    "function editMemory(key, type, value) {return 'MEM_EDIT:'+key+':'+type+':'+value};"+
                    "function runScript(s) {return 'RUN_SCRIPT:'+s};";
            variables =
                    "const location = {x: %loc_x%, y: %loc_y%, z: %loc_z%};" +
                    "const player = {name: '%player_name%'};";
        }
        List<String> rawCode = c.getOrSetStringList(path + ".java_script",
                Arrays.asList(
                        "function onRunScript(){",
                        "//your code here",
                        "//available variables: (if imported)",
                        "//location.x, location.y, location.z ",
                        "//player.name",
                        "//use return runScript(path) to run the script",
                        "//use return editMemory(type, value) to edit quest memory",
                        "};"
                )
        );
        importVariables = c.getOrSetBoolean(path+".import_variables", false);
        if(c.getOrSetBoolean(path+".simple_mode", false)){
            JavaScriptApplication = String.join("\n", rawCode);
        } else {
            JavaScriptApplication = prefix + String.join("\n", rawCode) + "onRunScript();";
        }
        String varsRaw = c.getOrSetString(path + ".import_as_memory", "none");
        if(varsRaw.equalsIgnoreCase("none"))vars = null;
        else {
            vars = varsRaw.replace(" ", "").split(",");
        }
    }


    private String parseVariables(AbstractQuestSession session){
        Player player = session.getPlayer();
        return variables.
                replace("%loc_x%", String.valueOf(player.getLocation().getX())).
                replace("%loc_y%", String.valueOf(player.getLocation().getY())).
                replace("%loc_z%", String.valueOf(player.getLocation().getZ())).
                replace("%player_name%", player.getName());
    }


    private String exportMemoryVars(AbstractQuestSession session){
        QuestMemory m = session.getMemory();
        StringBuilder code = new StringBuilder("const var {");
        for(String key:vars){
            Object o = m.getSection(key).getValue();
            if((o instanceof Integer) || (o instanceof Boolean)){
                code.append(key).append(": ").append(o).append(",");
            } else {
                code.append(key).append(": '").append(o).append("',");
            }
        }
        return code+"};";
    }


    @Override
    public void run(AbstractQuestSession session) {
        Object result;
        try {
            if(importVariables){
                if(vars != null){
                    result = engine.eval(parseVariables(session)+
                                    exportMemoryVars(session)+
                                    JavaScriptApplication);
                } else {
                    result = engine.eval(parseVariables(session) + JavaScriptApplication);
                }
            } else {
                if(vars != null) {
                    result = engine.eval(exportMemoryVars(session)+JavaScriptApplication);
                } else {
                    result = engine.eval(JavaScriptApplication);
                }
            }
        } catch (ScriptException e) {
            e.printStackTrace();
            return;
        }
        if(result == null)return;
        String answer = result.toString();

        String[] rawData = answer.split("&");
        if(rawData[0].equals("MEM_EDIT")){
            session.getMemory().Sections().put(rawData[1], QuestMemory.parseMemory(rawData[2], rawData[3]));
        } else if(rawData[0].equals("RUN_SCRIPT")){
            QuestScript script = cache.get(rawData[1]);
            if(script==null){
                script = QuestScriptFactory.getScript(
                        Main.instance.getOrCreateYandeReConfig("RPG_QUESTS.yml"),
                        rawData[1]
                );
                cache.put(rawData[1], script);
            }
            script.run(session);
        }
    }
}