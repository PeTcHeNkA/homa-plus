package plus.proto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class JsonTMap {
    private final Map<String, String> map;

    public JsonTMap() {
        this.map = new HashMap<>();
    }


    public JsonTMap(Map<String, String> map) {
        this.map = map;
    }


    public void loadFrom(String path){
        JsonUtils.readFile(path, map);
    }


    public void apply(LangPackV2 lpack, boolean logInvalid){
        Map<String, String> invalid = null;
        if(logInvalid)
            invalid = new TreeMap<>(Comparator.comparingInt(String::length));

        for (Map.Entry<String, String> entry : map.entrySet()) {
            boolean result = lpack.setLine(entry.getKey(), entry.getValue());
            if(!result && logInvalid)
                invalid.put(entry.getKey(), entry.getValue());
        }
        if(logInvalid && !invalid.isEmpty())
            JsonUtils.writeFile("invalid.json", invalid, JsonUtils.Type.UTF8);
    }
}