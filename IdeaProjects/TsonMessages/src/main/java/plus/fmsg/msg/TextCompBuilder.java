package plus.fmsg.msg;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import plus.fmsg.cmd.CmdMessage;
import plus.fmsg.cmd.McVarProvider;
import plus.fserver.api.TextComponentAuto;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonStr;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;
import java.util.ArrayList;
import java.util.function.Function;


public class TextCompBuilder implements Function<CmdMessage, ITextComponent> {
    private final Function<CmdMessage, ITextComponent>[] builders;

    public static Function<CmdMessage, ITextComponent> ofMap(TsonObj obj){
        if(obj.isList()){
            return new TextCompBuilder(obj.getList());
        }
        if(obj.isMap()){
            TsonMap map = obj.getMap();
            return new Mutable(map.getMap("style"), map.getStr("text")).simplify();
        }
        return new Const(obj);
    }


    private TextCompBuilder(TsonList list) {
        builders = new Function[list.getList().size()];

        for (int i = 0; i < list.getList().size(); i++) {
            TsonObj obj = list.get(i);
            if(obj.isMap()){
                TsonMap map = obj.getMap();
                builders[i] = new Mutable(map.getMap("style"), map.getStr("text")).simplify();
            } else {
                builders[i] = new Const(obj);
            }
        }
    }


    @Override
    public ITextComponent apply(CmdMessage msg) {
        TextComponentString comp = new TextComponentString("");
        for (Function<CmdMessage, ITextComponent> builder : builders) {
            comp.appendSibling(builder.apply(msg));
        }
        return comp;
    }


    private static class Mutable implements Function<CmdMessage, ITextComponent> {
        private final Function<CmdMessage, Style> styleBuilder;
        private final IVarGetter<CmdMessage> textBuilder;

        public Mutable(TsonMap map, String text) {
            this.styleBuilder = styleBuilder(map);
            this.textBuilder = TextFormatter.compile(text, McVarProvider.INSTANCE);
        }

        @Override
        public ITextComponent apply(CmdMessage cmdMessage) {
            Style style = styleBuilder.apply(cmdMessage);
            String text = textBuilder.get(cmdMessage);
            return new TextComponentAuto(text, style);
        }

        public boolean isConst(){
            return styleBuilder instanceof ConstStyle && textBuilder.isConst();
        }


        public Function<CmdMessage, ITextComponent> simplify(){
            if(!isConst())return this;
            return new Const(this.apply(null));
        }
    }


    private static class MutString implements Function<CmdMessage, TsonObj> {
        private final TextFormatter<CmdMessage> format;

        public MutString(String src) {
            format = TextFormatter.compile(src, McVarProvider.INSTANCE);
        }


        public Function<CmdMessage, TsonObj> simplify(){
            if(format.isConst()){
                return new ConstTson(new TsonStr(format.get(null)));
            }
            return this;
        }


        @Override
        public TsonObj apply(CmdMessage msg) {
            return new TsonStr(format.get(msg));
        }
    }


    private static class ConstTson implements Function<CmdMessage, TsonObj> {
        private final TsonObj src;

        public ConstTson(TsonObj src) {
            this.src = src;
        }

        @Override
        public TsonObj apply(CmdMessage msg) {
            return src;
        }
    }


    private static class MutTsonList implements Function<CmdMessage, TsonObj> {
        private Function<CmdMessage, TsonObj>[] builders;

        public MutTsonList(TsonList src) {
            ArrayList<Function<CmdMessage, TsonObj>> valueBuilders = new ArrayList<>();
            src.getList().forEach(tson -> {
                if(maybeMut(tson)){
                    valueBuilders.add(mutBuilder(tson));
                }else{
                    valueBuilders.add(new ConstTson(tson));
                }
            });
            this.builders = valueBuilders.toArray(new Function[0]);
        }

        @Override
        public TsonList apply(CmdMessage cmdMessage) {
            TsonList list = new TsonList(builders.length);
            for (Function<CmdMessage, TsonObj> valueBuilder : builders) {
                list.add(valueBuilder.apply(cmdMessage));
            }
            return list;
        }
    }


