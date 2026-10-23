package plus.ds.api;

import plus.ds.IrisSystem;
import plus.ds.memory.MemoryFilter;
import plus.ds.memory.MemoryReader;
import plus.ds.memory.MemoryWriter;
import plus.tson.TsonMap;
import plus.tson.TsonObj;

import java.util.ArrayList;
import java.util.regex.Pattern;


@FunctionalInterface
public interface IMsgFilter {
    boolean isSuccess(IMessageEvent event, String msg);

    static IMsgFilter build(IrisSystem.GuildProgram program, TsonObj obj){
        String type;
        TsonMap map;
        if(obj==null)return null;
        if(obj.isMap()){
            map = obj.getMap();
            type = map.getOrDefaultStr("type","eq");
        } else if(obj.isString()) {
            type = "eq";
            map = new TsonMap();
            map.put("msg", obj);
        } else if(obj.isList()){
            ArrayList<IMsgFilter> filters = new ArrayList<>();
            for(TsonObj obj1: obj.getList()){
                IMsgFilter filter = build(program, obj1);
                if(filter!=null)filters.add(filter);
            }
            return new IMsgFilter() {
                private final IMsgFilter[] finFilters = filters.toArray(filters.toArray(new IMsgFilter[0]));
                @Override
                public boolean isSuccess(IMessageEvent e, String msg) {
                    for(IMsgFilter filter:finFilters){
                        if(!filter.isSuccess(e, msg))return false;
                    }
                    return true;
                }
            };
        } else return null;

        String suffix;
        if(type.contains(":")){
            int i = type.indexOf(":");
            type = type.substring(0,i);
            suffix = type.substring(i);
        } else {
            suffix = null;
        }
        String msg0;
        if(suffix != null){
            switch (type){
                case "guild":return buildGuildFilter(map, suffix);
                case "btn":  return buildButtonFilter(map, suffix);
                case "var":  return buildVarFilter(program, map, suffix);
            }
            msg0 = suffix;
        } else {
            msg0 = map.getStrSafe("msg");
        }
        boolean invert = type.startsWith("!");
        if(invert){
            type = type.substring(1);
        }

        switch (type){
            case "true":
                return pre(useTrue, invert);
            case "not-null":
                return pre(notNull, invert);
            case "eq": case "equal":
                return pre((e, msg) -> msg.equals(msg0), invert);
            case "start-with": case "stw":
                return pre((e, msg) -> msg.startsWith(msg0), invert);
            case "end-with": case "edw":
                return pre((e, msg) -> msg.endsWith(msg0), invert);
            case "regex": case "rex":
                return pre(new IMsgFilter() {
                    private final Pattern pattern = Pattern.compile(msg0);
                    @Override
                    public boolean isSuccess(IMessageEvent e, String msg) {
                        return pattern.matcher(msg).find();
                    }
                }, invert);
            case "plugin": case "plug":
                try {
                    return pre((IMsgFilter) map.getField("handler"), invert);
                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            case "update":
                return pre(onlyUpdate, invert);
            case "not-update":
            case "-update":
                return pre(notUpdate, invert);
            case "create":
                return pre(onlyCreate, invert);
            case "not-create":
            case "-create":
                return pre(notCreate, invert);
            case "delete":
                return pre(onlyDelete, invert);
            case "not-delete":
            case "-delete":
                return pre(notDelete, invert);
        }
        return null;
    }


    IMsgFilter onlyUpdate = (event, msg) -> event.type() == IMessageEvent.Type.UPDATE;
    IMsgFilter notUpdate  = (event, msg) -> event.type() != IMessageEvent.Type.UPDATE;
    IMsgFilter onlyCreate = (event, msg) -> event.type() == IMessageEvent.Type.CREATE;
    IMsgFilter notCreate  = (event, msg) -> event.type() != IMessageEvent.Type.CREATE;
    IMsgFilter onlyDelete = (event, msg) -> event.type() == IMessageEvent.Type.DELETE;
    IMsgFilter notDelete  = (event, msg) -> event.type() != IMessageEvent.Type.DELETE;
    IMsgFilter notNull    = (event, msg) -> msg != null;
    IMsgFilter useTrue    = (event, msg) -> true;

    static IMsgFilter pre(IMsgFilter src, boolean invert){
        if(invert)return (event, msg) -> !src.isSuccess(event, msg);
        return src;
    }

    static IMsgFilter buildButtonFilter(TsonObj obj, String type){

        return null;
    }

    static IMsgFilter buildVarFilter(IrisSystem.GuildProgram program, TsonMap map, String suffix) {
        switch (suffix){
            case "check": return new MemoryFilter(program, map);
            case "set":   return new MemoryWriter(program, map);
        }
        return null;
    }

    static IMsgFilter buildGuildFilter(TsonObj obj, String type){
//        switch (type){
//            case
//        }
        return null;
    }
}