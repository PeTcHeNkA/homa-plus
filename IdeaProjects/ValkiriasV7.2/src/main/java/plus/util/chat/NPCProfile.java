package plus.util.chat;

import net.md_5.bungee.api.chat.TextComponent;
import plus.valcirias.avatar.AvatarFloader;
import plus.util.cfg.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NPCProfile {
    private static Config c;
    private static String avatarFormat;
    private static String messageFormat;
    private static final HashMap<String, NPCProfile> cashe = new HashMap<>();

    public static void Initialize(Config cfg){
        c = cfg;
        avatarFormat = c.getString("sys.format.avatar");
        messageFormat = "\n"+c.getString("sys.format.message");
    }
    public static void reload(){
        c.reload();
        avatarFormat = c.getString("sys.format.avatar");
        messageFormat = "\n"+c.getString("sys.format.message");
    }

    public static NPCProfile getNpcProfile(String label){
        NPCProfile result = cashe.get(label);
        if(result == null){
            result = new NPCProfile(label);
            cashe.put(label, result);
        }
        return result;
    }

    private final String format;
    private NPCProfile(String label){
        String f = c.getString("npc." + label + ".name");
        String[] avatarCs = c.getString("npc." + label + ".avatar").split(":");

        format = String.format(
                avatarFormat, AvatarFloader.getAvatarSet(avatarCs[0]).getAvatar(Integer.parseInt(avatarCs[1])).getSymbol(), f
        )+messageFormat;
    }

    public TextComponent[] toMessage(List<String> message){
        List<String> all1 = new ArrayList<>();
        List<String> all2 = new ArrayList<>();

        boolean first = true;
        int lastLenght = 0;
        //int prewComp = 0;
        //int index = 0;
        for(String v:message){
            if(v.charAt(0)=='@'){
                all1.add(v);
            } else if(v.charAt(0)=='$'){
                all1.add(v.substring(1).replace("+", messageFormat));
            } else {
                v = v.replace("\n", " ")
                        //.replace("+", messageFormat)
                        .replace("$", " ")
                        .replace("  ", " $$")
                        .replace("$ $", "$$");
                List<String> answ = splitAll(v, lastLenght);
                if(answ.size()>0){
                    //String r = "";
                    lastLenght = answ.get(answ.size()-1).length();
                    //for(String sv:answ){
                    String r = " "+(String.join(messageFormat, answ)).replace("$$", "  ").replace("+", messageFormat);
                    //    r = r+(sv+messageFormat);
                    //}
//                    String s = all1.get(prewComp);
//                    if(s.charAt(s.length()-1)=='\n' && r.charAt(r.length()-1)=='\n'){
//                        r = r.substring(1);
//                    }

                    all1.add(r.replace("\n\n", "\n"));

                }
                //prewComp = index;
            }
           // index +=1;
        }
        all2.add(format);
        all2.addAll(all1);
//        System.out.println("********");
//        for(String s:all2){
//            System.out.println(s);
//        }
        return ChatHelper.Parse(all2);
    }


    private static List<String> splitAll(String v, int fLimit) {
        if(fLimit<1)fLimit=49;
        List<String> hs = new ArrayList<>();

        String[] stt = v.split(" ");
        String ws = "";
        for (String st : stt) {
            if ((ws.length() + st.length()) < fLimit) {
                ws = ws + " " + st;
            } else {
                fLimit = 49;
                hs.add(ws);
                ws = st;
            }
        }
        if(ws.length()>0){
            hs.add(ws);
        }
//        System.out.println("===");
//        System.out.println(v);
//        System.out.println("----");
//        for(String s:hs){
//            System.out.println(s);
//        }
//        System.out.println("===");
        return hs;
    }

}
