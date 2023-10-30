package plus.te4net.core.html.vr;

import java.util.Objects;

class StyleName implements ICitem {
    String selector;
    String name;
    String params;
    String ender;

    @Override
    public void toString(StringBuilder b) {
        b.append(selector).append(name);
        if (params != null) {
            b.append('[').append(params).append(']');
        }
        if (ender != null) {
            b.append(':').append(ender);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StyleName sName = (StyleName) o;
        if (!selector.equals(sName.selector)) return false;
        if (!name.equals(sName.name)) return false;
        if (!Objects.equals(params, sName.params)) return false;
        return Objects.equals(ender, sName.ender);
    }


    @Override
    public int hashCode() {
        int result = selector.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (ender != null ? ender.hashCode() : 0);
        return result;
    }
}