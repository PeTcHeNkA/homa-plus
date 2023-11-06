package plus.te4net.mds.plsys;

import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.node.HEWorker;
import plus.util.IPluginProcessor;


public final class RouteProcessor implements IPluginProcessor<PluginRoute> {
    private final HEWorker worker;

    public RouteProcessor(HEWorker worker) {
        this.worker = worker;
    }


    @Override
    public void onLoad(PluginRoute obj) {
        worker.addRoute(obj.get(), obj);
    }


    @Override
    public Class<?> getSupportedType() {
        return PluginRoute.class;
    }
}