package plus.te4net.mds.html.vr;

import java.util.ArrayList;
import java.util.HashSet;


class Style extends AnStyle implements ICitem {
    final ArrayList<StyleName> names = new ArrayList<>();
    HashSet<String> immutable;

    public String fullName() {
        StringBuilder b = new StringBuilder();
        for (StyleName name : names) {
            name.toString(b);
            b.append(',');
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }


    @Override
    public void addParam(StyleParam param) {
        if(immutable==null){
            immutable = new HashSet<>();
            for (StyleParam name:items) immutable.add(name.key);
        }
        if(immutable.contains(param.key))return;
        super.addParam(param);
    }


    @Override
    public void toString(StringBuilder b) {
        if(items.isEmpty())return;
        int countNames = 0;
        for (StyleName name : names) {
            if(name.name != null) {
                name.toString(b);
                b.append(',');
                ++countNames;
            }
        }
        if(countNames==0)return;
        VrCss.last(b, '{');
        for (StyleParam param : items) {
            param.toString(b);
            b.append(';');
        }
        VrCss.last(b, '}');
    }


    boolean eqNames(ArrayList<StyleName> names){
        return this.names.equals(names);
    }


    boolean eqNames(Style etc) {
        return etc.names.equals(names);
    }


    public boolean isEmpty(){
        int countNames = 0;
        for (StyleName name : names) {
            if(name.name != null) {
                ++countNames;
            }
        }
        return countNames==0;
    }
}
