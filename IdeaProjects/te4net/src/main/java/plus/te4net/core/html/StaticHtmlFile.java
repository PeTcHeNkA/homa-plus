package plus.te4net.core.html;

import plus.te4net.core.node.HWorker;
import plus.te4net.utl.CharNum;
import plus.te4net.utl.IReplacer;
import plus.tson.TsonFile;
import plus.tson.utl.Tuple;
import java.io.File;


public class StaticHtmlFile extends SimpleCompressedTextFile {
    public StaticHtmlFile(HWorker worker, File file){
        super(worker, file.getName(),TsonFile.read(file,""));
    }


    public StaticHtmlFile(HWorker worker, String name, String data, boolean compress){
        super(worker, name, data, compress);
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


    @Override
    public String replace(HWorker worker, String data) {
        return optimizeHtml(worker, data);
    }


    public static String optimizeHtml(HWorker worker, String html){
        VirtualCss vr = new VirtualCss(worker);
        html = removeComments(html, "<style>","</style>", vr);
        html = new OnlyHtmlReplacer(
                removeComments(html, "<!--", "-->",(data1 -> ""))
        ).parse(StaticHtmlFile::compressHtml).getHtml();
        html = vr.applyCss(html);
        ReactiveHtml reactiveHtml = new ReactiveHtml(worker);
        html = removeComments(html, "<reactive", "</reactive>", reactiveHtml);
        html = removeComments(html, "<script", "</script>",
                //StaticHtmlFile::exportInnerJs
                data -> exportInnerJs(worker, data)
        );
        if(!html.startsWith("<!DOCTYPE html>"))html = "<!DOCTYPE html>"+html;
        if(html.contains("<html>"))html = html.replace("<html>","<html lang=\"ru\">");
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
        if(html.startsWith(" src"))return "<script"+html+"</script>";
        if(html.startsWith(" dev"))return "";
        if(html.startsWith(" async>")){
            return exportInnerJs(worker, html.substring(7),true);
        } else {
            return exportInnerJs(worker, html.substring(html.indexOf(">")+1),false);
        }
    }
//
//
//    protected static String compressInnerJs(String js){
//        if(js.startsWith(" src"))return "<script"+js+"</script>";
//        return "<script>"+JsFile.optimizeJs(js.substring(js.indexOf(">")+1))+"</script>";
//    }


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
        if(js.length()*3>link.length()) {
            worker.getJsLib().put(name, new JsFile(worker, name, js, false));
            return link;
        } else {
            return "<script>"+js+"</script>";
        }
    }


    protected static String getName(String suffix, String obj){
        int hs = obj.hashCode();
        if(hs<0)hs = -hs;
        return "_"+CharNum.Default.getCharNumOf(hs)+"_"+suffix;
    }


    @Override
    public String type() {
        return "text/html;charset=utf-8";
    }
}