package plus.te4net.core.html.vr;


class SField implements ICitem {
    protected String value;
    public SField(){}

    public SField(String v){
        this.value = v;
    }


    @Override
    public void toString(StringBuilder b) {
        b.append(value);
    }
}