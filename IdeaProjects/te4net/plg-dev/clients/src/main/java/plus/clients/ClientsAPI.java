package plus.clients;

import io.netty.handler.codec.http.HttpHeaderNames;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.bsys.DataProviderBuilder;
import plus.te4net.core.bsys.IDataProvider;
import plus.te4net.core.node.HWorker;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.nio.charset.StandardCharsets;


public final class ClientsAPI implements PluginRoute {
    private final IDataProvider provider;
    private final TsonList cacheItems = new TsonList();
    private boolean needScan = true;

    public ClientsAPI(HWorker worker, TsonMap config){
        provider = DataProviderBuilder.ofTson(worker, config.getOrDefault(
                "clients-plug-data", new TsonMap("{type='memory',parent='root:clients'}")
        ));
    }


    @Override
    public String get() {
        return "/clients/api/";
    }


    @Override
    public void handle(IRequestSession session) {
//        if(session.auth()!=null){
//            session.requestError("need auth");
//            return;
//        }
        String mdt = session.queryParams("mtd");
        if(mdt==null){
            session.requestError("bad method");
            return;
        }
        if(session.method().equals("GET")){
            switch (mdt){
                case "list":sendList(session);return;
            }
        } else {
            switch (mdt){
                case "add":addItem(session);return;
                case "items":sendItems(session);return;
            }
        }
        session.requestError("bad method");
    }


    private void addItem(IRequestSession session){
        rescanIfNeed();
        TsonMap tm = new TsonMap(session.encodeBodyAsStr().replace("\n","<br>"));
        String name;
        if(!cacheItems.add(name = tm.getStr("name"))){
            session.requestError("ready exists");
            return;
        }
        tm.put("time", String.valueOf(System.currentTimeMillis()));
        provider.saveRaw(
                tm.toString().getBytes(StandardCharsets.UTF_8),
                "reviews", name
        );
    }


    private void sendItems(IRequestSession session){
        rescanIfNeed();
        TsonMap result = new TsonMap();
        for(TsonObj name:new TsonList(session.encodeBodyAsStr())){
            byte[] review = provider.getRaw("reviews", name.getStr());
            if(review != null) {
                result.put(name.getStr(), new TsonMap(new String(review, StandardCharsets.UTF_8)));
            }
        }
        session.setHeader(HttpHeaderNames.CACHE_CONTROL, "max-age=1200");
        session.send(result.toJsonStr());
    }


    private void sendList(IRequestSession session){
        rescanIfNeed();
        session.send(cacheItems.toJsonStr());
    }


    private void rescanIfNeed(){
        if(!needScan)return;
        needScan = false;
        for(String item: provider.getItems("reviews")){
            cacheItems.add(item);
        }
    }
}