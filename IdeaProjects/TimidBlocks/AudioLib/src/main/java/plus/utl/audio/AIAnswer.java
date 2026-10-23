package plus.utl.audio;

import com.google.gson.Gson;


/**
 * Ответ от Vosk AI приходит в виде JSON. Просто привести это к обычным строкам
 * @author HomaPlus
 */
public class AIAnswer {
    private static final Gson GSON = new Gson();

    public String partial;
    public String text;

    public static String getResult(String raw){
        return GSON.fromJson(raw, AIAnswer.class).text;
    }

    public static AIAnswer getResultRaw(String raw){
        return GSON.fromJson(raw, AIAnswer.class);
    }
}