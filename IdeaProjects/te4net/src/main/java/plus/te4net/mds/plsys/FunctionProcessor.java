package plus.te4net.mds.plsys;

import plus.te4net.core.api.PluginFunction;
import plus.te4net.core.node.HEWorker;
import plus.util.IPluginProcessor;


public final class FunctionProcessor implements IPluginProcessor<PluginFunction> {
    private final HEWorker worker;

    public FunctionProcessor(HEWorker worker) {
        this.worker = worker;
    }

    @Override
    public void onLoad(PluginFunction obj) {
        worker.regFunction(obj.name(), obj::onCall, obj.isRoot());
    }


    @Override
    public Class<?> getSupportedType() {
        return PluginFunction.class;
    }
}