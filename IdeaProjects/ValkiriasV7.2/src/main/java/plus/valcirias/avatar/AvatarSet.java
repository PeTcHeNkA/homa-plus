package plus.valcirias.avatar;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import plus.util.ConfigHelper;
import plus.util.cfg.BasicSector;


public class AvatarSet {

    private final String permission;
    private final Avatar[] avatars;
    private static int seed = 1;

    public int getNextRandom() {
        byte a = 31;
        int c = 119;
        seed = (a * seed + c) % avatars.length;
        return seed;
    }

    private final List<TextComponent[]> pages = new ArrayList<>();

    public AvatarSet(String groupName,String hash, String cmdHash,String permission){
        this.permission = permission==null?"none":permission;

        char[] chars = hash.toCharArray();
        String[] cmdListString = cmdHash.split(";");

        avatars = new Avatar[chars.length];

        int i = 0;
        for(char symbol:chars){
            avatars[i] = new Avatar(groupName, i,symbol, Integer.parseInt(cmdListString[i]));
            i = i+1;
        }

        int countAvatars = 0;
        int workPage = 0;
        int countPages = avatars.length/7 + (avatars.length%7==0?0:1);

        String pageFormat1 = BasicSector.parseColor(ConfigHelper.getConfigStrValue("chat.pageformat1"));
        String pageFormat2 = BasicSector.parseColor(ConfigHelper.getConfigStrValue("chat.pageformat2"));
        TextComponent prewPage = ConfigHelper.getTextComponent("chat.prewpage");
        TextComponent lastPage = ConfigHelper.getTextComponent("chat.lastpage");

        while (chars.length>0) {
            ArrayList<TextComponent> work = new ArrayList<>();

            int marker = chars.length<7?chars.length:7;
            char[] pagechars = new char[marker];
            char[] newChars = new char[chars.length-marker];

            System.arraycopy(chars, 0, pagechars, 0, marker);
            System.arraycopy(chars, marker, newChars, 0, chars.length-marker);
            chars = newChars;

            int oldCountAvatars = countAvatars;

            int flag = oldCountAvatars;
            for (char symbol : pagechars) {
                TextComponent com = new TextComponent(String.valueOf(symbol));
                com.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/avatar set "+groupName+" "+ flag));
                work.add(com);
                countAvatars += 1;
                flag+=1;
            }
            if(workPage>0) {
                prewPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/avatar page " + groupName + " " + workPage));
                work.add(prewPage);
            }
            work.add(new TextComponent(String.valueOf('\n')));

            for (flag = oldCountAvatars; flag < countAvatars; flag++) {
                TextComponent com = new TextComponent("         ");
                com.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/avatar set "+groupName+" "+ flag));
                work.add(com);
            }

            TextComponent sen = new TextComponent(String.format(pageFormat1, workPage+1, countPages));
            work.add(sen);
            work.add(new TextComponent(String.valueOf('\n')));

            for (flag = oldCountAvatars; flag < countAvatars; flag++) {
                TextComponent com = new TextComponent("         ");
                com.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/avatar set "+groupName+" "+ flag));
                work.add(com);
            }

            TextComponent sen2 = new TextComponent(pageFormat2);
            work.add(sen2);
            work.add(new TextComponent(String.valueOf('\n')));
            for (flag = oldCountAvatars; flag < countAvatars; flag++) {
                TextComponent com = new TextComponent("         ");
                com.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/avatar set "+groupName+" "+ flag));
                work.add(com);
            }

            //workpage+=1;
            if(workPage+1<countPages){
                TextComponent nextpage = ConfigHelper.getTextComponent("chat.nextpage");
                //TextComponent nextpage = nextpage1.duplicate();
                nextpage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/avatar page " + groupName + " " + (workPage + 2)));
                work.add(nextpage);
            } else {
                work.add(lastPage);
            }
            //nextpage.setClickEvent(new ClickEvent());


            pages.add(work.toArray(new TextComponent[0]));
            workPage+=1;
        }

    }


    public String getPermission() {
        return permission;
    }


    public int getCount() {
        return avatars.length;
    }


    public Avatar getAvatar(int index) {
        if(index<avatars.length){
            return avatars[index];
        } else {
            return avatars[getNextRandom()];
        }
    }


    public TextComponent[] getAvatarListView(int page){
        int index = page-1;
        if(index<1)return pages.get(0);
        else if(index<pages.size())
        return pages.get(index);
        else return pages.get(pages.size()-1);
    }


//    private TextComponent[] addargs(TextComponent[] a, TextComponent[] b) {
//        TextComponent[] r = new TextComponent[a.length + b.length];
//        System.arraycopy(a, 0, r, 0, a.length);
//        System.arraycopy(b, 0, r, a.length, b.length);
//        return r;
//    }
}
