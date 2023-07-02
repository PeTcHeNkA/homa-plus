package plus.te4net.mds.proc;

import io.netty.handler.codec.http.HttpHeaderNames;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Locale;


public class SimpleRoute implements IRequestProcessor {
    private final File dir;
    private final String cahceStr;

    public SimpleRoute(String path, int cacheTime) {
        this.dir = new File(path);
        this.cahceStr = "max-age="+cacheTime;
    }


    @Override
    public void handle(IRequestSession session) {
        session.setHeader(HttpHeaderNames.CACHE_CONTROL, cahceStr);
        String name = session.splat()[0];
        File toRead = new File(dir, name);
        if(toRead.isFile()){
            try {
                session.setHeader(HttpHeaderNames.CONTENT_TYPE, mathType(name, session));
                session.send(new FileInputStream(toRead));
            } catch (FileNotFoundException e){
                session.notFound();
            }
        } else {
            session.notFound();
        }
    }


    protected String mathType(String def, IRequestSession request){
        String type = request.queryParams("type");
        if(type!=null){
            return type;
        }
        String[] items = def.split("\\.");
        if(items.length>1) {
            String ending = items[items.length - 1].toLowerCase(Locale.ROOT);
            switch (ending) {
                case "mp4":
                    return "video/mp4";
                case "webm":
                    return "video/webm";
                case "mp3":
                    return "audio";
            }
        }
        return "file";
    }
}
