package plus.util.factory;


import plus.util.cfg.Config;
import plus.util.cfg.Sector;
import java.util.HashMap;


public abstract class AbstractFactory<Result> {

    public final HashMap<String, Blueprint<Result>> memory = new HashMap<>();
    private final Blueprint<Result> emptyBluePrint = (c, path) -> null;

    public void add(String name, Blueprint<Result> blueprint){
        memory.put(name, blueprint);
    }

    public Result build(Sector c, String path, String name){
        return memory.getOrDefault(name, emptyBluePrint).build(c, path);
    }

    protected void onCreate(Sector docx){}
    protected void onFinish(Sector docx){}
    protected abstract void onAddLabel(Sector docx, String label);

    public void createDocx(Config docx){
//        Config docx = new Config(Main.instance, "DOCX.yml");
        onCreate(docx);
        for(String key:memory.keySet()){
            onAddLabel(docx, key);
        }
        onFinish(docx);
        docx.saveChanges();
    }
}