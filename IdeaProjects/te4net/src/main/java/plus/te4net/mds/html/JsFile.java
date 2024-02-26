package plus.te4net.mds.html;

import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.mds.html.vr.FastJsOptimizer;
import plus.te4net.core.node.HEWorker;
import java.io.File;


@PublicUsage
public class JsFile extends SimpleCompressedTextFile {
    public JsFile(HEWorker worker, String name, String code){
        super(worker, name, code);
    }


    public JsFile(HEWorker worker, String name, String code, boolean needPress){
        super(worker, name, code, needPress, TYPE_JS);
    }


    public JsFile(HEWorker worker, File file){
        super(worker, TYPE_JS, file);
    }


    public JsFile(IDataProvider worker, String name, String data) {
        super(worker, TYPE_JS, name, data);
    }


    @Override
    public String replace(HEWorker worker, String data) {
        return optimizeJs(worker, data);
    }


    public static String optimizeJs(HEWorker worker, String code){
        return new FastJsOptimizer(code).doScan();
    }
}