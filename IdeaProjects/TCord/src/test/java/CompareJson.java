import plus.tcord.JTranslationPack;

import java.util.TreeMap;


public class CompareJson {
    public static void main(String[] args) {
        SortJson.GroupComparator comp = SortJson.create();
        TreeMap<String, String> map = new TreeMap<>(comp);
        TreeMap<String, String> map2 = new TreeMap<>(comp);

        JTranslationPack.readFile("C:/Users/Te4hnoPlus/Desktop/DTranslator/server/export_rus.json", map);


        JTranslationPack.readFile("C:/Users/Te4hnoPlus/Desktop/GFLT/translation_ru.json", map2);


        TreeMap<String, String> map3 = new TreeMap<>(comp);

        for (String key:map.keySet()){
            map3.put(key, map2.get(key) +" |A######D| "+ map.get(key));
        }

        JTranslationPack.writeFile("C:/Users/Te4hnoPlus/Desktop/DTranslator/server/export_diff.json", map3, JTranslationPack.Type.UTF8);
    }
}
