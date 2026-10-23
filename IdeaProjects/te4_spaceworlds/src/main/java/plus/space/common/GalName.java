package plus.space.common;


import java.util.Objects;

public class GalName {
    private final String selfName;
    private final GalName parent;

    public static GalName ofStr(String name){
        String[] items = name.split(";");
        int cursor = 0;
        GalName galName = null;
        do{
            galName = new GalName(items[cursor], galName);
            ++cursor;
        } while (cursor != items.length);
        return galName;
    }

    public GalName(String selfName, GalName parent) {
        this.selfName = selfName;
        this.parent = parent;
    }


    public String getSelfName() {
        return selfName;
    }


    public GalName getParent() {
        return parent;
    }


    @Override
    public String toString() {
        if(parent==null)return selfName;
        return parent+";"+selfName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalName galName = (GalName) o;
        if (!selfName.equals(galName.selfName)) return false;
        return Objects.equals(parent, galName.parent);
    }


    @Override
    public int hashCode() {
        int result = selfName.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
}