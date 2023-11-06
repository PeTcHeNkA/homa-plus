package plus.te4net.mds.html;

import io.netty.handler.codec.http.HttpHeaderNames;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.node.HEWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


@PublicUsage
public class FileThreeProc implements IRequestProcessor {
    private final File root;
    private final String cacheStr;

    public FileThreeProc(HEWorker worker, File root, int cacheTime){
        this.root = root;
        cacheStr = "max-age="+cacheTime;
    }


    @Override
    public void handle(RequestSession session) {
        try {
            String ae;
            File file = new File(root, ae = session.routeKey().afterRoute());
            if(!file.isFile()){
                session.notFound();
                return;
            }
            int hs = (int) (file.lastModified() + ae.hashCode()*31);
            String etag = session.getRHeader(HttpHeaderNames.IF_NONE_MATCH);

            boolean flag = false;
            if(etag!=null){
                try {
                    flag = Integer.parseInt(etag) == hs;
                } catch (NumberFormatException ignored){}
            }
            if(flag){
                session.notModified();
            } else {
                session.setHeader(HttpHeaderNames.CACHE_CONTROL, cacheStr);
                session.setHeader(HttpHeaderNames.ETAG, String.valueOf(hs));

                FileInputStream is = new FileInputStream(file);
                session.send(is);
            }
        } catch (FileNotFoundException e) {
            session.notFound();
        }
    }
}