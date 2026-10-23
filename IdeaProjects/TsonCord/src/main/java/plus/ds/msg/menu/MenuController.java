package plus.ds.msg.menu;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import plus.ds.IrisSystem;
import plus.ds.api.ILayoutBuilder;
import plus.ds.api.IListener;
import plus.ds.api.IMessageEvent;
import plus.ds.api.IMsgBuilder;
import plus.ds.msg.FormattedMsg;
import plus.ds.msg.utl.Emojis;
import plus.tson.*;
import plus.tson.utl.Te4HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MenuController extends SelectMenuImpl implements IListener<StringSelectInteractionEvent>, StringSelectMenu, ILayoutBuilder {
    private final Te4HashMap<Option,Option> options;
    public MenuController(IrisSystem.GuildProgram program, String id, String placeholder, List<Option> options) {
        super(id, placeholder, 1, 1, false);
        this.options = new Te4HashMap<>(options.size());
        for(Option opt:options){
            this.options.put(opt, opt);
        }
        program.addMenuListener(this);
    }


    @Override
    public LayoutComponent toLayout(IMessageEvent event){
        return ActionRow.of(this);
    }


    public static MenuController build(IrisSystem.GuildProgram program, TsonMap map){
        String id = map.getStr("id");
        String placeholder = map.getOrDefault("plc", new TsonStr(id)).getStr();
        ArrayList<Option> options = new ArrayList<>();
        TsonObj obj = map.get("options");
        if(obj==null)throw new NullPointerException("Map ["+map+"] not contains [options] !");
        if(obj.isList()){
            int num = 0;
            for(TsonObj option: obj.getList()){
                options.add(buildOption(program, "val_"+num, option));
                ++num;
            }
        } else if(obj.isMap()){
            map = obj.getMap();
            for(String key: map.keySet()){
                options.add(buildOption(program, key, map.get(key)));
            }
        } else throw new RuntimeException("wrong type!");
        if(options.size()==0)throw new NullPointerException("Options should be >1");
        return new MenuController(program, id, placeholder, options);
    }


    private static Option buildOption(IrisSystem.GuildProgram program, String key, TsonObj obj){
        String text;
        String description = null;
        Emojis emoji = null;
        IMsgBuilder msgBuilder;
        boolean isDef = false, reply = false, hide = false;
        if(obj.isList()){
            TsonList lst = obj.getList();
            text = lst.getStr(0);
            msgBuilder = new FormattedMsg(program, lst.getStr(1));
            if(lst.size()>3){
                switch (lst.get(3).getStr()){
                    case "def":
                        isDef = true; break;
                    case "reply":
                        reply = true; break;
                    case "reply-hide":
                        reply = true; hide = true; break;
                }
                if(lst.size()>4){
                    isDef = lst.get(4).getStr().equals("def");
                }
                emoji = Emojis.valueOf(lst.getStr(1));
            } else {
                if(lst.size()>2){
                    emoji = Emojis.valueOf(lst.getStr(2));
                } else {
                    emoji = null;
                }
            }
        } else if(obj.isMap()){
            TsonMap map = obj.getMap();
            text = map.getOrDefault("text", new TsonStr("text_"+key)).getStr();
            description = map.getStrSafe("desc");
            msgBuilder = IMsgBuilder.build(program, map);
            isDef = map.getOrDefault("def", new TsonBool(false)).getBool();
            reply = map.getOrDefault("reply",new TsonBool(false)).getBool();
            hide = map.getOrDefault("hide", new TsonBool(false)).getBool();
            try {
                if(map.containsKey("ico")){
                    emoji = Emojis.valueOf(map.getStr("ico"));
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } else throw new RuntimeException("wrong type!");
        return new Option(
                msgBuilder, text, key, description, emoji, isDef, reply, hide
        );
    }


    public static class Option extends SelectOption implements IListener<StringSelectInteractionEvent>{
        private final IMsgBuilder msgBuilder;
        private final boolean reply;
        private final boolean hide;
        private final int hs;

        protected Option(IMsgBuilder builder,String text, String key,
                         String description, Emojis e, boolean isDefault, boolean reply, boolean hide
        ) {
            super(text, key, description, isDefault, e==null?null:Emoji.fromUnicode(e.E));
            hs = key.hashCode();
            this.msgBuilder = builder;
            this.reply = reply;
            this.hide = hide;
        }


        @Override
        public int hashCode() {
            return hs;
        }


        @Override
        public boolean equals(Object obj) {
            return obj.hashCode()==hs;
        }


        @Override
        public boolean handle(StringSelectInteractionEvent event) {
            MessageCreateData msg = msgBuilder.build(event);
            if(reply){
                event.getInteraction().reply(msg).setEphemeral(hide).queue();
            } else {
                event.getChannel().sendMessage(msg).queue();
            }
            return false;
        }
    }


    @Override
    public boolean handle(StringSelectInteractionEvent event) {
        options.get(event.getValues().get(0)).handle(event);
        return true;
    }


    @Override
    public Type getType()
    {
        return Type.STRING_SELECT;
    }


    @Override
    public List<SelectOption> getOptions() {
        return new ArrayList<>(options.values());
    }


    @Override
    public DataObject toData() {
        return super.toData()
                .put("type", Type.STRING_SELECT.getKey())
                .put("options", DataArray.fromCollection(options.values()));
    }


    @Override
    public int hashCode() {
        return id.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof StringSelectMenu))
            return false;
        StringSelectMenu other = (StringSelectMenu) obj;
        return Objects.equals(id, other.getId())
                && Objects.equals(placeholder, other.getPlaceholder())
                && minValues == other.getMinValues()
                && maxValues == other.getMaxValues()
                && disabled == other.isDisabled()
                && Objects.equals(options, other.getOptions());
    }
}

