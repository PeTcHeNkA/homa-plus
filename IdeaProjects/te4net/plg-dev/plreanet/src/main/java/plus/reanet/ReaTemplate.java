package plus.reanet;

import io.netty.handler.codec.http.HttpHeaderNames;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.bsys.IDataProvider;
import plus.te4net.core.html.SimpleCompressedTextFile;


public class ReaTemplate extends SimpleCompressedTextFile {
    private final String cacheTime;
    public ReaTemplate(IDataProvider worker, String name, String data, String cacheStr) {
        super(worker, name, data);
        cacheTime = cacheStr;
    }

    @Override
    public void handle(IRequestSession session) {
        session.setHeader(HttpHeaderNames.CACHE_CONTROL, cacheTime);
        super.handle(session);
    }
}