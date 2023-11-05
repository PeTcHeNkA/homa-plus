package plus.te4net.core.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import plus.te4net.core.mapping.HRouteMap;
import plus.te4net.core.node.HWorker;
import plus.te4net.core.auth.User;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import static plus.te4net.mds.util.ByteUtils.EMPTY;


@PublicUsage
public interface IRequestSession extends IResponse{
    default void send(byte[] data){
        send(data, HttpResponseStatus.OK);
    }


    void send(byte[] data, HttpResponseStatus status);


    void send(InputStream stream, HttpResponseStatus status, int chunkSize);


    default void send(InputStream stream){
        send(stream, HttpResponseStatus.OK, 8192);
    }


    default void send(String data){
        send(data.getBytes(StandardCharsets.UTF_8), HttpResponseStatus.OK);
    }


    default void send(String data, HttpResponseStatus status){
        send(data.getBytes(StandardCharsets.UTF_8), status);
    }


    void addHeader(CharSequence name, CharSequence value);


    void setHeader(CharSequence name, CharSequence value);


    void removeHeader(CharSequence name);


    String method();


    ChannelHandlerContext context();


    String path();


    String[] splat();


    HRouteMap.StrKey routeKey();


    HWorker worker();


    String queryParams(String label);


    void redirect(String goTo);


    default void notModified(){
        send(EMPTY, HttpResponseStatus.NOT_MODIFIED);
    }


    default void notFound(){
        send(EMPTY, HttpResponseStatus.NOT_FOUND);
    }


    default void ok(){
        send(EMPTY, HttpResponseStatus.OK);
    }


    default void requestError(String cause){
        send(cause, HttpResponseStatus.NOT_IMPLEMENTED);
    }


    default User auth(){
        IAuthProvider sys = worker().getAuthSystem();
        if(sys==null)return null;
        return sys.auth(this);
    }


    Cookie getCookie(String key);


    void setCookie(Cookie cookie);


    default boolean isSafe(){
        return true;
    }


    String getDomain();


    @Override
    default HttpResponseStatus status(){
        throw new UnsupportedOperationException();
    }
}