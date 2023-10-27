package plus.te4net.core.html;

import plus.te4net.core.bsys.IDataProvider;
import plus.te4net.core.html.vr.FastJsOptimizer;
import plus.te4net.core.node.HWorker;
import java.io.File;


public class JsFile extends SimpleCompressedTextFile {
    public JsFile(HWorker worker, String name, String code){
        super(worker, name, code);
    }


    public JsFile(HWorker worker, String name, String code, boolean needPress){
        super(worker, name, code, needPress);
    }


    public JsFile(HWorker worker, File file){
        super(worker, file);
    }


    public JsFile(IDataProvider worker, String name, String data) {
        super(worker, name, data);
    }


    @Override
    public String replace(HWorker worker, String data) {
        return optimizeJs(worker, data);
    }


    public static String optimizeJs(HWorker worker, String code){
        return new FastJsOptimizer(code).doScan();
    }


    @Override
    public String type() {
        return TYPE_JS;
    }
}