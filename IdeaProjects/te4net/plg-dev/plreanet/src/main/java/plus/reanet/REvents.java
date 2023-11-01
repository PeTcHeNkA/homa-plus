package plus.reanet;

import plus.te4net.core.api.IEventHandler;
import plus.te4net.core.event.events.VrHtmlEvent;
import plus.te4net.core.html.vr.HtmlNode;
import plus.te4net.core.html.vr.ICitem;
import plus.te4net.core.html.vr.SField;
import plus.te4net.core.html.vr.VrHtml;
import plus.te4net.core.node.HWorker;
import plus.te4net.utl.CharNum;
import plus.tson.TsonInt;
import plus.tson.TsonMap;


public class REvents implements IEventHandler<VrHtmlEvent>{
    private final String cstr;

    public REvents(TsonMap map){
        cstr = "max-age="+map.getOrDefault("reanet-tm-cahce", new TsonInt(432000)).getStr();
    }

    @Override
    public boolean handle(VrHtmlEvent vrHtmlEvent) {
        if(vrHtmlEvent.type()== VrHtmlEvent.Type.HTML){
            return onHtml(vrHtmlEvent.getWorker(), vrHtmlEvent.getHtml());
        }
        return false;
    }


    public boolean onHtml(HWorker worker, VrHtml html){
        final boolean[] flag = {true};

        html.forItems("reactive", (htmlNode, htmlObject) -> {
            flag[0] = false;
            export(worker, (HtmlNode) htmlObject);
        });

        if(flag[0]) {
            flag[0] = false;
            html.forItems("script", (htmlNode, htmlObject) -> {
                if (flag[0]) return;
                ICitem src = htmlObject.getAttr("src");
                if (src == null) return;
                if (src.equals("./reanet-min.js")) {
                    flag[0] = true;
                }
            });
            if (!flag[0]) {
                html.forItems("head", (htmlNode, htmlObject) -> {
                    if (!flag[0]) return;
                    htmlNode.add(new HtmlNode("script", new String[]{"src", "./reanet-min.js"}));
                });
            }
        }

        return false;
    }


    private void export(HWorker worker, HtmlNode node){
        String str = node.toString();
        int hs = str.hashCode();
        String hs0 = hs>0?CharNum.Default.getCharNumOf(hs):"0"+CharNum.Default.getCharNumOf(-hs);

        String name = "_rea"+hs0;
        String url = "/rea/"+hs0+".html";

        node.clear();
        node.clearAttributes();
        node.setTag("script");
        node.setStrAttribute("id", name);
        SField script = new SField(
                "reanet.netload('" +name+"','"+url+"');reanet.placeTo('"+name+"','"+name+"')"
        );
        node.add(script);
        worker.addRoute(url, new ReaTemplate(worker, name, str, cstr));
    }
}