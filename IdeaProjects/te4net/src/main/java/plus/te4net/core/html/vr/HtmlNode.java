package plus.te4net.core.html.vr;

import java.util.ArrayList;


public class HtmlNode extends HtmlObject {
    protected final ArrayList<ICitem> items = new ArrayList<>();

    public HtmlNode(){}

    public HtmlNode(String name, ArrayList<VrHtml.Attribute> attributes){
        super(name, attributes);
    }

    public HtmlNode(String name, String[]... attributes){
        super(name, attributes);
    }

    public ArrayList<HtmlObject> getBy(String tag){
        ArrayList<HtmlObject> list = new ArrayList<>();
        getBy(list, tag);
        return list;
    }


    public interface Proc{
        void apply(HtmlNode parent, int index, HtmlObject cur);
    }


    public void forItems(Proc proc){
        forItems(null, proc);
    }

    public void forItems(String tag, Proc proc){
        int i = 0;
        for (ICitem obj : new ArrayList<>(items)) {
            if(obj instanceof HtmlObject obj0){
                if(tag==null || obj0.name.equals(tag)){
                    proc.apply(this, i, obj0);
                }
                if(obj instanceof HtmlNode){
                    ((HtmlNode) obj).forItems(tag, proc);
                }
            }
            ++i;
        }
    }


    void getBy(ArrayList<HtmlObject>list, String tag){
        for (ICitem obj : items) {
            if(obj instanceof HtmlObject obj0){
                if(obj0.name.equals(tag)){
                    list.add(obj0);
                }
                if(obj instanceof HtmlNode){
                    ((HtmlNode) obj).getBy(list, tag);
                }
            }
        }
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }


    public String innerHtml(){
        StringBuilder b = new StringBuilder();
        innerHtml(b);
        return b.toString();
    }


    public void innerHtml(StringBuilder b){
        for (ICitem obj : items) {
            obj.toString(b);
        }
    }


    @Override
    public void toString(StringBuilder b) {
        super.toString(b);
        innerHtml(b);
        b.append("</").append(name).append('>');
    }


    @Override
    public void toString(StringBuilder b, ICitem parent, IToStrAgent agent) {
        if(agent.onAdd(b, parent, this)){
            super.toString(b);
            for (ICitem obj : items) {
                obj.toString(b, this, agent);
            }
            b.append("</").append(name).append('>');
        }
    }


    public boolean remove(ICitem obj){
        return items.remove(obj);
    }


    public ICitem get(int i){
        return items.get(i);
    }


    public void add(ICitem obj){
        items.add(obj);
    }


    public ICitem remove(int i){
        return items.remove(i);
    }


    public void clear(){
        items.clear();
    }
}