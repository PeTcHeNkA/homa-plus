package plus.te4net.mds.html.vr;

import java.util.ArrayList;


class Style implements ICitem {
    final ArrayList<StyleName> names = new ArrayList<>();
    final ArrayList<StyleParam> items = new ArrayList<>();

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
    public void toString(StringBuilder b) {
        if(items.isEmpty())return;
        for (StyleName name : names) {
            name.toString(b);
            b.append(',');
        }
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
}
