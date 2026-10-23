package plus.reanet;

import plus.te4net.core.api.IEventHandler;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.html.SimpleCompressedTextFile;
import plus.te4net.mds.html.events.VrHtmlEvent;
import plus.te4net.mds.html.vr.*;
import plus.te4net.mds.util.CharNum;
import plus.tson.TsonInt;
import plus.tson.TsonMap;


public class REvents implements IEventHandler<VrHtmlEvent> {
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


    public boolean onHtml(HEWorker worker, VrHtml html){
        //if(1==1)return false;
        final boolean[] flag = {true};
        TemplateExporter exporter = new TemplateExporter(worker);
        CSSExporter cssExporter = new CSSExporter(worker, html);

//        html.forItems("reactive", (htmlNode, htmlObject) -> {
//            flag[0] = false;
//            export(worker, (HtmlNode) htmlObject);
//        });

        html.forItems("template", exporter);
        if(cssExporter.mayApply()) {
            html.forItems("style", cssExporter);
            cssExporter.complete();
        }

        if(exporter.script != null){
            flag[0] = false;
        }

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


    private class CSSExporter implements HtmlNode.Proxy{
        private final HEWorker worker;
        private final VrHtml html;
        private final StringBuilder cssBundle = new StringBuilder();
        private HtmlNode head;

        public CSSExporter(HEWorker worker, VrHtml html){
            this.worker = worker;
            this.html = html;

            html.forItems("head", (parent, item) -> head = (HtmlNode) item);
        }


        private boolean mayApply(){
            return head != null;
        }


        @Override
        public void apply(HtmlNode.Item item) {
            HtmlNode node = (HtmlNode) item.getObj();
            node.innerHtml(cssBundle);
            item.remove();
        }


        public void complete(){

            String code = cssBundle.toString();
            String name = "dcss-"+strHash(code)+".css";

            String url  = "/rea/"+name;

            HtmlObject link = new HtmlNode("link");
            link.setStrAttribute("rel", "stylesheet");
            link.setStrAttribute("href", url);

            head.add(link);

            worker.addRoute(url, new ReaTemplate(worker, SimpleCompressedTextFile.TYPE_CSS, name, code, cstr));
        }
    }


    private class TemplateExporter implements HtmlNode.Proc{
        private final HEWorker worker;
        private SField script;

        private TemplateExporter(HEWorker worker) {
            this.worker = worker;
        }


        @Override
        public void apply(HtmlNode parent, HtmlObject obj) {
            HtmlNode node = (HtmlNode) obj;
            ICitem attr = node.getAttr("name");
            if(attr == null)return;

            String templateName = attr.toString();

            VrHtml.uptJs(node, worker);
            String templateData = node.innerHtml();


            String hs0 = strHash(templateData);
            String url = "/rea/"+templateName+"-"+hs0+".html";

            node.clear();
            node.setTag("script");

            String genJs = "reanet.netload('" + templateName + "','" + url + "');";
            if(script==null) {
                script = new SField(genJs);
                node.add(script);
            } else {
                script.value += genJs;
            }
            node.remAttribute("name");

            worker.addRoute(url, new ReaTemplate(worker, templateName, templateData, cstr));
        }
    }


    private String strHash(String obj){
        int hs = obj.hashCode();
        return hs>0? CharNum.Default.getCharNumOf(hs):"0"+CharNum.Default.getCharNumOf(-hs);
    }


    private void export(HEWorker worker, HtmlNode node){
        String str = node.toString();
        int hs = str.hashCode();
        String hs0 = hs>0? CharNum.Default.getCharNumOf(hs):"0"+CharNum.Default.getCharNumOf(-hs);

        String name = "_rea"+hs0;
        String url = "/rea/"+hs0+".html";

        node.clear();
        node.clearAttributes();
        node.setTag("script");
        node.setStrAttribute("id", name);
        SField script = new SField(
                "reanet.netload('" +name+"','"+url+"');reanet.placeTo('"+name+"','"+name+"');"
        );
        node.add(script);
        worker.addRoute(url, new ReaTemplate(worker, name, str, cstr));
    }
}