package plus.quest.script.quest.logic;

import plus.quest.AbstractQuestSession;

import java.util.ArrayList;
import java.util.Collections;


public abstract class Conduction {

    public static Conduction build(String rawData){
        String[] sc = rawData.strip().replace("  ", " ").split(" ");

        ArrayList<ArrayList<String>> codes = new ArrayList<>();

        int index = 0;
        for (String s:sc){
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

        Conduction c = new BConduction(codes.remove(0));
        while (!codes.isEmpty()){
            BinLogic l = BinLogic.build(codes.remove(0).get(0));
            ArrayList<String> args = codes.remove(0);
            if(args.size()>1){
                if(args.size()<5){
                    c = new LConduction(new BConduction(
                            PlaceHolder.build(args.get(0)),
                            Logic.build(args.get(1)),
                            PlaceHolder.build(args.get(2))
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
                            Mather.build(args1),
                            Logic.build(logic),
                            Mather.build(args2)
                    ), l, c);
                }
            } else c = new LConduction(new BConduction(args), l, c);
        }
        return c;
    }


    public abstract boolean checkFor(AbstractQuestSession session);
}


//class MConduction extends Conduction{
//    private final Mather mather1;
//    private final Mather mather2;
//
//
//    @Override
//    public boolean checkFor(QuestSession session) {
//        return false;
//    }
//}


class BConduction<T> extends Conduction{
    private final Logic<T> l;
    private final PlaceHolder<T> o1;
    private final PlaceHolder<T> o2;

    public BConduction(ArrayList<String> args){
        this(args.get(0), args.get(1), args.get(2));
    }


    public BConduction(String operand1, String logic, String operand2){
        this(PlaceHolder.build(operand1), Logic.build(logic), PlaceHolder.build(operand2));
    }


    public BConduction(PlaceHolder<T> operand1, Logic<T> logic, PlaceHolder<T> operand2){
        l = logic;
        o1 = operand1;
        o2 = operand2;
    }


    @Override
    public boolean checkFor(AbstractQuestSession session) {
        return l.checkFor(o1.getValue(session), o2.getValue(session));
    }
}


class LConduction<T> extends Conduction{
    private final BinLogic l;
    private final Conduction o1;
    private final Conduction o2;


    public LConduction(Conduction operand1, BinLogic logic, Conduction operand2){
        l = logic;
        o1 = operand1;
        o2 = operand2;
    }


    @Override
    public boolean checkFor(AbstractQuestSession session) {
        return l.checkFor(o1.checkFor(session), o2.checkFor(session));
    }
}