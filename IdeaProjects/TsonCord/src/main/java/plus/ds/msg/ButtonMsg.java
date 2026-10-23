package plus.ds.msg;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import plus.ds.IrisSystem;
import plus.ds.api.IListener;
import plus.ds.api.IMessageEvent;
import plus.ds.api.IMsgAction;
import plus.ds.dyn.ButtonListener;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class ButtonMsg implements IMsgAction {
    private final Button button;

    public ButtonMsg(IrisSystem.GuildProgram program, TsonMap map){
        ButtonStyle style;
        String content;
        button = Button.of(
                style = styleOf(map),
                content = contentOf(map, style),
                map.getOrDefaultStr("label", content)
        );

        if(map.containsKey("handle")){
            TsonObj handlers = map.get("handle");
            if(handlers.isList()){
                for (TsonObj obj: handlers.getList()){
                    regHandler(program, content, obj);
                }
            } else {
                regHandler(program, content, handlers);
            }
        }
    }


    private static void regHandler(IrisSystem.GuildProgram program, String id, TsonObj obj){
        if(obj.isCustom()){
            program.addButtonListener(id, (IListener<ButtonInteractionEvent>) obj.getField());
        } else if (obj.isMap()) {
            TsonMap map = obj.getMap();
            if(!map.containsKey("id")){
                map.put("id", id);
            }
            new ButtonListener(program, map);
        }
    }


    private static String contentOf(TsonMap map, ButtonStyle style){
        String content = null;
        if(style == ButtonStyle.LINK){
            if(map.containsKey("url")){
                content = map.getStr("url");
            }
        }
        if(style == ButtonStyle.PRIMARY || style== ButtonStyle.SECONDARY ||
                style == ButtonStyle.DANGER || style == ButtonStyle.SUCCESS){
            if(map.containsKey("id")){
                content = map.getStr("id");
            }
        }
        if(content == null){
            content = map.getStr("content");
        }
        return content;
    }


    private static ButtonStyle styleOf(TsonMap map){
        String style = map.getOrDefaultStr("style", "primary").toLowerCase();
        switch (style) {
            case "primary":    return ButtonStyle.PRIMARY;
            case "secondary":  return ButtonStyle.SECONDARY;
            case "danger":     return ButtonStyle.DANGER;
            case "link":       return ButtonStyle.LINK;
            case "success":    return ButtonStyle.SUCCESS;
            case "unknown":    return ButtonStyle.UNKNOWN;
        };
        return null;
    }


    @Override
    public void apply(MessageCreateBuilder msfBuilder, IMessageEvent event) {
        msfBuilder.addActionRow(button);
    }
}
