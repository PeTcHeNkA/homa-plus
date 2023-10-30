package plus.te4net.core.html.vr;

import plus.te4net.core.html.vr.ICitem;


class StyleParam implements ICitem {
    String key;
    ICitem value;

    @Override
    public void toString(StringBuilder b) {
        b.append(key).append(':');
        value.toString(b);
    }
}