    private static Function<CmdMessage, Style> styleBuilder(TsonObj src){
        if(maybeMut(src)){
            return new MutStyle(mutBuilder(src));
        } else{
            return new ConstStyle(TsonCompUtls.styleFromTson(src));
        }
    }


    private static Function<CmdMessage, TsonObj> mutBuilder(TsonObj src){
        if(src.isList()){
            return new MutTsonList(src.getList());
        }
        if(src.isMap()){
            return new MutTsonMap(src.getMap());
        }
        if(src.isString()){
            return new MutString(src.getStr()).simplify();
        }
        return new ConstTson(src);
    }


    private static class MutTsonMap implements Function<CmdMessage, TsonObj> {
        private final String[] constKeys;
        private final String[] mutKeys;
        private final TsonObj[] constValues;
        private final Function<CmdMessage, TsonObj>[] mutValues;

        public MutTsonMap(TsonMap src) {
            ArrayList<String> constKeys = new ArrayList<>();
            ArrayList<String> mutKeys = new ArrayList<>();
            ArrayList<TsonObj> constValues = new ArrayList<>();
            ArrayList<Function<CmdMessage, TsonObj>> mutValues = new ArrayList<>();

            src.getMap().forEach((s, tson) -> {
                if(maybeMut(tson)){
                    mutKeys.add(s);
                    mutValues.add(mutBuilder(tson));
                }else{
                    constKeys.add(s);
                    constValues.add(tson);
                }
            });

            this.constKeys = constKeys.toArray(new String[0]);
            this.mutKeys = mutKeys.toArray(new String[0]);
            this.constValues = constValues.toArray(new TsonObj[0]);
            this.mutValues = mutValues.toArray(new Function[0]);
        }


        @Override
        public TsonMap apply(CmdMessage msg) {
            TsonMap map = new TsonMap(constKeys.length + mutKeys.length);
            for (int i = 0; i < constKeys.length; i++) {
                map.put(constKeys[i], constValues[i]);
            }

            for (int i = 0; i < mutKeys.length; i++) {
                map.put(mutKeys[i], mutValues[i].apply(msg));
            }
            return map;
        }
    }


    private static class MutStyle implements Function<CmdMessage, Style> {
        private final Function<CmdMessage, TsonObj> builder;

        private MutStyle(Function<CmdMessage, TsonObj> builder) {
            this.builder = builder;
        }

        @Override
        public Style apply(CmdMessage cmdMessage) {
            return TsonCompUtls.styleFromTson(builder.apply(cmdMessage));
        }
    }


    private static class ConstStyle implements Function<CmdMessage, Style> {
        private final Style style;

        public ConstStyle(Style style) {
            this.style = style;
        }

        @Override
        public Style apply(CmdMessage msg) {
            return style;
        }
    }


    private static class Const implements Function<CmdMessage, ITextComponent> {
        private final ITextComponent comp;

        public Const(TsonObj obj) {
            this.comp = TsonCompUtls.msgFromTson(obj);
        }


        public Const(ITextComponent comp) {
            this.comp = comp;
        }


        @Override
        public ITextComponent apply(CmdMessage msg) {
            return comp;
        }
    }


    static boolean maybeMut(TsonObj obj){
        if(obj.isMap()){
            final boolean[] maybeMut = {false};
            obj.getMap().forEach((s, obj1) -> {
                if(maybeMut[0])return;
                if(maybeMut(obj1)) maybeMut[0] = true;
            });
            return maybeMut[0];
        }
        if(obj.isList()){
            for(int i = 0; i < obj.getList().size(); i++){
                if(maybeMut(obj.getList().get(i))){
                    return true;
                }
            }
        }
        if(obj.isString()){
            char[] str = obj.getStr().toCharArray();
            boolean flag = false;
            for (int i = 0; i < str.length; i++) {
                if(str[i] == '%'){
                    if(i == 0 || str[i - 1] != '\\'){
                        if(flag)return true;
                        flag = true;
                    }
                }
            }
        }
        return false;
    }
}
