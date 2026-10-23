package plus.quest.script.quest.logic;

import plus.quest.AbstractQuestSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class Mather extends PlaceHolder<Number>{

    public static Mather build(String data){
        return build(Arrays.asList(data.replace("  ", " ").split(" ")));
    }

    public static Mather build(List<String> codes){
        if(codes.size()<3){
            List<String> finalCodes = codes;
            return new Mather() {
                private final PlaceHolder<Number> n = PlaceHolder.build(finalCodes.get(0));
                @Override
                public Number getValue(AbstractQuestSession session) {
                    return n.getValue(session);
                }
            };
        }
        codes = new ArrayList<>(codes);
        Mather c = new BMather(codes.remove(0), codes.remove(0), codes.remove(0));
        while (!codes.isEmpty()){
            MathOperand l = MathOperand.build(codes.remove(0));
            c = new LMather(c, l, PlaceHolder.build(codes.remove(0)));
        }
        return c;
    }
}

class BMather extends Mather{
    private final MathOperand l;
    private final PlaceHolder<Number> o1;
    private final PlaceHolder<Number> o2;

    public BMather(ArrayList<String> args){
        this(args.get(0), args.get(1), args.get(2));
    }


    public BMather(String operand1, String logic, String operand2){
        this(PlaceHolder.build(operand1), MathOperand.build(logic), PlaceHolder.build(operand2));
    }


    public BMather(PlaceHolder<Number> operand1, MathOperand logic, PlaceHolder<Number> operand2){
        l = logic;
        o1 = operand1;
        o2 = operand2;
    }


    @Override
    public Number getValue(AbstractQuestSession session) {
        return l.calc(o1.getValue(session), o2.getValue(session));
    }
}


class LMather extends Mather{
    private final Mather mather;
    private final MathOperand l;
    private final PlaceHolder<Number> o1;

    public LMather(Mather mather, MathOperand logic, PlaceHolder<Number> operand1){
        this.mather = mather;
        l = logic;
        o1 = operand1;
    }

    @Override
    public Number getValue(AbstractQuestSession session) {
        return l.calc(mather.getValue(session), o1.getValue(session));
    }
}