package plus.te4net.mds.html;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.api.RequestSession;


@PublicUsage
public class TupleProcessor implements IRequestProcessor{
    private final IRequestProcessor first, second;

    public TupleProcessor(IRequestProcessor first, IRequestProcessor second) {
        this.first = first;
        this.second = second;
    }


    @Override
    public void handle(RequestSession session) {
        first.handle(session);
        second.handle(session);
    }
}