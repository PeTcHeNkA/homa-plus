package plus.te4net.mds.html.vr;

import java.util.ArrayList;
import java.util.Objects;


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


    public void renameVar(String from, String keyTo){
        renameVar(from, keyTo, keyTo);
    }


    public void renameVar(String from, String keyTo, String varTo){
        for (StyleParam param:items){
            if(param.key.equals(from)) param.key = keyTo;

            for (ICitem iCitem:param.value){
                renVarRec(iCitem, from, varTo);
            }
        }
    }


    private void renVarRec(ICitem item, String from, String to){
        if(item instanceof VrCss.Var vrcssvar){
            if(Objects.equals(from, vrcssvar.header)) vrcssvar.header = to;

            if(vrcssvar.items==null)return;
            for (ArrayList<ICitem> item0: vrcssvar.items){
                for (ICitem iCitem1:item0){
                    renVarRec(iCitem1, from, to);
                }
            }
        } else if (item instanceof SField val) {
            if(val.value.equals(from)){
                val.value = to;
            }
        }
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
        if(add) items.add(param);

    }


    public StyleParam getParam(String key){
        for (StyleParam p:items)
            if(p.key.equals(key)) return p;
        return null;
    }


    public final int size(){
        return items.size();
    }


    public boolean isEmpty(){
        return size()==0;
    }


    @Override
    public String toString() {
        if(isEmpty())return "";
        StringBuilder b = new StringBuilder();
        toString(b);
        return b.toString();
    }
}