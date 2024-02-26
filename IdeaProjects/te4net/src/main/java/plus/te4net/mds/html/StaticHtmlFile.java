package plus.te4net.mds.html;

import plus.te4net.core.api.PublicUsage;
import plus.te4net.mds.html.events.VrHtmlEvent;
import plus.te4net.mds.html.vr.VrHtml;
import plus.te4net.core.node.HEWorker;
import java.io.File;


@PublicUsage
public class StaticHtmlFile extends SimpleCompressedTextFile {
    public StaticHtmlFile(HEWorker worker, File file){
        super(worker, TYPE_HTML, file);
    }


    public StaticHtmlFile(HEWorker worker, String name, String data, boolean compress){
        super(worker, name, data, compress, TYPE_HTML);
    }


    @Override
    public String replace(HEWorker worker, String data) {
        return optimizeHtml(worker, data);
    }


    public static String optimizeHtml(HEWorker worker, String data){
        VrHtml html = new VrHtml(data);
        worker.throwEvent(new VrHtmlEvent(worker, VrHtmlEvent.Type.HTML, html));
        return html.toString();
    }
}