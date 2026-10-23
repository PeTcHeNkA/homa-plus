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
            String key = entry.getKey();
            int id = getId(key);

            if(id != 0){
                LangPackV2.Line line = lpack.getLineById(id);
                if(line != null){
                    line.actual = entry.getValue();
                    continue;
                }
            }
            if(id != 0){
                key = remId(key);
            }

            boolean result = lpack.setLine(key, entry.getValue());
            if(!result && logInvalid)
                invalid.put(key, entry.getValue());
        }
        if(logInvalid && !invalid.isEmpty())
            JsonUtils.writeFile("invalid.json", invalid.entrySet(), JsonUtils.Type.UTF8);
    }


    public static int getId(String key){
        if(key.startsWith("#ID:")){
            int id = 0, max1 = key.length();
            for (int i = 4; i < max1; i++) {
                char chr = key.charAt(i);
                if (chr >= '0' && chr <= '9') {
                    id *= 10;
                    id += (chr - '0');
                } else if (chr == '|') {
                    return id;
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }


    public static String remId(String src)
    {
        if (src.startsWith("#ID:"))
        {
            int index = src.indexOf("|");
            if (index == -1) return src;
            return src.substring((index + 1));
        }
        return src;
    }
}