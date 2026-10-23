package plus.sol.core.math;

import plus.sol.api.IEnvironment;
import plus.sol.api.IVarProxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public abstract class Mather<U> implements IVarProxy<U, Object> {

    public static<U> IVarProxy<U, Object> build(Function<String, IVarProxy<U,?>> b2, ArrayList<String> codes) {
        Function<String, IVarProxy<U,Number>> builder = s -> (IVarProxy<U, Number>) b2.apply(s);
        if(codes.size()<3){
            List<String> finalCodes = codes;
            return new IVarProxy<U, Object>() {
                private final IVarProxy<U, Object> n = (IVarProxy<U, Object>) b2.apply(finalCodes.get(0));
                @Override
                public Object getValue(U user, IEnvironment<U> env) {
                    return n.getValue(user, env);
                }
            };
        }
        codes = new ArrayList<>(codes);
        IVarProxy c = new BMather<U>(builder, codes.remove(0), codes.remove(0), codes.remove(0));
        while (!codes.isEmpty()){
            MathOperand l = MathOperand.build(codes.remove(0));
            c = new LMather<U>(c, l, builder.apply(codes.remove(0)));
        }
        return (Mather<U>) c;
    }
}


class BMather<U> extends Mather<U>{
    private final MathOperand l;
    private final IVarProxy<U,Number> o1;
    private final IVarProxy<U,Number> o2;

//    public BMather(Function<String, IVarGetter<U, Number>> builder, ArrayList<String> args){
//        this(builder, args.get(0), args.get(1), args.get(2));
//    }


    public BMather(Function<String, IVarProxy<U,Number>> builder,
                   String operand1, String logic, String operand2){
        this(builder.apply(operand1), MathOperand.build(logic), builder.apply(operand2));
    }


    public BMather(IVarProxy<U,Number> operand1, MathOperand logic, IVarProxy<U,Number> operand2){
        l = logic;
        o1 = operand1;
        o2 = operand2;
    }


    @Override
    public Number getValue(U user, IEnvironment<U> env) {
        return l.calc(o1.getValue(user, env), o2.getValue(user, env));
    }
}


class LMather<U> implements IVarProxy<U, Number>{
    private final IVarProxy<U, Number> mather;
    private final MathOperand l;
    private final IVarProxy<U,Number> o1;

    public LMather(IVarProxy<U, Number> mather, MathOperand logic, IVarProxy<U,Number> operand1){
        this.mather = mather;
        l = logic;
        o1 = operand1;
    }


    @Override
    public Number getValue(U user, IEnvironment<U> env) {
        return l.calc(mather.getValue(user, env), o1.getValue(user, env));
    }
}