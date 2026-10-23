package plus.sol.core.math;

import plus.sol.api.IEnvironment;
import plus.sol.api.IVarProxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;


public abstract class Conduction<U> {

    public static<U> Conduction<U> build(Function<String, IVarProxy<U, ?>> builder, Object[] fArgs){
        ArrayList<ArrayList<String>> codes = new ArrayList<>();

        int index = 0;
        for (Object arg0:fArgs){
            String s = arg0.toString();

            if(s.equals("&&") || s.equals("||")){
                index +=2;
                codes.add(new ArrayList<>(Collections.singleton(s)));
                continue;
            }
            ArrayList<String> c;
            if(codes.size()<=index){
                c = new ArrayList<>();
                codes.add(c);
            } else {
                c = codes.get(index);
            }
            c.add(s);
        }
        ArrayList<String> last = codes.get(codes.size() - 1);
        if(last.get(0).equals("||") || last.get(0).equals("&&"))codes.remove(codes.size()-1);

        Conduction c = new BConduction(builder, codes.remove(0));
        while (!codes.isEmpty()){
            BinLogic l = BinLogic.build(codes.remove(0).get(0));
            ArrayList<String> args = codes.remove(0);
            if(args.size()>1){
                if(args.size()<5){
                    c = new LConduction(new BConduction(
                            builder.apply(args.get(0)),
                            Logic.build(args.get(1)),
                            builder.apply(args.get(2))
                    ), l, c);
                } else {
                    ArrayList<String> args1 = new ArrayList<>();
                    int i = 0;
                    String arg = args.get(i);
                    while (!Logic.isLogic(arg)) {
                        args1.add(arg);
                        i++;
                        arg = args.get(i);
                    }
                    String logic = arg;
                    i++;
                    ArrayList<String> args2 = new ArrayList<>();
                    while (i < args.size()) {
                        args2.add(args.get(i));
                        i++;
                    }
                    c = new LConduction(new BConduction(
                            Mather.build(builder, args1),
                            Logic.build(logic),
                            Mather.build(builder, args2)
                    ), l, c);
                }
            } else c = new LConduction(new BConduction(builder, args), l, c);
        }
        return c;
    }


    public abstract boolean checkFor(U user, IEnvironment<U> env);
}


class BConduction<U, T> extends Conduction<U>{
    private final Logic l;
    private final IVarProxy<U, T> o1;
    private final IVarProxy<U, T> o2;

    public BConduction(Function<String, IVarProxy<U, T>> builder, ArrayList<String> args){
        this(builder, args.get(0), args.get(1), args.get(2));
    }


    public BConduction(Function<String, IVarProxy<U, T>> builder,
                       String operand1, String logic, String operand2){
        this(builder.apply(operand1), Logic.build(logic), builder.apply(operand2));
    }


    public BConduction(IVarProxy<U, T> operand1, Logic logic, IVarProxy<U, T> operand2){
        l = logic;
        o1 = operand1;
        o2 = operand2;
    }


    @Override
    public boolean checkFor(U user, IEnvironment<U> env) {
        return l.checkFor(o1.getValue(user, env), o2.getValue(user, env));
    }
}


class LConduction<U extends Conduction<U>> extends Conduction<U>{
    private final BinLogic l;
    private final Conduction<U> o1;
    private final Conduction<U> o2;


    public LConduction(Conduction<U> operand1, BinLogic logic, Conduction<U> operand2){
        l = logic;
        o1 = operand1;
        o2 = operand2;
    }


    @Override
    public boolean checkFor(U user, IEnvironment<U> env) {
        return l.checkFor(o1.checkFor(user, env), o2.checkFor(user, env));
    }
}