package plus.te4net.core.event.events;

import plus.te4net.core.html.vr.VrCss;
import plus.te4net.core.html.vr.VrHtml;


public class VrHtmlEvent {
    private final Object obj;
    private final Type type;

    public VrHtmlEvent(Type type, Object obj) {
        this.obj = obj;
        this.type = type;
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


    public enum Type{
        HTML, CSS, JS
    }
}