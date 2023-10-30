package plus.te4net.core.event.events;

import plus.te4net.core.html.vr.VrCss;
import plus.te4net.core.html.vr.VrHtml;
import plus.te4net.core.node.HWorker;


public class VrHtmlEvent {
    private final Object obj;
    private final Type type;
    private final HWorker worker;

    public VrHtmlEvent(HWorker worker, Type type, Object obj) {
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


    public HWorker getWorker() {
        return worker;
    }


    public enum Type{
        HTML, CSS, JS
    }
}