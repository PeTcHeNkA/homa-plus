package plus.te4net.core.html.vr;
import java.util.ArrayList;


class HtmlObject implements ICitem {
    String name;
    ArrayList<VrHtml.Attribute> attributes;

    public ICitem getAttr(String key){
        for(VrHtml.Attribute a:attributes){
            if(a.key.equals(key))return a.getValue();
        }
        return null;
    }


    public String getStrAtr(String key){
        for(VrHtml.Attribute a:attributes){
            if(a.key.equals(key))return a.dicValue();
        }
        return null;
    }


    public void setStrAttribute(String key, String value){
        if(value==null){
            setAttribute(key, null);
        } else {
            setAttribute(key, new SField(value));
        }
    }


    public void setAttribute(String key, ICitem value){
        for(VrHtml.Attribute a:attributes){
            if(a.key.equals(key)){
                a.setValue(value);
                return;
            }
        }
        VrHtml.Attribute atr = new VrHtml.Attribute();
        atr.key = key;
        atr.setValue(value);
        attributes.add(atr);
    }


    public VrHtml.Attribute remAttribute(String key){
        int index = 0;
        for(VrHtml.Attribute a:attributes){
            if(a.key.equals(key)){
                break;
            }
            ++index;
        }
        if(index>=attributes.size()){
            return null;
        }
        return attributes.remove(index);
    }


    @Override
    public void toString(StringBuilder b) {
        b.append('<').append(name);
        if (!attributes.isEmpty()) {
            b.append(' ');
            for (VrHtml.Attribute attr : attributes) {
                attr.toString(b);
            }
        }
        b.append('>');
    }
}