package plus.fmsg.cmd.proc;

import plus.fmsg.cmd.CmdMessage;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.ext.TsonFuncUtils;

import java.util.*;
import java.util.function.Function;


public class CompleteCmdProc {
    private static final Function<CmdMessage, List<String>> EMPTY = msg -> Collections.emptyList();

    public static Function<CmdMessage, List<String>> ofTson(TsonObj obj){
        if(obj == null) return EMPTY;
        return TsonFuncUtils.functionOf(obj, CompleteCmdProc::ofTsonBase, CmdConduction::ofTsonBase);
    }


    protected static Function<CmdMessage, List<String>> ofTsonBase(TsonObj obj){
        if(obj == null) return EMPTY;
        if(obj.isMap()) return fromMap(obj.getMap());
        if(obj.isList()) return fromList(obj.getList());
        if(obj.isString())return fromStr(obj.getStr());
        return EMPTY;
    }


    private static Function<CmdMessage, List<String>> fromStr(String str){
        if(str.charAt(0) == '%' && str.charAt(str.length()-1) == '%'){
            String var = str.substring(1, str.length()-1);

            switch (var){
                case "player-names":
                    return msg -> {
                        ArrayList<String> list = new ArrayList<>();
                        msg.getServer().getPlayerList().getPlayers().forEach(
                                player -> list.add(player.getName())
                        );
                        return list;
                    };
                case "world-player-names":
                    return msg -> {
                        if(!msg.fromPlayer())return Collections.emptyList();
                        ArrayList<String> list = new ArrayList<>();
                        msg.getPlayer().getServerWorld().playerEntities.forEach(
                                player -> list.add(player.getName())
                        );
                        return list;
                    };
            }
        }
        return new ConstListProc(Collections.singletonList(str));
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
            strings.trimToSize();
            return new ConstListProc(strings);
        } else {
            Function<CmdMessage, List<String>>[] procs = new Function[list.size()];
            for (int i = 0; i < list.size(); i++) {
                procs[i] = ofTson(list.get(i));
            }
            return new MergedListProc(merge(procs));
        }
    }


    private static Function<CmdMessage, List<String>>[] merge(Function<CmdMessage, List<String>>[] procs){
        ArrayList<String> constReults = new ArrayList<>();
        ArrayList<Function<CmdMessage, List<String>>> newProcs = new ArrayList<>();
        mergeTree(procs, constReults, newProcs);
        if(!constReults.isEmpty()){
            newProcs.add(new ConstListProc(constReults));
        }
        return newProcs.toArray(new Function[0]);
    }


    private static void mergeTree(
            Function<CmdMessage, List<String>>[] procs,
            ArrayList<String> constReults,
            ArrayList<Function<CmdMessage, List<String>>> newProcs
    ){
        for (Function<CmdMessage, List<String>> proc : procs) {
            if(proc instanceof ConstListProc){
                constReults.addAll(((ConstListProc)proc).list);
            } else {
                if(proc instanceof MergedListProc){
                    mergeTree(((MergedListProc)proc).procs, constReults, newProcs);
                } else {
                    newProcs.add(proc);
                }
            }
        }
    }


    private static Function<CmdMessage, List<String>> fromMap(TsonMap map){
        String type = map.getStrSafe("type");
        if(type == null){
            return EMPTY;
        }

        switch (type) {
            case "tree":{
                Map<String, Function<CmdMessage, List<String>>> treeMap = new HashMap<>();
                map.forEach((s, obj) -> {
                    if(s.equals("type") || s.equals("def"))return;
                    treeMap.put(s, ofTson(obj));
                });
                return new TreeProc(treeMap, ofTson(map.get("def")));

            }
        }

        return EMPTY;
    }


    private static final class TreeProc implements Function<CmdMessage, List<String>> {
        private final Map<String, Function<CmdMessage, List<String>>> map;
        private final Function<CmdMessage, List<String>> def;

        public TreeProc(Map<String, Function<CmdMessage, List<String>>> map, Function<CmdMessage, List<String>> def) {
            this.map = map;
            this.def = def;
        }


        @Override
        public List<String> apply(CmdMessage msg) {
            String cur = msg.curArg();
            if(cur == null)return def.apply(msg);
            Function<CmdMessage, List<String>> proc = map.get(cur);
            if(proc == null)return def.apply(msg);
            return proc.apply(msg);
        }
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