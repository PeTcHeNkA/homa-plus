package plus.elch.chat.group;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import plus.elch.chat.rule.IUserSelector;
import plus.elch.chat.rule.SelectAll;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.utl.Tuple;
import java.util.Arrays;


public abstract class ChatGroup {
    private static final Char2ObjectOpenHashMap<ChatGroup> GROUP_BY_CHR  = new Char2ObjectOpenHashMap<>(4);
    private static ImmutableMap<String, ChatGroup>         GROUP_BY_NAME = ImmutableMap.of();
    private static ChatGroup DEFAULT;
    private final String name;
    private final IUserSelector selector;

    protected ChatGroup(IUserSelector selector, String name) {
        this.name = name;
        if(selector == null) selector = SelectAll.getInstance();
        this.selector = selector;
    }


    public String getName() {
        return name;
    }


    public IUserSelector getSelector() {
        return selector;
    }


    public abstract ChatPage getPage(Player player, String message);


    public static void loadFrom(TsonMap map){
        GROUP_BY_CHR.clear();
        ChatPage.PAGE_BY_ID.clear();
        GROUP_BY_NAME = ImmutableMap.of();
        DEFAULT = null;
    }


    public static ChatGroup fromTson(String name, TsonObj raw){
        if(raw.isCustom()){
            Object obj = raw.getField();
            if(obj instanceof ChatGroup)return (ChatGroup)obj;
            throw new IllegalArgumentException("Illegal chat group: " + obj.getClass()+" ("+obj+")");
        }
        TsonMap map = raw.getMap();
        name = map.getOrDefaultStr("name", name);

        if(map.containsKey("type")){
            IUserSelector selector = SelectAll.getInstance();
            if(map.containsKey("selector")){
                selector = IUserSelector.fromTson(map.get("selector"));
            }

            String type = map.getStr("type");
            switch (type) {
                case "singleton":
                case "simple":
                    return new SingletonGroup(selector, name, new ChatPage(map));
                case "perm-sort": {
                    TsonList list = map.getList("pages");
                    Tuple<ChatPage, String>[] pages = new Tuple[list.size()];

                    for (int i = 0; i < list.size(); i++) {
                        TsonMap pageMap = list.getMap(i);
                        pages[i] = new Tuple<>(new ChatPage(pageMap), pageMap.getStr("perm"));
                    }
                    Arrays.sort(pages, (o1, o2) -> o1.A.compareTo(o2.A));

                    ChatPage[] chatPages = new ChatPage[pages.length];
                    String[] perms       = new String[pages.length];
                    for (int i = 0; i < pages.length; i++) {
                        chatPages[i] = pages[i].A;
                        perms[i]     = pages[i].B;
                    }
                    return new PermSortGroup(selector, name, chatPages, perms);
                }
            }
        }

        if(map.containsKey("format")) return new SingletonGroup(null, name, new ChatPage(map));

        throw new IllegalArgumentException("Illegal chat group: " + name);
    }


    public static ChatGroup getDefault() {
        return DEFAULT;
    }


    public static ChatGroup byName(String name) {
        return GROUP_BY_NAME.get(name);
    }


    protected static void registerPage(byte id, ChatPage page) {
        if(ChatPage.PAGE_BY_ID.put(id, page) != null) throw new IllegalArgumentException("Duplicate page id: " + id);
    }


    public static ChatGroup byChar(char chr) {
        return GROUP_BY_CHR.get(chr);
    }
}
