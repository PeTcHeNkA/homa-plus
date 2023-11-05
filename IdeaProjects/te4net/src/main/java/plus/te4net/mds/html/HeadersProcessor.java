package plus.te4net.mds.html;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PublicUsage;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.ArrayList;
import java.util.Locale;

@PublicUsage
public class HeadersProcessor implements IRequestProcessor {
    private final CharSequence[] keys;
    private final CharSequence[] values;

    HeadersProcessor(CharSequence[] keys, CharSequence[] values) {
        this.keys = keys;
        this.values = values;
    }

    public static HeadersProcessor build(TsonObj obj){
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        if(obj.isMap()){
            TsonMap map = obj.getMap();
            for(String key: map.keySet()){
                keys.add(key);
                values.add(map.getStr(key));
            }
        } else if (obj.isList()) {
            TsonList lst = obj.getList();
            for(TsonObj obl:lst){
                TsonList sublist = obl.getList();
                keys.add(sublist.getStr(0).toLowerCase(Locale.ROOT));
                values.add(sublist.getStr(1));
            }
        } else throw new RuntimeException("not supported ["+obj+"]");
        return new HeadersProcessor(
                keys.toArray(new String[0]),
                values.toArray(new String[0])
        );
    }


    @Override
    public void handle(IRequestSession session) {
        for(int i=0;i< keys.length;i++){
            session.setHeader(keys[i], values[i]);
        }
    }
}