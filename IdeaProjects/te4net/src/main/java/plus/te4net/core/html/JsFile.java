package plus.te4net.core.html;

import plus.te4net.core.node.HWorker;
import plus.tson.TsonFile;
import java.io.*;


public class JsFile extends SimpleCompressedTextFile {
    public JsFile(HWorker worker, String name, String code){
        super(worker, name, code);
    }


    public JsFile(HWorker worker, String name, String code, boolean needPress){
        super(worker, name, code, needPress);
    }


    public JsFile(HWorker worker, File file){
        super(worker, file.getName(),TsonFile.read(file,""));
    }


    @Override
    public String replace(HWorker worker, String data) {
        return optimizeJs(worker, data);
    }



    public static String optimizeJs(HWorker worker, String code){
        //code = code.replace("%domain%", worker.getDomain());
//        if(Main.DEBUG)return code;
        return StaticHtmlFile.replaceAll(StaticHtmlFile.removeComments(code.trim(), "/*", "*/",(data1 -> "")), new String[][]{
                {"  "," "},{" '","'"},{" =","="},{"\t",""},
                {"' ","'"},{"= ","="},{";;",";"},{"{;","{"},
                {" }","}"},{"+ ","+"},{"- ","-"},{" ;",";"},
                {"} ","}"},{" +","+"},{" -","-"},{"; ",";"},
                {" (", "("},{"\r",""},{", ",","},{". ","."},
                {"( ", "("},{" )",")"},{" ,",","},{" .","."},
                {") ",")"},{" :",":"},{": ",":"},{"\n"," "},
                {";}","}"},{"{ ","{"},{" {","{"},{"true","1"},{"false","0"}
        }).trim();
    }


    @Override
    public String type() {
        return "text/javascript";
    }
}