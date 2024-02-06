package plus.ds.msg;

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import plus.ds.IrisSystem;
import plus.ds.api.ILayoutBuilder;
import plus.ds.api.IMessageEvent;
import plus.ds.api.IMsgAction;
import plus.ds.api.IMsgBuilder;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.ArrayList;


public class ComplexMsg implements IMsgBuilder {
    private final IMsgAction[] applicators;
    public ComplexMsg(IrisSystem.GuildProgram program, TsonMap map) {
        ArrayList<IMsgAction> parts = new ArrayList<>();

        map.ifContainsStr("msg", s -> parts.add(new FormattedMsg(s)));

        map.ifContains("embeds",  obj -> addEmbeds(parts, obj)           );
        map.ifContains("buttons", obj -> addButtons(program, parts, obj) );
        map.ifContains("layouts", obj -> addLayouts(program, parts, obj) );

        applicators = parts.toArray(new IMsgAction[0]);
    }


    private static void addButtons(IrisSystem.GuildProgram program, ArrayList<IMsgAction> parts, TsonObj obj){
        if(obj.isList()){
            for(TsonObj obj0: obj.getList()){
                parts.add(new ButtonMsg(program, obj0.getMap()));
            }
        } else {
            parts.add(new ButtonMsg(program, obj.getMap()));
        }
    }


    private static void addLayouts(IrisSystem.GuildProgram program, ArrayList<IMsgAction> parts, TsonObj obj){
        if(obj.isList()){
            for(TsonObj obj0: obj.getList()){
                parts.add(ILayoutBuilder.build(program, obj0.getMap()));
            }
        } else {
            parts.add(ILayoutBuilder.build(program, obj));
        }
    }


    private static void addEmbeds(ArrayList<IMsgAction> parts, TsonObj obj){
        if(obj.isMap()){
            parts.add(EmbedMsg.build(obj.getMap()));
        } else if(obj.isList()){
            for(TsonObj obj0: obj.getList()){
                if(obj0.isMap()){
                    parts.add(EmbedMsg.build(obj0.getMap()));
                } else {
                    System.out.println("Not supported -> ["+obj+"]");
                }
            }
        } else {
            System.out.println("Not supported -> ["+obj+"]");
        }
    }


    @Override
    public MessageCreateData build(IMessageEvent event) {
        MessageCreateBuilder builder = new MessageCreateBuilder();
        for (IMsgAction applicator:applicators){
            applicator.apply(builder, event);
        }
        return builder.build();
    }
}