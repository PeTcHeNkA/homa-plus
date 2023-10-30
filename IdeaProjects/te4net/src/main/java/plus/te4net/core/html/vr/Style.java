package plus.te4net.core.html.vr;

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

    boolean eqNames(Style etc) {
        return etc.names.equals(names);
    }
}
