package plus.ds.api;

import plus.tson.TsonMap;
import plus.tson.TsonObj;

import java.util.ArrayList;
import java.util.regex.Pattern;


@FunctionalInterface
public interface IMsgFilter {
    boolean isSuccess(IMessageEvent event, String msg);

    static IMsgFilter build(TsonObj obj){
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
                IMsgFilter filter = build(obj1);
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
            }
            msg0 = suffix;
        } else {
            msg0 = map.getStrSafe("msg");
        }

        switch (type){
            case "eq": case "equal":
                return (e, msg) -> msg.equals(msg0);
            case "start-with": case "stw":
                return (e, msg) -> msg.startsWith(msg0);
            case "end-with": case "edw":
                return (e, msg) -> msg.endsWith(msg0);
            case "regex": case "rex":
                return new IMsgFilter() {
                    private final Pattern pattern = Pattern.compile(msg0);
                    @Override
                    public boolean isSuccess(IMessageEvent e, String msg) {
                        return pattern.matcher(msg).find();
                    }
                };
            case "plugin": case "plug":
                try {
                    return (IMsgFilter) map.getField("handler");
                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            case "update":
                return onlyUpdate;
            case "not-update":
            case "-update":
                return notUpdate;
            case "create":
                return onlyCreate;
            case "not-create":
            case "-create":
                return notCreate;
            case "delete":
                return onlyDelete;
            case "not-delete":
            case "-delete":
                return notDelete;
        }
        return null;
    }


    IMsgFilter onlyUpdate = (event, msg) -> event.type() == IMessageEvent.Type.UPDATE;
    IMsgFilter notUpdate  = (event, msg) -> event.type() != IMessageEvent.Type.UPDATE;
    IMsgFilter onlyCreate = (event, msg) -> event.type() == IMessageEvent.Type.CREATE;
    IMsgFilter notCreate  = (event, msg) -> event.type() != IMessageEvent.Type.CREATE;
    IMsgFilter onlyDelete = (event, msg) -> event.type() == IMessageEvent.Type.DELETE;
    IMsgFilter notDelete  = (event, msg) -> event.type() != IMessageEvent.Type.DELETE;


    static IMsgFilter buildButtonFilter(TsonObj obj, String type){

        return null;
    }

    static IMsgFilter buildGuildFilter(TsonObj obj, String type){
//        switch (type){
//            case
//        }
        return null;
    }
}