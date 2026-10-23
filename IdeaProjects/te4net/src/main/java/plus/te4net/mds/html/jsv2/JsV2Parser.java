package plus.te4net.mds.html.jsv2;

import java.util.ArrayList;



public class JsV2Parser {


    abstract class JObject{
        private final int type;
        private String name;
        JObject(int type) {
            this.type = type;
        }

    }


    class Context{
        Context root;
        final ArrayList<JObject> objects = new ArrayList<>();
    }
}












