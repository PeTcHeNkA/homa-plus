package plus.te4net.core.html.legacy;

import plus.te4net.core.html.CssFile;
import plus.te4net.core.html.StaticHtmlFile;
import plus.te4net.core.node.HWorker;
import plus.te4net.utl.CharNum;
import plus.te4net.utl.IReplacer;
import plus.tson.utl.Tuple;
import plus.tson.utl.Te4HashMap;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class VirtualCss implements IReplacer {
    private final HashMap<String,Te4HashMap<VKeyStr, String>> cssItems = new HashMap<>();
    private final AtomicInteger cssVarUsages = new AtomicInteger(-1);
    private final HWorker worker;
    private boolean ended = false;
    private String cssName = null;
    private int usages = -1;
    int count = 0;

    public VirtualCss(HWorker worker) {
        this.worker = worker;
    }


    static final class VKeyStr {
        final String val;
        private final int number;

        VKeyStr(String val, int num) {
            this.val = val;
            number = num;
        }

        VKeyStr(String val) {
            this.val = val;
            number = 0;
        }


        @Override
        public boolean equals(Object o) {
            if (o instanceof String) return val.equals(o);
            return val.equals(((VKeyStr) o).val);
        }


        @Override
        public int hashCode() {
            return val.hashCode();
        }


        @Override
        public String toString() {
            return val;
        }
    }


    private void read(String code) {
        if (ended) throw new RuntimeException("Reader ended!");
        char[] chars = code.toCharArray();

        boolean isName = true;
        boolean isMeta = false;
        boolean waitEndMeta = false;

        StringBuilder name = new StringBuilder();
        StringBuilder style = new StringBuilder();
        StringBuilder metaName = new StringBuilder();

        Te4HashMap<VKeyStr, String> defMeta;
        if(cssItems.containsKey("")){
            defMeta = cssItems.get("");
        } else {
            cssItems.put("", defMeta = new Te4HashMap<>());
        }
        Te4HashMap<VKeyStr, String> curMeta = defMeta;

        for (char c : chars) {
            if (isName) {
                if(c=='}') {
                    if (waitEndMeta) {
                        cssItems.put(metaName.toString(), curMeta);
                        metaName = new StringBuilder();
                        waitEndMeta = false;
                        curMeta = defMeta;
                        continue;
                    }
                    throw new RuntimeException("CSS Syntax ERROR!");
                }
                if(c=='{'){
                    if(isMeta){
                        curMeta = new Te4HashMap<>();
                        waitEndMeta = true;
                        isMeta = false;
                    } else {
                        isName = false;
                    }
                    continue;
                }
                if(isMeta){
                    metaName.append(c);
                } else {
                    if(c == '@'){
                        isMeta = true;
                        metaName.append(c);
                    } else {
                        name.append(c);
                    }
                }
            } else {
                if (c == '}') {
                    isName = true;
                    String ns = name.toString();
                    VKeyStr rwNsKey = new VKeyStr(ns);
                    if (curMeta.containsKey(rwNsKey)) {
                        Te4HashMap.Node<VKeyStr, String> node = curMeta.getNode(rwNsKey);
                        String usedCss = node.getValue();
                        curMeta.put(node.getKey(), usedCss.substring(0, usedCss.length() - 1) + ";" + style + "}");
                    } else {
                        curMeta.put(new VKeyStr(ns, ++count), "{" + style + "}");
                    }
                    name = new StringBuilder();
                    style = new StringBuilder();
                    continue;
                }
                style.append(c);
            }
        }
    }


    private Tuple<String, String> toString(String html) {
        if (ended) throw new RuntimeException("Value ready calculated!");
        ended = true;
        Pattern pattern = Pattern.compile("class=\"(.*?)\"");
        Matcher matcher = pattern.matcher(html);

        HashSet<String> usedClasses = new HashSet<>();
        Te4HashMap<String, String> realClasses = new Te4HashMap<>();


        for (MatchResult r : matcher.results().toList()) {
            String cssClassName = r.group();
            cssClassName = cssClassName.substring(7, cssClassName.length() - 1);
            if (cssClassName.contains(" ")) {
                for (String subCssClass : cssClassName.split(" ")) {
                    if (cssClassName.length()<2 || cssClassName.startsWith("__")) continue;
                    usedClasses.add(subCssClass);
                }
            } else {
                if (cssClassName.length()<2 || cssClassName.startsWith("__")) continue;
                usedClasses.add(cssClassName);
            }
            realClasses.put(cssClassName, cssClassName);
        }

        final String[] realKeys = realClasses.keysToArray(new String[realClasses.size()]);

        tmMetas = cssItems.values().toArray(new Te4HashMap[0]);
        for (String cssClass : usedClasses) {
            String newName = replaceIfContainsCss(cssClass);
            if (newName != null) {
                for (String realClass : realKeys) {
                    if (realClass.contains(" ")) {
                        StringBuilder sb = new StringBuilder();
                        boolean flag2 = false;
                        for (String realClassPart : realClasses.get(realClass).split(" ")) {
                            if (realClassPart.equals(cssClass)) {
                                if (flag2) {
                                    sb.append(" ").append(newName);
                                } else {
                                    sb.append(newName);
                                    flag2 = true;
                                }
                            } else {
                                if (flag2) {
                                    sb.append(" ").append(realClassPart);
                                } else {
                                    sb.append(realClassPart);
                                    flag2 = true;
                                }
                            }
                        }
                        if (flag2) {
                            realClasses.put(realClass, sb.toString());
                        }
                    } else if (realClass.equals(cssClass)) {
                        realClasses.put(realClass, newName);
                    }
                }
            }
        }
        String resultHtml = html;

        for (String realClass : realKeys) {
            Te4HashMap.Node<String, String> node = realClasses.getNode(realClass);
            resultHtml = resultHtml.replace(
                    "class=\"" + node.getKey() + "\"", "class=\"" + node.getValue() + "\""
            );
        }

        StringBuilder newCssCode = new StringBuilder();
        for(String key:cssItems.keySet().stream().sorted().toList()) {
            boolean isMeta = !key.isBlank();
            Te4HashMap<VKeyStr, String> curMeta = cssItems.get(key);
            if(isMeta){
                newCssCode.append(key).append("{");
            }

            List<VKeyStr> styles = new ArrayList<>(curMeta.keySet().stream().toList());
            styles.sort(Comparator.comparingInt(o -> o.number));
            for (VKeyStr cssName : styles) {
                String cssCode = curMeta.get(cssName);
                newCssCode.append(cssName.val).append(cssCode);
            }

            if(isMeta){
                newCssCode.append("}");
            }
        }
        return new Tuple<>(resultHtml, newCssCode.toString());
    }


    private String[] splitPrefix(String s) {
        int index = s.indexOf(":");
        if (index > 0) {
            return new String[]{s.substring(1, index), s.substring(index)};
        } else {
            return new String[]{s.substring(1), ""};
        }
    }


    private Te4HashMap<VKeyStr, String>[] tmMetas;
    private String replaceIfContainsCss(String cssClass) {
        String newName = null;

        for(Te4HashMap<VKeyStr, String> curMeta:tmMetas) {
            for (VKeyStr key : curMeta.keySet().stream().toList()) {
                String innerCssKey = key.val;
                boolean hasSplit = innerCssKey.contains(",");
                if (!hasSplit) {
                    String[] cssName = splitPrefix(innerCssKey);
                    if (cssName[0].equals(cssClass)) {
                        if (newName == null) newName = getCssName(++usages);
                        curMeta.put(new VKeyStr("." + newName + cssName[1], key.number), curMeta.remove(key));
                    }
                } else {
                    StringBuilder newKys = new StringBuilder();
                    boolean flag4 = false;
                    boolean flag5 = false;
                    for (String tabbedInnerCss : innerCssKey.split(",")) {
                        String[] cssName = splitPrefix(tabbedInnerCss);
                        if (cssName[0].equals(cssClass)) {
                            flag4 = true;
                            if (newName == null) newName = getCssName(++usages);
                            if (flag5) {
                                newKys.append(",").append(".").append(newName).append(cssName[1]);
                            } else {
                                newKys.append(".").append(newName).append(cssName[1]);
                                flag5 = true;
                            }
                        } else {
                            if (flag5) {
                                newKys.append(",").append(tabbedInnerCss);
                            } else {
                                newKys.append(tabbedInnerCss);
                                flag5 = true;
                            }
                        }
                    }
                    if (flag4) {
                        String finalNEwKey = newKys.toString();
                        curMeta.put(new VKeyStr(finalNEwKey, key.number), curMeta.remove(key));
                    }
                }
            }
        }
        return newName;
    }


    public String applyCss(String html) {
        Tuple<String, String> htmlCss = toString(html);
        String varOptimized = CssFile.optimizeVars(htmlCss.B, cssVarUsages);
        worker.getCssLib().put(cssName, new CssFile(worker, cssName, varOptimized, false));
        return htmlCss.A;
    }


    private String getCssName(int i) {
        return "_" + CharNum.Default.getCharNumOf(i);
    }


    @Override
    public String replace(String css) {
        if (cssName == null) {
            read(CssFile.optimizeCss(css));
            cssName = StaticHtmlFile.getName("t.css", css);
            return "<link rel=\"stylesheet\" href=\"/assets/css/" + cssName + "\">";
        } else {
            read(CssFile.optimizeCss(css));
            return "";
        }
    }
}
