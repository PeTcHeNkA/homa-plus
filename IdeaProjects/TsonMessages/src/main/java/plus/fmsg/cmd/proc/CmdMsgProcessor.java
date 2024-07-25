package plus.fmsg.cmd.proc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.text.ITextComponent;
import plus.fmsg.cmd.CmdMessage;
import plus.fmsg.cmd.McVarProvider;
import plus.fmsg.msg.TextCompBuilder;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.ext.TsonFuncUtils;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;


public class CmdMsgProcessor {
    public static Consumer<CmdMessage> ofTson(TsonObj obj) {
        return TsonFuncUtils.consumerOf(obj, CmdMsgProcessor::ofTsonBase, CmdConduction::ofTsonBase);
    }


    public static Consumer<CmdMessage> ofTsonBase(TsonObj obj) {
        if(obj.isMap()) return ofTsonMap(obj.getMap());
        throw new RuntimeException("Unsupported object: " + obj);
    }


    private static Consumer<CmdMessage> ofTsonMap(TsonMap map) {
        if(map == null) return msg -> {};

        String type = map.getStrSafe("type");
        if(type == null){
            if(map.containsKey("msg")){
                type = "msg";
            }
            if(type == null){
                throw new RuntimeException("Unknown type: " + map);
            }
        }
        switch (type){
            case "tree":{
                Map<String, Consumer<CmdMessage>> treeMap = new HashMap<>();
                map.forEach((s, obj) -> {
                    if(s.equals("type") || s.equals("def"))return;
                    treeMap.put(s, ofTson(obj));
                });
                return new TreeProc(treeMap, ofTson(map.get("def")));
            }
            case "msg":{
                TsonObj obj = map.getMap("msg");
                if(obj.isString())return new SendTextProcSimple(obj.getStr());
                return new SendTextProc(obj);
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
