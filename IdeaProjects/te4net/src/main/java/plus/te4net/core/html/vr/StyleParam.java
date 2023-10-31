package plus.te4net.core.html.vr;

import java.util.ArrayList;


class StyleParam implements ICitem {
    String key;
    ArrayList<ICitem> value = new ArrayList<>();

    @Override
    public void toString(StringBuilder b) {
        b.append(key).append(':');
        boolean flag = false;
        for (ICitem item:value){
            if(flag){
                b.append(' ');
            }
            item.toString(b);
            flag = true;
        }
    }
}