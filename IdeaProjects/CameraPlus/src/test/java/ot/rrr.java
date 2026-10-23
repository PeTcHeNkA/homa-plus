package ot;

import java.util.ArrayList;
import java.util.List;

public class rrr {

    public static void main(String args[]){
        try {
            main();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(){
        List<String> list = new ArrayList<>();

        list.add("&1test&4test2");
        list.add("@Stest1");
        list.add("@Hhower? &6@lcolorful!");
        //list.add("@C/test!");

        t2 msg = new t2(list);

        System.out.println(msg);
        //String Text = "&6&lColorful! &4RED";

//        ot.TextComp c = new ot.TextComp("&6&lColor: &4RED");
//        c.setHoverEvent("&7text!");
//        System.out.println(c.toString());






        //System.out.println(ot.t2.parseColorToJson("&6colorful!"));

    }
}
