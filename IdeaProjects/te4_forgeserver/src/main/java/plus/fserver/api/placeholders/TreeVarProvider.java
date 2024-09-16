package plus.fserver.api.placeholders;

import plus.tson.format.IVarGetter;
import plus.tson.format.IVarProvider;
import plus.tson.utl.Te4HashMap;
import java.util.function.Function;


public class TreeVarProvider<T,S extends TreeVarProvider<T,S>> extends Te4HashMap<String, Object>
        implements IVarProvider<T>, Function<TreeVarProvider.Args,IVarGetter<T>>{
    @Override
    public IVarGetter<T> getFmGetter(String name) {
        return apply(new Args(name));
    }

    public S add(String name){
        S result = create();
        fput(name, result);
        return result;
    }


    protected S create(){
        return (S)new TreeVarProvider<>();
    }


    public S add(String name, Function<TreeVarProvider.Args,IVarGetter<T>> func){
        fput(name, func);
        return (S) this;
    }


    public S add(String name, IVarGetter<T> func){
        fput(name, func);
        return (S) this;
    }


    @Override
    public IVarGetter<T> apply(Args args) {
        Object raw = super.get(args.cur());

        if(raw != null){
            args.shift();
            if(raw instanceof Function) return (IVarGetter<T>)((Function)raw).apply(args);
            if(raw instanceof IVarGetter) return (IVarGetter<T>) raw;
        }
        return null;
    }


    static class Args{
        private String[] args;
        private int cursor = 0;

        Args(String name) {
            this.args = name.split(":");
        }
        public String cur(){
            return args[cursor];
        }
        public String get(int i){
            return args[cursor+i];
        }
        public int length(){
            return args.length-cursor;
        }
        public void shift(){
            ++cursor;
        }
        public boolean isLast(){
            return args.length == cursor-1;
        }
        public String allLast(){
            if(length() == 0) return "";
            if(length() == 1) return args[cursor];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length - cursor; i++) {
                sb.append(args[cursor+i]).append(":");
            }
            sb.setLength(sb.length()-1);
            return sb.toString();
        }
    }
}