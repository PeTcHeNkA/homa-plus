package plus.reanet;

import io.netty.handler.codec.http.HttpHeaderNames;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PluginBase;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.html.CssFile;
import plus.te4net.mds.html.JsFile;
import plus.tson.TsonInt;
import plus.tson.TsonMap;


public class ReaNet implements PluginBase {
    public ReaNet(HEWorker worker, TsonMap map){
        String cstr = "max-age="+map.getOrDefault("reanet-cahce", new TsonInt(432000)).getStr();
        add(worker, cstr, "reanet-min.js","reanet.js");
        add(worker, cstr, "modules/lang-min.js",      "modules/lang.js"      );
        add(worker, cstr, "modules/sl_rloader-min.js","modules/sl_rloader.js");

        addCss(worker, cstr, "stdboost.css", "stdboost.css");
    }


    private void add(HEWorker worker, String cacheTime, String name, String vi){
        String code = getStrSrc(name);
        RJsFile rjs = new RJsFile(worker, name, code, cacheTime);
        worker.addRoute(vi, rjs);
    }


    private void addCss(HEWorker worker, String cacheTime, String name, String vi){
        String code = getStrSrc(name);
        RCssFile rjs = new RCssFile(worker, name, code, cacheTime);
        worker.addRoute(vi, rjs);
    }


    private static class RCssFile extends CssFile {
        private final String cacheTime;
        public RCssFile(IDataProvider worker, String name, String data, String cacheStr) {
            super(worker, name, data);
            cacheTime = cacheStr;
        }
        @Override
        public void handle(RequestSession session) {
            session.setHeader(HttpHeaderNames.CACHE_CONTROL, cacheTime);
            super.handle(session);
        }
    }

    private static class RJsFile extends JsFile {
        private final String cacheTime;
        public RJsFile(IDataProvider worker, String name, String data, String cacheStr) {
            super(worker, name, data);
            cacheTime = cacheStr;
        }

        @Override
        public void handle(RequestSession session) {
            session.setHeader(HttpHeaderNames.CACHE_CONTROL, cacheTime);
            super.handle(session);
        }
    }
}