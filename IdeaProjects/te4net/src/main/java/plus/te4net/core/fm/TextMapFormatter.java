package plus.te4net.core.fm;

import plus.te4net.core.node.HWorker;
import plus.tson.format.ConstVarGetter;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;


public class TextMapFormatter extends TextFormatter<IVarGetter<String>> {
    public TextMapFormatter(HWorker worker, String format) {
        this(worker, format, "<%","%>");
    }
    public TextMapFormatter(HWorker worker, String format, String st, String end) {
        super(compileFormat(format, name -> {
            IVarGetter result = worker.getFmGetter(name);
            if (result != null && result instanceof ConstVarGetter){
                return stringIVarGetter -> result.get(null);
            }
            return getter -> getter.get(name);
        }, st, end));
    }
}