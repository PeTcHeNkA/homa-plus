package plus.item;

import org.bukkit.plugin.Plugin;
import plus.util.cfg.Config;

import java.util.HashMap;
import java.util.List;

public class CashedConfig extends Config {

    protected HashMap<String, String> hash = new HashMap<>();
    protected HashMap<String, Integer> hash1 = new HashMap<>();
    protected HashMap<String, RandomCInt> hash2 = new HashMap<>();
    protected HashMap<String, List<String>> hash3 = new HashMap<>();

    public final String ICO_STRING_FORMAT_3;
    public final String ICO_STRING_FORMAT_2;
    public final String ICO_STRING_FORMAT_1;

    public CashedConfig(Plugin p, String name) {
        super(p, name, true);
        ICO_STRING_FORMAT_3 = getOrSetString("format.ico_string", "%s %s : %s", true);
        ICO_STRING_FORMAT_2 = getOrSetString("format.ico_and_lore", "%s %s", true);
        ICO_STRING_FORMAT_1 = getOrSetString("format.only_ico", "%s", true);
    }

    @Override
    public void reload(){
        ClearHash();
        super.reload();
    }
    public void ClearHash(){
        hash.clear();
        hash1.clear();
        hash2.clear();
        hash3.clear();
    }


    public String getCashedString(String path, String def, boolean colored){
        return hash.getOrDefault(path, putCashedString(path, def, colored));
    }
    public String getCashedString(String path, String def) {
        return hash.getOrDefault(path, putCashedString(path, def, true));
    }
    private String putCashedString(String path, String def, boolean colored){
        String result = getOrSetString(path, def);
        if(colored)result = result.replace('&', '\u00A7');
        hash.put(path, result);
        return result;
    }


    public int getCashedInt(String path, int def){
        return hash1.getOrDefault(path, putCashedInt(path, def));
    }

    private int putCashedInt(String path, int def){
        int result = getOrSetInt(path, def);
        hash1.put(path, result);
        return result;
    }
    public int getRandomInt(String path, String def){
        return hash2.getOrDefault(path, putCashedRInt(path, def)).getRandomSlot();
    }
    private RandomCInt putCashedRInt(String path, String def){
        RandomCInt r = new RandomCInt(getOrSetString(path, def));
        hash2.put(path, r);
        return r;
    }

    public List<String> getCashedStringList(String path, List<String> def){
        return hash3.getOrDefault(path, putCashedStringList(path, def));
    }
    private List<String> putCashedStringList(String path, List<String> def){
        List<String> result;
        if(contains(path)){
            result = getStringList(path);
        } else {
            result = def;
            set(path, def);
        }
        hash3.put(path, result);
        return result;
    }
}