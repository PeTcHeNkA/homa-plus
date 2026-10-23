package plus.te4net.mds.html;

import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.mds.html.events.VrHtmlEvent;
import plus.te4net.mds.html.vr.VrCss;
import plus.te4net.core.node.HEWorker;
import java.io.File;


@PublicUsage
public class CssFile extends SimpleCompressedTextFile {
    public CssFile(HEWorker worker, String name, String cssCode, boolean compress){
        super(worker, name, cssCode, compress);
    }


    public CssFile(HEWorker worker, String name, String cssCode){
        super(worker, TYPE_CSS, name, cssCode);
    }


    public CssFile(HEWorker worker, String path){
        super(worker, TYPE_CSS, "???", path);
    }


    public CssFile(HEWorker worker, File file){
        super(worker, TYPE_CSS, file);
    }


    public CssFile(IDataProvider worker, String name, String data) {
        super(worker, TYPE_CSS, name, data);
    }


    @Override
    public String replace(HEWorker worker, String data) {
        VrCss css;
        worker.throwEvent(new VrHtmlEvent(worker, VrHtmlEvent.Type.CSS, css = new VrCss(data)));
        return css.toString();
    }
}