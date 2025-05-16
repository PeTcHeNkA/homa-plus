package plus.proto;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class JsonUtils {

    /**
     * Прочитать json-перевод из файла и записать его в Map
     * @return Тип файла
     */
    public static Type readFile(String path, Map<String, String> map) {
        File file = new File(path);
        if(!file.exists() || file.isDirectory()) return Type.ERROR;
        JsonElement jobj = null;
        boolean hasBOM = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.mark(3);
            if (reader.read() != 0xFEFF) {
                reader.reset();
            } else {
                hasBOM = true;
            }
            jobj = JsonParser.parseReader(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(jobj == null) return Type.ERROR;

        jobj.getAsJsonObject().entrySet().forEach(
                stringJsonElementEntry -> map.put(stringJsonElementEntry.getKey(), stringJsonElementEntry.getValue().getAsString())
        );

        return hasBOM? Type.UTF8_BOM : Type.UTF8;
    }


    /**
     * Записать Map в json-файл с указанным типом
     */
    public static void writeFile(String path, Map<String, String> map, Type type) {
        File file = new File(path);
        createPathIfNeed(file);

        try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            if(type == Type.UTF8_BOM)
                out.write('\ufeff');

            JsonWriter jw = new JsonWriter(out);
            jw.setIndent("  ");

            jw.beginObject();
            map.forEach((s, s2) -> {
                try {
                    jw.name(s).value(s2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            jw.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Создать директорию, для несуществующего файла, если она не была создана ранее
     */
    public static boolean createPathIfNeed(File file){
        File parent = file.getParentFile();
        if(parent != null){
            return parent.mkdirs();
        }
        return false;
    }


    /**
     * Тип json-файла
     */
    public enum Type {
        ERROR, UTF8, UTF8_BOM
    }
}