package plus.te4net.mds.plsys;

import plus.te4net.core.api.IEventHandler;
import plus.te4net.core.node.HWorker;
import plus.util.IPluginProcessor;


public class EventHandlerProcessor implements IPluginProcessor<IEventHandler<?>> {
    private final HWorker worker;
    public EventHandlerProcessor(HWorker worker) {
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
