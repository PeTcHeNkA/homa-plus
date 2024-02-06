package plus.ds.api;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import plus.ds.IrisSystem;
import plus.ds.msg.ComplexMsg;
import plus.ds.msg.FormattedMsg;
import plus.ds.msg.SimpleMsg;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonStr;


@FunctionalInterface
public interface IMsgBuilder {
    IMsgBuilder EMPTY = event -> null;
    MessageCreateData build(IMessageEvent event);

    static IMsgBuilder build(IrisSystem.GuildProgram program, TsonMap map0){
        String type;
        TsonMap map;
        TsonObj obj = map0.get("msg");
        if(obj==null)return EMPTY;
        if(obj.isMap()){
            map = obj.getMap();
            type = map.getStrSafe("type");
            if(type==null){
                if(obj.isMap()){
                    type = "adv";
                } else {
                    type = "str";
                }
            }
        } else {
            if(obj.isString()){
                String str = obj.getStr();
                if(str.isBlank() || str.equalsIgnoreCase("null"))return EMPTY;
            }
            map = new TsonMap();
            map.put("msg", obj);
            type = map0.getOrDefault("type",new TsonStr("str")).getStr();
        }
        switch (type){
            case "str": case "string": case "simple":
                return new SimpleMsg(map.getStr("msg"));
            case "fm-str": case "formatted-string": case "format": case "fm-msg":
                return new FormattedMsg(map.getStr("msg"));
            case "adv": case "adventure": case "com": case "complex": case "dif": case "difficult":
                return new ComplexMsg(program, map);
            case "plugin": case "plug":
                try {
                    return (IMsgBuilder) map.getField("msg");
                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
        }
        System.out.println("MESSAGE TYPE NOT EXIST! ("+type+")");
        return EMPTY;
    }
}





