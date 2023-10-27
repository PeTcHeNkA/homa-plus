package plus.te4net.core.html.legacy;

import plus.te4net.core.html.JsFile;
import plus.te4net.core.html.StaticHtmlFile;
import plus.te4net.core.node.HWorker;
import plus.te4net.core.fm.TextMapFormatter;
import plus.te4net.utl.CharNum;
import plus.te4net.utl.IReplacer;
import plus.tson.utl.Tuple;
import plus.tson.utl.Te4HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;


public final class ReactiveHtml extends Te4HashMap<ReactiveHtml.RKeyStr, String> implements IReplacer {
    private AtomicBoolean currNewKey = new AtomicBoolean(true);
    private AtomicBoolean curHasUsg = new AtomicBoolean(true);
    private boolean async;
    private final HWorker worker;

    private final HashSet<String> usedScripts = new HashSet<>();

    public ReactiveHtml(HWorker worker) {
        this.worker = worker;
    }

    static final class RKeyStr {
        private final String val;
        private final AtomicBoolean isNew;
        private AtomicBoolean hasUsages;

        RKeyStr(String val, AtomicBoolean isNew) {
            this.val = val;
            this.isNew = isNew;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof String) return val.equals(o);
            return val.equals(((RKeyStr) o).val);
        }

        @Override
        public int hashCode() {
            return val.hashCode();
        }
    }

    private String name = null;
    private String onLoad = "";
    @Override
    public String replace(String html) {
        name = null;
        async = false;
        Tuple<String, ArrayList<Tuple<String, String>>> result = prepare(html);
        String args0 = null;
        for(Tuple<String,String> args:result.B){
            switch (args.A) {
                case "async" -> async = true;
                case "name" -> name = args.B;
                case "draw" -> args0 = args.B;
                case "onload" -> onLoad = args.B;
            }
        }
        if(result.A.isBlank()){
            if(name==null) {
                return "";
            } else {
                return namedReactive(args0);
            }
        }
        if(name!=null)async = false;
        return reactivity(result.A);
    }


    private String namedReactive(String args){
        String scriptName = "rea_0"+name;
        String selector = getRSelector();
        if(usedScripts.add(scriptName)) {
            return "<div id=\"" + selector + "\"><script src=\"/assets/js/"
                    + scriptName + "\"" + (async ? " async" : "") + "></script><script>" + joinNames(args)
                    + (scriptName + "(\"" + selector + "\")") + (onLoad==null?"":";"+onLoad) + "</script></div>";
        } else {
            return "<div id=\"" + selector + "\"><script>" + joinNames(args)
                    + (scriptName + "(\"" + selector + "\")") + (onLoad==null?"":";"+onLoad) + "</script></div>";
        }
    }


    private String joinNames(String args){
        if(args==null)return "";
        final String[] buffer = {""};
        new TextMapFormatter(worker, args).get(key -> {
            RKeyStr keyStr = new RKeyStr(key, currNewKey);
            if(!ReactiveHtml.this.containsKey(keyStr)){
                String valRaw = getVar(keyStr);
                String val = valRaw.substring(2, valRaw.length() - 1);
                buffer[0] += ("const "+val+"=%"+key+"%;");
            }
            return null;
        });
        return buffer[0];
    }


    final class ScriptAgent implements IReplacer {
        private String htmlCode = "";
        private String jsCode = "";
        private String preJsCode = "";

        @Override
        public String replace(String html) {
            if (html.startsWith(" src")) {
                String result = "<script" + html + "</script>";
                if(name==null){
                    htmlCode += result;
                    return "";
                } else {
                    return result;
                }
            } else {
                if(html.startsWith(" dev"))return "";
                String js;
                boolean async;
                if (html.startsWith(" async>")) {
                    async = true;
                    js = html.substring(7);
                } else {
                    js = html.substring(html.indexOf(">") + 1);
                    async = false;
                }
                js = JsFile.optimizeJs(worker, js);
                if (async) {
                    preJsCode = "(async()=>{" + js + "})();" + preJsCode;
                } else {
                    jsCode += (";" + js);
                }
                return "";
            }
        }


        @Override
        public int hashCode() {
            return 31 * (31 * htmlCode.hashCode() + jsCode.hashCode()) + preJsCode.hashCode();
        }
    }

    private String getRSelector(){
        return "sel_"+CharNum.Default.getCharNumOf(ThreadLocalRandom.current().nextInt(1,Integer.MAX_VALUE));
    }

    private String reactivity(String template) {
        ScriptAgent jsAgent = new ScriptAgent();
        template = StaticHtmlFile.removeComments(template, "<script", "</script>", jsAgent);

        String scriptName;
        String selector;
        template = template.replace("`", "\\`");
        if(name==null) {
            int hs = 31 * template.hashCode() + jsAgent.hashCode();
            if (hs < 0) hs = -hs;
            scriptName = "_" + CharNum.Default.getCharNumOf(hs) + "_r";
            selector = scriptName;

            template = "document.getElementById(\"" + selector + "\").innerHTML=`" + template + "`";

        } else {
            scriptName = "rea_0"+name;
            selector = getRSelector();
            template = "function "+scriptName+"(__sel){document.getElementById(__sel).innerHTML=`" + template + "`}";
        }


        template = new TextMapFormatter(worker, template).get(this::getVar);
        StringBuilder mtdArgs = new StringBuilder();
        StringBuilder htmlCall = new StringBuilder();
        boolean isFirst = true;

        for (RKeyStr key : this.keySet()) {
            if (!key.hasUsages.get()) continue;
            String valRaw = this.get(key);
            String val = valRaw.substring(2, valRaw.length() - 1);
            if (async || name != null) {
                if (key.isNew.get()) {
                    if (isFirst) {
                        isFirst = false;
                    }
                    htmlCall.append("const ").append(val).append("=").append("`%").append(key.val).append("%`;");
                }
            } else {
                if (isFirst) {
                    isFirst = false;
                    mtdArgs.append(val);
                    htmlCall.append("`%").append(key.val).append("%`");
                } else {
                    mtdArgs.append(',').append(val);
                    htmlCall.append(",`%").append(key.val).append("%`");
                }
            }
        }

        String strHtmlCall;

        if (isFirst) {
            if(name==null) {
                if(onLoad==null || onLoad.isBlank()) {
                    strHtmlCall = "<div id=\"" + selector + "\">" + jsAgent.htmlCode + "<script src=\"/assets/js/"
                            + scriptName + ".js\""+(async?" async":"")+"></script></div>";
                } else {
                    strHtmlCall = "<div id=\"" + selector + "\">" + jsAgent.htmlCode + "<script src=\"/assets/js/"
                            + scriptName + ".js\""+(async?" async":"")+"></script><script>"+onLoad+"</script></div>";
                }
            } else {
                if(usedScripts.add(scriptName)) {
                    strHtmlCall = "<div id=\"" + selector + "\"><script src=\"/assets/js/"
                            + scriptName + ".js\"></script><script>"
                            + scriptName + "(\"" + selector + "\")" + (onLoad==null?"":";"+onLoad) + "</script></div>";
                } else {
                    strHtmlCall = "<div id=\"" + selector + "\"><script>"
                            + scriptName + "(\"" + selector + "\")" + (onLoad==null?"":";"+onLoad) + "</script></div>";
                }
            }
        } else {
            if(name==null) {
                if (async) {
                    if(!(onLoad==null || onLoad.isBlank())) htmlCall.append(onLoad);
                    strHtmlCall = "<div id=\"" + selector + "\">" + jsAgent.htmlCode + "<script>" + htmlCall +
                            "</script><script src=\"/assets/js/" + scriptName + ".js\" async></script></div>";
                } else {
                    strHtmlCall = "<div id=\"" + selector + "\">" + jsAgent.htmlCode + "<script src=\"/assets/js/"
                            + scriptName + ".js\"></script><script>"
                            + scriptName +"("+ htmlCall + ")" + (onLoad==null?"":";"+onLoad) + "</script></div>";

                    template = "function " + scriptName +"("+ mtdArgs + "){" + template + "}";
                }
            } else {
                if(usedScripts.add(scriptName)) {
                    strHtmlCall = "<div id=\"" + selector + "\"><script>"
                            + htmlCall + (scriptName + "(\"" + selector + "\")") + (onLoad==null?"":";"+onLoad)
                            + "</script><script src=\"/assets/js/" + scriptName
                            + ".js\"" + (async ? " async" : "") + "></script></div>";
                } else {
                    strHtmlCall = "<div id=\"" + selector + "\"><script>"
                            + htmlCall + (scriptName + "(\"" + selector + "\")") + (onLoad==null?"":";"+onLoad)
                            + "</script></div>";
                }
            }
        }
        template = jsAgent.preJsCode + template + jsAgent.jsCode;
        worker.getJsLib().put(scriptName+".js", new JsFile(worker, scriptName, template, false));
        currNewKey.set(false);
        curHasUsg.set(false);
        currNewKey = new AtomicBoolean(true);
        curHasUsg = new AtomicBoolean(true);
        return strHtmlCall;
    }


    private String getVar(String key){
        return getVar(new RKeyStr(key, currNewKey));
    }


    private String getVar(RKeyStr keyStr) {
        Node<RKeyStr, String> node = getNode(keyStr);
        if (node != null) {
            RKeyStr realKey = node.getKey();
            realKey.hasUsages = curHasUsg;
            return node.getValue();
        }
        String result0;
        if (async || name != null) {
            int hs = keyStr.val.hashCode();
            if (hs < 0) hs = -hs;
            result0 = "${_var" + CharNum.Default.getCharNumOf(hs) + "}";
        } else {
            result0 = "${_var" + CharNum.Default.getCharNumOf(size()) + "}";
        }
        keyStr.hasUsages = curHasUsg;
        put(keyStr, result0);
        return result0;
    }


    private static Tuple<String, ArrayList<Tuple<String,String>>> prepare(String val0){
        ArrayList<Tuple<String,String>> args = new ArrayList<>();
        boolean isEcran = false;
        char unEcran = '"';
        boolean flag = false;
        boolean flag2 = false;

        StringBuilder arg = new StringBuilder();
        StringBuilder val = new StringBuilder();
        int cursor = 0;
        for(char c:val0.toCharArray()){
            cursor++;
            if(isEcran){
                if(!flag){
                    flag = (c == '\\');
                    if(flag)continue;
                }
                if(!flag && c == unEcran){
                    isEcran = false;
                    args.add(new Tuple<>(arg.toString(), val.toString()));
                    arg = new StringBuilder();
                    val = new StringBuilder();
                    continue;
                }
                val.append(c);
                if(flag) flag = false;
            } else {
                if(c=='>'){
                    if(arg.length()>0) args.add(new Tuple<>(arg.toString(), null));
                    return new Tuple<>(val0.substring(cursor), args);
                }
                if(flag) {
                    if (c == '"' || c == '\'') {
                        flag = false;
                        isEcran = true;
                        unEcran = c;
                    }
                } else {
                    if (c != ' ') {
                        if (c == '=') {
                            flag = true;
                            continue;
                        }
                        if(flag2){
                            if(arg.length()>0) {
                                args.add(new Tuple<>(arg.toString(), null));
                                arg = new StringBuilder();
                                val = new StringBuilder();
                            }
                            flag2 = false;
                        }
                        arg.append(c);
                    } else {
                        flag2 = true;
                    }
                }
            }
        }
        return new Tuple<>(null, args);
    }
}
