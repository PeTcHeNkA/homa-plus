package plus.te4net.core.node;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.stream.ChunkedStream;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.mapping.HRouteMap;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static plus.te4net.core.node.HServer.SERVER_NAME;


abstract class AbsH1Handler extends ChannelInboundHandlerAdapter implements IRequestSession {
    protected final AbsH1Handler.StdHeaders headers = new StdHeaders();
    protected HRouteMap.StrKey uriParams = null;
    protected Map<String, Cookie> cookieHashMap;
    protected ChannelHandlerContext ctx;
    protected FullHttpRequest request;


    @Override
    public final void send(byte[] data, HttpResponseStatus status){
        HttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, wrappedBuffer(data), headers, new DefaultHttpHeaders(false)
        );
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    @Override
    public final void send(InputStream stream, HttpResponseStatus status, int chunkSize){
        headers.add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        headers.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status, headers);
        ctx.write(response);

        ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(stream, chunkSize)),
                ctx.newProgressivePromise()).addListener(ChannelFutureListener.CLOSE);
    }


    @Override
    public final String method() {
        return request.method().name();
    }


    @Override
    public final String path() {
        return request.uri();
    }

    @Override
    public byte[] encodeBody() {
        return ByteBufUtil.getBytes(request.content());
    }


    @Override
    public final String encodeBodyAsStr() {
        return request.content().toString(StandardCharsets.UTF_8);
    }


    @Override
    public final ChannelHandlerContext context() {
        return ctx;
    }


    @Override
    public final String[] splat() {
        return uriParams.splat();
    }


    @Override
    public final HRouteMap.StrKey routeKey(){
        return uriParams;
    }


    @Override
    public final String queryParams(String label) {
        return uriParams.param(label);
    }


    @Override
    public final Cookie getCookie(String key) {
        if(cookieHashMap==null) {
            String cookie = request.headers().get(HttpHeaderNames.COOKIE);
            if(cookie==null)cookieHashMap = Collections.emptyMap();
            cookieHashMap = CookieCoder.decode(cookie);
        }
        return cookieHashMap.get(key);
    }


    @Override
    public final void setCookie(Cookie cookie) {
        headers.add(HttpHeaderNames.SET_COOKIE, CookieCoder.code(cookie));
    }


    @Override
    public final void addHeader(CharSequence name, CharSequence value) {
        headers.add(name, value);
    }


    @Override
    public final void setHeader(CharSequence name, CharSequence value) {
        headers.set(name, value);
    }


    @Override
    public final void removeHeader(CharSequence name) {
        headers.remove(name);
    }


    @Override
    public final String getRHeader(CharSequence name) {
        return request.headers().get(name);
    }


    @Override
    public final void redirect(String goTo) {
        headers.set(HttpHeaderNames.LOCATION, goTo);
        ctx.writeAndFlush(
                new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, headers)
        );
    }


    private static final class StdHeaders extends DefaultHttpHeaders {
        private StdHeaders() {
            super(false);
            add(HttpHeaderNames.SERVER, SERVER_NAME);
        }
    }
}