package plus.te4net.core.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import plus.te4net.core.mapping.HRouteMap;
import plus.te4net.core.node.HWorker;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public interface IRequestSession {
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


    String getRHeader(CharSequence name);


    String method();


    ChannelHandlerContext context();


    String path();


    String[] splat();


    HRouteMap.StrKey routeKey();


    HWorker worker();


    String queryParams(String label);


    void redirect(String goTo);


    byte[] encodeBody();


    default String encodeBodyAsStr(){
        return new String(encodeBody(), StandardCharsets.UTF_8);
    }


    default void notModified(){
        send("", HttpResponseStatus.NOT_MODIFIED);
    }


    default void notFound(){
        send("", HttpResponseStatus.NOT_FOUND);
    }


    Cookie getCookie(String key);


    void setCookie(Cookie cookie);


    default boolean isSafe(){
        return true;
    }


    String getDomain();
}