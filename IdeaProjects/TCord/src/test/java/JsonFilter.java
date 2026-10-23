import plus.tcord.JTranslationPack;

import java.util.TreeMap;


public class JsonFilter {
    public static void main(String[] args) {
        SortJson.GroupComparator comp = SortJson.create();

        TreeMap<String, String> map = new TreeMap<>(comp);

        JTranslationPack.Type type = JTranslationPack.readFile("C:/Users/Te4hnoPlus/Desktop/DTranslator/server/export_ru.json", map);

        for (String key : map.keySet().toArray(new String[0])) {
            if(map.get(key).equals(key)){
                map.remove(key);
            }
        }

        JTranslationPack.writeFile("C:/Users/Te4hnoPlus/Desktop/DTranslator/server/export_rus.json", map, type);
    }
}
