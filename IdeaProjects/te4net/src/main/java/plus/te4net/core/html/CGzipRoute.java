package plus.te4net.core.html;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.node.HWorker;
import plus.te4net.core.html.CssFile;
import plus.te4net.core.html.IBuilder;
import plus.te4net.core.html.JsFile;
import plus.te4net.core.html.StaticHtmlFile;
import plus.util.SimpleSrcGetter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class CGzipRoute extends HashMap<String, IRequestProcessor> implements SimpleSrcGetter, IRequestProcessor{
    private final String cacheStr;

    public CGzipRoute(HWorker worker, File dir, IBuilder builder) {
        this(worker, dir, 1200, builder);
    }

    public CGzipRoute(HWorker worker, File dir, int cacheTime, IBuilder builder) {
        cacheStr = "max-age="+cacheTime;
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
        IRequestProcessor page = get(session.splat()[0]);
        if(page==null) {
            session.notFound();
        } else {
            page.handle(session);
        }
    }
}