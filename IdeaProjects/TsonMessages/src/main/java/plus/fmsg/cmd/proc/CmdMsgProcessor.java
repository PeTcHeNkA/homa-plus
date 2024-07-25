package plus.fmsg.cmd.proc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.text.ITextComponent;
import plus.fmsg.cmd.CmdMessage;
import plus.fmsg.cmd.McVarProvider;
import plus.fmsg.msg.TextCompBuilder;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class CmdMsgProcessor {
    public static Consumer<CmdMessage> fromTson(TsonObj obj) {
        if(obj == null) return msg -> {};
        if(obj.isMap()) return fromMap(obj.getMap());
        if(obj.isList()) return fromList(obj.getList());
        throw new RuntimeException("Unsupported object: " + obj);
    }


    private static Consumer<CmdMessage> fromList(TsonList list) {
        if(list == null || list.isEmpty()) return msg -> {};
        Consumer<CmdMessage>[] consumers = new Consumer[list.size()];
        for(int i = 0; i < list.size(); i++){
            consumers[i] = fromTson(list.get(i));
        }
        return msg -> {
            for(Consumer<CmdMessage> consumer : consumers) consumer.accept(msg);
        };
    }


    private static Consumer<CmdMessage> fromMap(TsonMap map) {
        if(map == null) return msg -> {};

        String type = map.getStrSafe("type");
        if(type == null){
            if(map.containsKey("msg")){
                type = "msg";
            } else if(map.containsKey("if")){
                type = "if";
            }
            if(type == null){
                throw new RuntimeException("Unknown type: " + map);
            }
        }
        switch (type){
            case "tree":{
                Map<String, Consumer<CmdMessage>> treeMap = new HashMap<>();
                map.forEach((s, obj) -> {
                    if(s.equals("type"))return;
                    treeMap.put(s, fromTson(obj));
                });
                return new TreeProc(treeMap, fromTson(map.get("def")));
            }
            case "msg":{
                TsonObj obj = map.getMap("msg");
                if(obj.isString())return new SendTextProcSimple(obj.getStr());
                return new SendTextProc(obj);
            }
            case "if":{
                TsonObj obj = map.get("if");
                Predicate<CmdMessage> pred = CmdConduction.ofTson(obj);
                Consumer<CmdMessage> then = fromTson(map.get("then"));
                Consumer<CmdMessage> els = fromTson(map.get("else"));
                return msg -> {
                    if(pred.test(msg))then.accept(msg);
                    else els.accept(msg);
                };
            }
        }
        throw new RuntimeException("Unknown type: " + map);
    }


    private static final class SendTextProcSimple implements Consumer<CmdMessage> {
        private final IVarGetter<CmdMessage> format;

        public SendTextProcSimple(String format) {
            this.format = TextFormatter.compile(format, McVarProvider.INSTANCE);
        }

        @Override
        public void accept(CmdMessage msg) {
            msg.reply(format.get(msg));
        }
    }


    private static final class TreeProc implements Consumer<CmdMessage> {
        private final ImmutableMap<String, Consumer<CmdMessage>> map;
        private final Consumer<CmdMessage> def;

        public TreeProc(Map<String, Consumer<CmdMessage>> map, Consumer<CmdMessage> def) {
            this.map = ImmutableMap.copyOf(map);
            this.def = def;
        }


        @Override
        public void accept(CmdMessage msg) {
            String cur = msg.curArg();
            if(cur == null){
                def.accept(msg);
                return;
            }
            Consumer<CmdMessage> proc = map.get(cur);
            if(proc == null){
                def.accept(msg);
                return;
            }
            msg.addCursor();
            proc.accept(msg);
        }
    }


    private static final class SendTextProc implements Consumer<CmdMessage> {
        private final Function<CmdMessage, ITextComponent> builder;

        public SendTextProc(TsonObj obj) {
            this.builder = TextCompBuilder.ofMap(obj);
        }

        @Override
        public void accept(CmdMessage msg) {
            msg.reply(builder.apply(msg));
        }
    }
}
