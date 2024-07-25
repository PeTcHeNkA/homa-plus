package plus.fmsg.cmd.proc;

import plus.fmsg.cmd.CmdMessage;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;


public class CompleteCmdProc {
    private static final Function<CmdMessage, List<String>> EMPTY = msg -> Collections.emptyList();

    public static Function<CmdMessage, List<String>> fromTson(TsonObj obj){
        if(obj == null) return EMPTY;
        if(obj.isMap()) return fromMap(obj.getMap());
        if(obj.isList()) return fromList(obj.getList());
        return EMPTY;
    }


    private static Function<CmdMessage, List<String>> fromList(TsonList list){
        if(list == null) return EMPTY;
        if(list.isEmpty()) return EMPTY;

        boolean allString = true;
        for (TsonObj obj : list) {
            if(!(obj.isString() || obj.isNumber() || obj.isBool())){
                allString = false;
                break;
            }
        }

        if(allString){
            ArrayList<String> strings = new ArrayList<>(list.size());
            for (TsonObj obj : list) {
                strings.add(obj.getStr());
            }
            return new ConstListProc(strings);
        } else {
            Function<CmdMessage, List<String>>[] procs = new Function[list.size()];
            for (int i = 0; i < list.size(); i++) {
                procs[i] = fromTson(list.get(i));
            }
            return new MergedListProc(procs);
        }
    }


    private static Function<CmdMessage, List<String>> fromMap(TsonMap map){
        String type = map.getStrSafe("type");
        if(type == null){
            if(map.containsKey("if")){
                type = "if";
            }

            if(type == null){
                return EMPTY;
            }
        }

        switch (type) {
            case "if":{
                TsonObj obj = map.get("if");
                Predicate<CmdMessage> pred = CmdConduction.ofTson(obj);
                Function<CmdMessage, List<String>> then = fromTson(map.get("then"));
                Function<CmdMessage, List<String>> els = fromTson(map.get("else"));
                return msg -> {
                    if(pred.test(msg))return then.apply(msg);
                    else return els.apply(msg);
                };
            }
        }

        return EMPTY;
    }


    private static final class MergedListProc implements Function<CmdMessage, List<String>> {
        private final Function<CmdMessage, List<String>>[] procs;

        public MergedListProc(Function<CmdMessage, List<String>>[] procs){
            this.procs = procs;
        }

        @Override
        public List<String> apply(CmdMessage msg) {
            ArrayList<String> list = new ArrayList<>();
            for (Function<CmdMessage, List<String>> proc : procs) {
                list.addAll(proc.apply(msg));
            }
            return list;
        }
    }


    private static final class ConstListProc implements Function<CmdMessage, List<String>> {
        private final List<String> list;

        public ConstListProc(List<String> list){
            this.list = list;
        }

        @Override
        public List<String> apply(CmdMessage msg) {
            return list;
        }
    }
}