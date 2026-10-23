package plus.reanet;

import io.netty.handler.codec.http.HttpHeaderNames;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.RequestSession;
import plus.te4net.mds.html.SimpleCompressedTextFile;


public class ReaTemplate extends SimpleCompressedTextFile implements IRequestProcessor {
    private final String cacheTime;

    public ReaTemplate(IDataProvider worker, String type, String name, String data, String cacheStr) {
        super(worker, type, name, data);
        cacheTime = cacheStr;
    }

    public ReaTemplate(IDataProvider worker, String name, String data, String cacheStr) {
        super(worker, name, data);
        cacheTime = cacheStr;
    }


    @Override
    public void handle(RequestSession session) {
        session.setHeader(HttpHeaderNames.CACHE_CONTROL, cacheTime);
        super.handle(session);
    }
}