package plus.sol.core.math;

import plus.sol.api.IEnvironment;
import plus.sol.api.IVarProxy;
import java.util.ArrayList;
import java.util.function.Function;


public abstract class Editor<U, T> {

    public static<U,T> Editor<U,T> build(Function<String, IVarProxy<U,T>> builder, ArrayList<String> args){
        if(args.size()<3)throw new RuntimeException("Must be more than 3 args!");

        IVarProxy<U,T> var = builder.apply(args.remove(0));
        Operator<U,T> op = Operator.build(args.remove(0));
        if(args.size()==1){
            return build(var, op, builder.apply(args.get(0)));
        } else {
            Function<String, IVarProxy<U,?>> builder2 = s -> (IVarProxy<U, ?>) builder.apply(s);
            return build(var, op, (IVarProxy<U, T>) Mather.build(builder2, args));
        }
    }


    public static<U,T> Editor<U,T> build(IVarProxy<U, T> var, Operator<U,T> op, IVarProxy<U, T> val){
        if(val instanceof IVarProxy.Constant){
            return new SEditor<>(var,op,val.getValue(null,null));
        } else {
            return new NEditor<>(var,op,val);
        }
    }


    public abstract void edit(U user, IEnvironment<U> env);
}


class NEditor<U,T> extends Editor<U,T>{
    private final IVarProxy<U, T> var;
    private final Operator<U,T> operator;
    private final IVarProxy<U,T> getter;

    NEditor(IVarProxy<U, T> var, Operator<U, T> operator, IVarProxy<U, T> getter) {
        this.var = var;
        this.operator = operator;
        this.getter = getter;
    }

    @Override
    public void edit(U user, IEnvironment<U> env) {
        operator.apply(var, user, env, getter.getValue(user, env));
    }
}


class SEditor<U,T> extends Editor<U,T>{
    private final IVarProxy<U, T> var;
    private final Operator<U,T> operator;
    private final T value;

    SEditor(IVarProxy<U, T> var, Operator<U, T> operator, T value) {
        this.var = var;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public void edit(U user, IEnvironment<U> env) {
        operator.apply(var, user, env, value);
    }
}


abstract class Operator<U,T>{
    static<U,T> Operator<U, T> build(String s){
        if(s.equals("=")){
            return new Operator<U, T>() {
                @Override
                void apply(IVarProxy<U, T> var, U user, IEnvironment<U> env, T val) {
                    var.setValue(user, env, val);
                }
            };
        }
        if(s.equals("+=")){
            return (Operator<U, T>) new Operator<U, Number>() {
                @Override
                void apply(IVarProxy<U, Number> var, U user, IEnvironment<U> env, Number val) {
                    Number val2 = var.getValue(user, env).doubleValue() + val.doubleValue();

                    var.setValue(user, env, val2);
                }
            };
        }
        if(s.equals("-=")){
            return (Operator<U, T>) new Operator<U, Number>() {
                @Override
                void apply(IVarProxy<U, Number> var, U user, IEnvironment<U> env, Number val) {
                    Number val2 = var.getValue(user, env).doubleValue() - val.doubleValue();

                    var.setValue(user, env, val2);
                }
            };
        }
        throw new UnsupportedOperationException(s);
    }

    abstract void apply(IVarProxy<U, T> var, U user, IEnvironment<U> env, T val);
}

