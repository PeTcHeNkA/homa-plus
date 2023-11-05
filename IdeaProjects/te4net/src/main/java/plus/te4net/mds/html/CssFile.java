package plus.te4net.mds.html;

import plus.te4net.core.api.IDataProvider;
import plus.te4net.mds.html.events.VrHtmlEvent;
import plus.te4net.mds.html.vr.VrCss;
import plus.te4net.core.node.HEWorker;

import java.io.File;


public class CssFile extends SimpleCompressedTextFile {
    public CssFile(HEWorker worker, String name, String cssCode, boolean compress){
        super(worker, name, cssCode, compress);
    }


    public CssFile(HEWorker worker, String name, String cssCode){
        super(worker, name, cssCode);
    }


    public CssFile(HEWorker worker, String path){
        super(worker, "???", path);
    }


    public CssFile(HEWorker worker, File file){
        super(worker, file);
    }


    public CssFile(IDataProvider worker, String name, String data) {
        super(worker, name, data);
    }


    @Override
    public String replace(HEWorker worker, String data) {
        VrCss css = new VrCss(data);
        worker.throwEvent(new VrHtmlEvent(worker, VrHtmlEvent.Type.CSS, css));
        return css.toString();
    }


    @Override
    public String type() {
        return TYPE_CSS;
    }
}