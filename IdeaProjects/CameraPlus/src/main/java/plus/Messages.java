package plus;

import plus.util.Chat.DynamicJsonMessage;
import plus.util.Config.Config;


public class Messages {

    public static DynamicJsonMessage MSG_JOIN;
    private static Config c;

    public static void load(Config c){
        MSG_JOIN = new DynamicJsonMessage(c.getStringList("join_message"));
    }
    public static void Reload(){
        load(c);
    }
}