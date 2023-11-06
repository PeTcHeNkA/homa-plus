package plus.te4net.mds.html.vr;

import java.util.ArrayList;


class StyleParam implements ICitem {
    ArrayList<ICitem> value = new ArrayList<>();
    String key;

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