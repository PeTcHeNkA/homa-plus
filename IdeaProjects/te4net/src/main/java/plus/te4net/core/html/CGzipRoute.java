package plus.te4net.core.html;

import io.netty.handler.codec.http.HttpHeaderNames;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.node.HWorker;
import plus.tson.TsonInt;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonStr;
import plus.util.SimpleSrcGetter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


@PublicUsage
public class CGzipRoute extends HashMap<String, IRequestProcessor> implements SimpleSrcGetter, IRequestProcessor{
    public static final int DEF_CACHE_TIME = 1200;
    private final String cacheStr;

    public CGzipRoute(HWorker worker, File dir, IBuilder builder) {
        this(worker, dir, DEF_CACHE_TIME, builder);
    }

    public CGzipRoute(int cacheTime){
        cacheStr = "max-age="+cacheTime;
    }


    public static CGzipRoute build(HWorker worker, File dir, TsonMap map){
        int cacheTime = map.getOrDefault("cache",new TsonInt(DEF_CACHE_TIME)).getInt();
        TsonObj headers = map.get("headers");
        TsonObj obj = map.get("type");
        if(obj==null){
            obj = new TsonStr("none");
        }
        IBuilder constructor = null;
        if(obj.isString()) {
            String pressAlgo = obj.getStr();
            constructor = switch (pressAlgo) {
                case "javascript", "js" -> JsFile::new;
                case "css", "style", "stylesheet" -> CssFile::new;
                case "html", "page" -> HtmlRoute::new;
                case "none", "raw", "" -> SimpleCompressedTextFile::new;
                case "static-html", "static-page" -> StaticHtmlFile::new;
                default -> (worker1, file) -> {
                    String pressAlgo0 = pressAlgo;
                    if(pressAlgo0.equals("svg")){
                        pressAlgo0 = "image/svg+xml";
                    }
                    return new CustomGzipFile(pressAlgo0, worker1, file);
                };
            };
        } else if (obj.isCustom()){
            constructor = (IBuilder) obj.getField();
        }
        return headers==null?
                new CGzipRoute(worker, dir, cacheTime, constructor):
                new HGzipRoute(worker, dir, cacheTime, constructor, HeadersProcessor.build(headers));
    }


    public CGzipRoute(HWorker worker, File dir, int cacheTime, IBuilder builder) {
        this(cacheTime);
        if(!dir.exists()){
            dir.mkdirs();
            return;
        }
        ArrayList<String> files = new ArrayList<>();
        addAllFiles(null, dir, files);
        for(String name:files){
            put(name, builder.build(worker, new File(dir, name)));
        }
    }


    public IRequestProcessor put(String key, IRequestProcessor value){
        return super.put(key, value);
    }


    public void include(HWorker worker, String path, String name, boolean onlyInn) {
        include(worker, name, new String(getSrc(path + "/" + name, onlyInn)));
    }


    private void include(HWorker worker, String name, String data) {
        if (name.endsWith(".css")) {
            put(name, new CssFile(worker, name, data, true));
        } else if (name.endsWith(".js")) {
            put(name, new JsFile(worker, name, data, true));
        } else if (name.endsWith(".html")) {
            put(name, new StaticHtmlFile(worker, name,data,true));
        }
    }


    protected static void addAllFiles(String curName, File path, ArrayList<String> cache){
        if(!path.exists())return;
        if(path.isFile()){
            if(curName.equals("")){
                cache.add(path.getName());
            } else {
                cache.add(curName+"/"+path.getName());
            }
        } else {
            File[] files = path.listFiles();
            if(curName == null){
                curName = "";
            } else if(curName.equals("")){
                curName = path.getName();
            } else {
                curName = curName + "/" + path.getName();
            }
            if(files != null){
                for(File file:files){
                    addAllFiles(curName, file, cache);
                }
            }
        }
    }


    @Override
    public void handle(IRequestSession session) {
        session.setHeader(HttpHeaderNames.CACHE_CONTROL, cacheStr);
        IRequestProcessor page = get(session.routeKey().afterRoute());
        if(page==null) {
            session.notFound();
        } else {
            page.handle(session);
        }
    }
}