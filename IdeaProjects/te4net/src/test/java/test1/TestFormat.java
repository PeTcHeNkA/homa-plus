package test1;

import plus.te4net.core.api.PluginBase;
import plus.te4net.mds.util.TextMapFormatter;
import plus.tson.TsonMap;


public class TestFormat implements PluginBase {
    public static void main(String[] args) {
        new TestFormat();
    }


    public TestFormat(){
        TextMapFormatter fm = new TextMapFormatter(
                getStrSrc("mail-code-template.html"), "{%", "%}"
        );
        TsonMap map = new TsonMap();
        map.put("code", "__key______key__");


        System.out.println(fm.format(map));


    }
}
