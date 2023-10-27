package plus.te4net.core.html;

import plus.te4net.core.bsys.IDataProvider;
import plus.te4net.core.event.events.VrHtmlEvent;
import plus.te4net.core.html.vr.VrCss;
import plus.te4net.core.node.HWorker;
import java.io.File;


public class CssFile extends SimpleCompressedTextFile {
    public CssFile(HWorker worker, String name, String cssCode, boolean compress){
        super(worker, name, cssCode, compress);
    }


    public CssFile(HWorker worker, String name, String cssCode){
        super(worker, name, cssCode);
    }


    public CssFile(HWorker worker, String path){
        super(worker, "???", path);
    }


    public CssFile(HWorker worker, File file){
        super(worker, file);
    }


    public CssFile(IDataProvider worker, String name, String data) {
        super(worker, name, data);
    }


    @Override
    public String replace(HWorker worker, String data) {
        VrCss css = new VrCss(data);
        worker.throwEvent(new VrHtmlEvent(VrHtmlEvent.Type.CSS, css));
        return css.toString();
    }


    @Override
    public String type() {
        return TYPE_CSS;
    }
}