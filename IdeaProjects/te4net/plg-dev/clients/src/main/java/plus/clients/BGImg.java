package plus.clients;

import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PluginFunction;


public class BGImg implements PluginFunction {
    @Override
    public String name() {
        return "clients-bg-img";
    }


    @Override
    public String onCall(IRequestSession request) {
        String bg;
        if((bg=request.queryParams("bg"))!=null){
            return "style=\"background-image: url(/assets/img/"+bg+")\"";
        }
        return "";
    }
}