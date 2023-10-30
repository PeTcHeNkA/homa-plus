package plus.te4net.core.html.vr;
import java.util.ArrayList;


public class HtmlObject implements ICitem {
    String name;
    ArrayList<VrHtml.Attribute> attributes;

    public HtmlObject(){}

    public HtmlObject(String name, ArrayList<VrHtml.Attribute> attributes){
        this.name = name;
        this.attributes = attributes;
    }

    public HtmlObject(String name, String[]... attributes){
        this.name = name;
        this.attributes = new ArrayList<>();
        for (String[] s:attributes){
            this.attributes.add(new VrHtml.Attribute(s[0], new SField(s[1])));
        }
    }


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


    public void clearAttributes(){
        attributes.clear();
    }


    public void setTag(String name){
        this.name = name;
    }


    public ArrayList<VrHtml.Attribute> getAttributes(){
        return new ArrayList<>(attributes);
    }


    @Override
    public void toString(StringBuilder b) {
        b.append('<').append(name);
        if (attributes != null && !attributes.isEmpty()) {
            b.append(' ');
            for (VrHtml.Attribute attr : attributes) {
                attr.toString(b);
            }
        }
        b.append('>');
    }
}