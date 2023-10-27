package plus.te4net.core.html;

import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.event.events.VrHtmlEvent;
import plus.te4net.core.html.vr.VrHtml;
import plus.te4net.core.node.HWorker;
import java.io.File;


@PublicUsage
public class StaticHtmlFile extends SimpleCompressedTextFile {
    public StaticHtmlFile(HWorker worker, File file){
        super(worker, file);
    }


    public StaticHtmlFile(HWorker worker, String name, String data, boolean compress){
        super(worker, name, data, compress);
    }


    @Override
    public String replace(HWorker worker, String data) {
        return optimizeHtml(worker, data);
        //return LegacyHtmlUtils.optimizeHtml(worker, data);
    }


    public static String optimizeHtml(HWorker worker, String data){
        VrHtml html = new VrHtml(data);
        worker.throwEvent(new VrHtmlEvent(VrHtmlEvent.Type.HTML, html));
        return html.toString();
    }


    @Override
    public String type() {
        return "text/html;charset=utf-8";
    }
}