package plus.quest.script.quest.trigger;

import plus.util.cfg.Sector;
import plus.util.factory.AbstractFactory;
import plus.quest.QuestConfig;
import plus.quest.script.quest.ScriptProtectedContainer;
import java.util.HashMap;


public abstract class Trigger extends ScriptProtectedContainer implements STrigger{

    public static final AbstractFactory<STrigger> factory = define();
    private static final HashMap<String, STrigger> cache = new HashMap<>();
    private static Sector s;

    private static AbstractFactory<STrigger> define() {
        AbstractFactory<STrigger> f = new AbstractFactory<>() {
            @Override
            protected void onAddLabel(Sector docx, String label) {
                String path = "triggers.example_" + label;
                docx.set(path+".type", label);
                getOf(docx, path);
                cache.remove(path);
            }
        };

        f.add("cmd", CmdTrigger::new);
        f.add("region", RgnTrigger::new);
        f.add("block", BlockTrigger::new);
        f.add("mm_kill", MMKillTrigger::new);
        f.add("take_item", TakeItemsTrigger::new);

        return f;
    }


    public static void define(Sector sector){
        cache.clear();
        s = sector;
    }


    public static STrigger getOf(String path){
        return getOf(s, path);
    }


    public static STrigger getOf(Sector c, String path){
        STrigger result = cache.get(path);
        if(result != null)return result;

        String triggerType;

        if(c instanceof QuestConfig config) {
            triggerType = config.getScriptType(path, "cmd"); //s.getOrSetString(path + ".type", "cmd");
        } else triggerType = c.getOrSetString(path + ".type", "cmd");

        result = factory.build(c, path, triggerType);

        if(c.contains(path+".on_load")){
            if(!triggerType.equals("load"))
                result = new RunnableTrigger(result, c, path);
            else result = new LoadTrigger(c, path);
        }

        if(result==null){
            System.out.println("| Trigger ["+triggerType+"] not defined! check DOCX.yml");
        } else cache.put(path, result);
        return result;
    }

    protected final boolean needUnloadAfterActivation;
    private final String ID;

    public Trigger(Sector s, String path) {
        super(s, path);
        this.ID = path;

        needUnloadAfterActivation = !s.getOrSetBoolean(path + ".can_repeat", true);
    }


    public String toString(){
        return ID;
    }
}