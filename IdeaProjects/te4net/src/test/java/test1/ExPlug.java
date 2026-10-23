package test1;

import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.node.HEWorker;
import plus.tson.TJsonParser;
import plus.tson.TsonObj;


public class ExPlug implements PluginRoute {
    public ExPlug(HEWorker worker){
        //worker.client()...
    }


    @Override
    public void handle(RequestSession session) {
        session.worker().client().get("https://test/object.json", response -> {
            TsonObj map = new TJsonParser(response.encodeBody()).getMap();
            session.send(map.getMap().getStr("value"));
        });
    }


    @Override
    public String get() {
        return "/your/router";
    }
}
