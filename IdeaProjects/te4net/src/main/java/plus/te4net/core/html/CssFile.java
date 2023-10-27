package plus.te4net.core.html;

import plus.te4net.core.bsys.IDataProvider;
import plus.te4net.core.node.HWorker;
import plus.te4net.utl.CharNum;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CssFile extends SimpleCompressedTextFile {
    public CssFile(HWorker worker, String name, String cssCode, boolean compress){
        super(worker, name, cssCode, compress);
    }


    public CssFile(HWorker worker, String name, String cssCode){
        super(worker, name, cssCode);
    }


    public CssFile(HWorker worker, String path){
        super(worker, "???", path);
    }


    public CssFile(HWorker worker, File file){
        super(worker, file);
    }


    public CssFile(IDataProvider worker, String name, String data) {
        super(worker, name, data);
    }


    @Override
    public String replace(HWorker worker, String data) {
        return optimizeCss(data);
    }


    public static String optimizeCss(String cssCode){
        return StaticHtmlFile.replaceAll(StaticHtmlFile.removeComments(cssCode, "/*","*/",(data1 -> "")), new String[][]{
                {"  "," "},//{" (","("},
                {": ",":"},{"( ","("},
                {"; ",";"},{") ",")"},
                {", ", ","},{" )",")"},
                {" ,", ","},{"\r",""},
                {"{ ", "{"},{"\n",""},
                {" {", "{"},{"\t",""},
                {" }","}"},
                {";}","}"},
                {"} ","}"}
        }).trim();
    }


    public static String optimizeVars(String cssCode, AtomicInteger usages){
        Pattern pattern = Pattern.compile("--.*?:");
        Matcher matcher = pattern.matcher(cssCode);

        for(Object obj:matcher.results().toArray()){
            MatchResult m = (MatchResult) obj;
            String cssVar = m.group();
            cssVar = cssVar.substring(0, cssVar.length()-1);
            if(cssVar.startsWith("---"))continue;
            String newVar = "--"+CharNum.OnlyChars.getCharNumOf(usages.addAndGet(1));
            cssCode = cssCode.replace(cssVar+":",newVar+":");
            cssCode = cssCode.replace("var("+cssVar+")","var("+newVar+")");
        }
        return cssCode;
    }


    @Override
    public String type() {
        return TYPE_CSS;
    }
}