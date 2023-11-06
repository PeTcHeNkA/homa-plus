package plus.te4net.mds.html.events;

import plus.te4net.mds.html.vr.VrCss;
import plus.te4net.mds.html.vr.VrHtml;
import plus.te4net.core.node.HEWorker;


public class VrHtmlEvent {
    public enum Type{
        HTML, CSS, JS
    }

    private final Object obj;
    private final Type type;
    private final HEWorker worker;

    public VrHtmlEvent(HEWorker worker, Type type, Object obj) {
        this.obj = obj;
        this.type = type;
        this.worker = worker;
    }


    public VrHtml getHtml(){
        return (VrHtml) obj;
    }


    public VrCss getCss(){
        return (VrCss) obj;
    }


    public String getJs(){
        return (String) obj;
    }


    public Type type(){
        return type;
    }


    public HEWorker getWorker() {
        return worker;
    }
}