package plus.AChat.Format;

import java.util.HashMap;

public class AChatFormat {
    //List<String> format = new ArrayList<>();
    final protected String format;
    public AChatFormat(String format){
        this.format = format;
        //test$value$ resume $val2$
    }
    public static AChatFormat CreateFormat(String format){
        return new AChatFormat(format);
    }


    public String format(String label, String value){
        return format.replace(label, value);
    }

    public String format(HashMap<String, String> map){
        String result = format;
        for(String key:map.keySet()){
            result = result.replace(key, map.get(key));
        }
        return result;
    }
}
