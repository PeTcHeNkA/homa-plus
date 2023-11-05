package plus.te4net.core.api;

import io.netty.handler.codec.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;


@PublicUsage
public interface IHttpClient {
    void send(String host, String patch, int port, boolean safe, HttpMethod mtd,
              IResponseProcessor processor, byte[] data);


    default void send(String url, HttpMethod mtd, byte[] data, IResponseProcessor proc) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String scheme = uri.getScheme();
        int port = uri.getPort();
        boolean safe = !(scheme == null || "http".equals(scheme));
        if (port == -1) {
            port = safe?443:80;
        }
        send(uri.getHost(), uri.getPath(), port, safe, mtd, proc, data);
    }


    default void send(String url, HttpMethod mtd, IResponseProcessor proc) {
        send(url, mtd, null, proc);
    }


    void send(String url, HttpMethod mtd);


    default void get(String url, IResponseProcessor proc){
        send(url, HttpMethod.GET, proc);
    }


    default void post(String url, byte[] data, IResponseProcessor proc){
        send(url, HttpMethod.POST, data, proc);
    }


    void post(String url, byte[] data);
}