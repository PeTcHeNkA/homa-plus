package plus.te4net.mds.html.vr;

import java.util.ArrayList;


class AnStyle implements ICitem{
    final ArrayList<StyleParam> items = new ArrayList<>();

    @Override
    public void toString(StringBuilder b) {
        for (StyleParam param : items) {
            param.toString(b);
            b.append(';');
        }
        b.setLength(b.length()-1);
    }


    public void addParam(StyleParam param){
        boolean add = true;
        for (StyleParam p:items){
            if(p.key.equals(param.key)){
                p.value = param.value;
                add = false;
                break;
            }
        }
        if(add){
            items.add(param);
        }
    }


    public final int size(){
        return items.size();
    }


    public boolean isEmpty(){
        return size()==0;
    }
}