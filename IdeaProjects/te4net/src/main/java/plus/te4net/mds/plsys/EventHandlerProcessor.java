package plus.te4net.mds.plsys;

import plus.te4net.core.api.IEventHandler;
import plus.te4net.core.node.HEWorker;
import plus.util.IPluginProcessor;


public class EventHandlerProcessor implements IPluginProcessor<IEventHandler<?>> {
    private final HEWorker worker;
    public EventHandlerProcessor(HEWorker worker) {
        this.worker = worker;
    }

    @Override
    public void onLoad(IEventHandler<?> obj) {
        worker.addEventHandler(obj);
    }


    @Override
    public Class<?> getSupportedType() {
        return IEventHandler.class;
    }
}
