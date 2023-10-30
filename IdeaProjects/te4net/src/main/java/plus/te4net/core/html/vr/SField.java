package plus.te4net.core.html.vr;


public class SField implements ICitem {
    public String value;
    public SField(){}

    public SField(String v){
        this.value = v;
    }


    @Override
    public void toString(StringBuilder b) {
        b.append(value);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof SField){
            SField sField = (SField) o;
            return value.equals(sField.value);
        } else if(o instanceof String){
            return o.equals(value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}