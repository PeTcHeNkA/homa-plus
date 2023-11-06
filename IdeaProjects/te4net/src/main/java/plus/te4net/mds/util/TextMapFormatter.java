package plus.te4net.mds.util;

import plus.te4net.core.api.PublicUsage;
import plus.tson.TsonMap;
import plus.tson.TsonStr;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;


@PublicUsage
public class TextMapFormatter extends TextFormatter<IVarGetter<String>> {
    public TextMapFormatter(String format) {
        this(format, "<%","%>");
    }


    public TextMapFormatter(String format, String st, String end) {
        super(compileFormat(format, name -> getter -> getter.get(name), st, end));
    }


    public String format(TsonMap map){
        return format(s -> map.getOrDefault(s,new TsonStr(s)).getStr());
    }
}