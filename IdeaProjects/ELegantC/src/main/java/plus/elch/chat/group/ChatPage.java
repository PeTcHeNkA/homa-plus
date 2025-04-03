package plus.elch.chat.group;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plus.elch.user.ElData;
import plus.tson.TsonMap;
import static plus.elch.ElegantMain.MINI_MSG_INST;
import static plus.tson.ext.TsonBoolUtils.*;
import static plus.tson.ext.TsonNumUtils.*;


public class ChatPage implements Comparable<ChatPage>{
    static final Byte2ObjectOpenHashMap<ChatPage> PAGE_BY_ID = new Byte2ObjectOpenHashMap<>(4);
    private final boolean evalPapi, evalUserPapi;
    private final int priority;
    private final String format;
    private final String name;
    private final int id;
    private final boolean allowIgnore;

    public ChatPage(TsonMap map){
        this(
                map.getStr("name"),
                calc(map, "papi"     , true),
                calc(map, "user-papi", false),
                calc(map, "priority", 0),
                map.getStr("format"),
                calc(map, "id", 0),
                calc(map, "allow-ignore", false)
        );
    }


    public ChatPage(String name, boolean evalPapi, boolean evalUserPapi, int priority,
                    String format, int id, boolean allowIgnore){
        this.evalPapi = evalPapi;
        this.evalUserPapi = evalUserPapi;
        this.priority = priority;
        this.format = format;
        this.name = name;
        this.id = id;
        this.allowIgnore = allowIgnore;
        if(id < 0 || id > 15) throw new IllegalArgumentException("Id must be between 0 and 15");
        ChatGroup.registerPage((byte)id, this);
    }

    public static ChatPage byId(byte id) {
        return PAGE_BY_ID.get(id);
    }


    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public boolean canGet(ElData data){
        return !allowIgnore || !data.getBit(getId());
    }


    public Component format(Player sender, String msg, boolean allowEval){
        String format = this.format;
        if(evalPapi)
            format = PlaceholderAPI.setBracketPlaceholders(sender, format);

        if(allowEval || evalUserPapi)
            msg = PlaceholderAPI.setBracketPlaceholders(sender, msg);

        return MINI_MSG_INST.deserialize(format, Placeholder.unparsed("message", msg));
    }


    @Override
    public int compareTo(@NotNull ChatPage o) {
        return Integer.compare(priority, o.priority);
    }
}
