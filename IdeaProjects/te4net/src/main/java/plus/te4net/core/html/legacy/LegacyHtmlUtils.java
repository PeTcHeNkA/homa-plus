package plus.te4net.core.html.legacy;

import plus.te4net.core.html.JsFile;
import plus.te4net.core.node.HWorker;
import plus.te4net.utl.CharNum;
import plus.te4net.utl.IReplacer;
import plus.tson.utl.Tuple;

@Deprecated
public final class LegacyHtmlUtils {
    private String html;
    private int cursor = 0;
    private int type = 0;

    public LegacyHtmlUtils(String html){
        this.html = html;
    }

    public static String removeComments(String data, String stComm, String endComm, IReplacer replacer){
        int index = data.indexOf(stComm);
        while (index!=-1) {
            int index2 = data.indexOf(endComm, index);
            if(index2==-1)break;
            String toReplace = data.substring(index+stComm.length(), index2);
            String insert = replacer.replace(toReplace);
            if(insert.isEmpty()){
                data = data.substring(0,index)+data.substring(index2+endComm.length());
                index = data.indexOf(stComm, index);
            } else {
                data = data.substring(0,index)+insert+data.substring(index2+endComm.length());
                index = data.indexOf(stComm, index+insert.length());
            }
        }
        return data;
    }

    public static String replaceAll(String data, String[][] mathes){
        boolean flag = true;
        while (flag) {
            flag = false;
            for (String[] math : mathes) {
                Tuple<Boolean, String> data0 = replaceAll(data, math[0], math[1]);
                if(!flag){
                    flag = data0.A;
                }
                data = data0.B;
            }
        }
        return data;
    }

    public static Tuple<Boolean, String> replaceAll(String data, String from, String to){
        boolean flag = false;
        while (data.contains(from)){
            data = data.replace(from, to);
            flag = true;
        }
        return new Tuple<>(flag, data);
    }

    public static String optimizeHtml(HWorker worker, String html){
        VirtualCss vr = new VirtualCss(worker);
        html = removeComments(html, "<style>","</style>", vr);
        html = new LegacyHtmlUtils(
                removeComments(html, "<!--", "-->",(data1 -> ""))
        ).parse(LegacyHtmlUtils::compressHtml).getHtml();
        html = vr.applyCss(html);
        ReactiveHtml reactiveHtml = new ReactiveHtml(worker);
        html = removeComments(html, "<reactive", "</reactive>", reactiveHtml);
        html = removeComments(html, "<script", "</script>",
                data -> exportInnerJs(worker, data)
        );
        return html;
    }

    public static String compressHtml(String html){
        return replaceAll(html, new String[][]{
                {"  "," "},{" '","'"},{" =","="},
                {"> ",">"},{"' ","'"},{"= ","="},
                {" >",">"},{"\" ","\""},{"\t",""},
                {"< ", "<"},{" \"","\""},
                {" <", "<"},{"\r",""},
                {";>", ">"},{"\n",""}
        }).trim();
    }

    protected static String exportInnerJs(HWorker worker, String html){
        if(html.startsWith(" src"))return "<script src"+html.substring(4).trim()+"</script>";
        if(html.startsWith(" inc")){
            int index = html.indexOf(">") + 1;
            return "<script"+html.substring(0, index)+ JsFile.optimizeJs(worker, html.substring(index))+"</script>";
        }
        if(html.startsWith(" dev"))return "";
        if(html.startsWith(" async>")){
            return exportInnerJs(worker, html.substring(7),true);
        } else {
            return exportInnerJs(worker, html.substring(html.indexOf(">")+1),false);
        }
    }

    protected static String exportInnerJs(HWorker worker, String js, boolean async){
        return exportInnerPressedJs(worker, JsFile.optimizeJs(worker, js), async);
    }

    protected static String exportInnerPressedJs(HWorker worker, String js, boolean async){
        String name = getName("t.js", js);
        String link;
        if(async) {
            link = "<script src=\"/assets/js/" + name + "\" async></script>";
        } else {
            link = "<script src=\"/assets/js/" + name + "\"></script>";
            if (link.length() * 2 > js.length()) return "<script>" + js + "</script>";
        }
        if(js.length()*5>link.length()) {
            worker.getJsLib().put(name, new JsFile(worker, name, js, false));
            return link;
        } else {
            return "<script>"+js+"</script>";
        }
    }

    public static String getName(String suffix, String obj){
        int hs = obj.hashCode();
        if(hs<0)hs = -hs;
        return "_"+ CharNum.Default.getCharNumOf(hs)+"_"+suffix;
    }

    private int getNextIgnore() {
        int index = html.indexOf("<style", cursor);
        int index2 = html.indexOf("<script", cursor);
        if (index == index2) {
            type = 0;
            return html.length();
        }
        if (index < index2 && index != -1) {
            type = 1;
            return index;
        } else {
            type = 2;
            return index2;
        }
    }


    private void updateNextNoIgnore() {
        if (type == 1) {
            int index = html.indexOf("</style>", cursor);
            if (index != -1) {
                cursor = index + 8;
            }
        } else if (type == 2) {
            int index = html.indexOf("</script>", cursor);
            if (index != -1) {
                cursor = index + 9;
            }
        }
    }


    public LegacyHtmlUtils parse(IReplacer func) {
        int newCursor = getNextIgnore();
        StringBuilder builder = new StringBuilder();
        while (cursor < html.length()) {
            if(cursor!=newCursor) {
                String toReplace = html.substring(cursor, newCursor);
                String result = func.replace(toReplace);
                builder.append(result);
                cursor = newCursor;
            }
            updateNextNoIgnore();
            builder.append(html, newCursor, cursor);
            newCursor = getNextIgnore();
        }
        cursor = 0;
        type = 0;
        html = builder.toString();
        return this;
    }


    public String getHtml(){
        return html;
    }


    public void setHtml(String html){
        this.html = html;
    }
}