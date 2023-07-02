package plus.te4net.core.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http2.*;
import io.netty.handler.stream.ChunkedStream;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.mapping.HRouteMap;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static plus.te4net.core.node.HServer.SERVER_NAME;


@ChannelHandler.Sharable
final class H2Handler extends ChannelDuplexHandler implements IRequestSession {
    private Map<String, Cookie> cookieHashMap;
    private Std2Headers headers;
    private final String usedDomain;
    private final HWorker worker;
    private ChannelHandlerContext ctx;
    private HRouteMap.StrKey uriParams;
    private Http2DataFrame data;
    private Http2Headers resHeaders;
    private ByteBuf content;


    public H2Handler(HWorker worker, String domain) {
        this.worker = worker;
        this.usedDomain = domain;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            this.ctx = ctx;
            onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            onDataRead(ctx, (Http2DataFrame) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    private void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) {
        if(data.isEndStream()){
            this.data = data;
            this.content = ctx.alloc().buffer();
            this.cookieHashMap = null;
            this.headers = new Std2Headers();//TODO
            this.worker.handle(this);
        } else {
            data.release();
        }
    }


    private void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers) {
        this.resHeaders = headers.headers();
        this.uriParams = new HRouteMap.StrKey(resHeaders.path().toString());
        if (headers.isEndStream()) {
            this.content = ctx.alloc().buffer();
            this.cookieHashMap = null;
            this.headers = new Std2Headers();//TODO
            this.worker.handle(this);
        }
    }


    @Override
    public void send(byte[] data, HttpResponseStatus status) {
        headers.status(status.codeAsText());
        content.writeBytes(data);
        ctx.write(new DefaultHttp2HeadersFrame(headers));
        ctx.write(new DefaultHttp2DataFrame(content, true));
    }


    @Override//TODO
    public void send(InputStream stream, HttpResponseStatus status, int chunkSize) {
        headers.status(status.codeAsText());
        ctx.write(new DefaultHttp2HeadersFrame(headers));
        ctx.write(new HttpChunkedInput(new ChunkedStream(stream, chunkSize)));
//        ctx.writeAndFlush(new Http2DataChunkedInput(
//                new ChunkedFile(raf, 0, fileLength, 8192), stream)
//        );
    }


    @Override
    public void setHeader(CharSequence name, CharSequence value) {
        headers.set(name, value);
    }


    @Override
    public void addHeader(CharSequence name, CharSequence value) {
        headers.add(name, value);
    }


    @Override
    public void removeHeader(CharSequence name) {
        headers.remove(name);
    }


    @Override
    public void redirect(String goTo) {
        headers.status(HttpResponseStatus.TEMPORARY_REDIRECT.codeAsText());
        headers.set(HttpHeaderNames.LOCATION, goTo);
        ctx.write(new DefaultHttp2HeadersFrame(headers));
    }


    @Override
    public byte[] encodeBody() {
        return data==null?new byte[0]:ByteBufUtil.getBytes(data.content());
    }


    @Override
    public String encodeBodyAsStr() {
        return data.content().toString(StandardCharsets.UTF_8);
    }


    @Override//TODO
    public String getRHeader(CharSequence name) {
        CharSequence result = resHeaders.get(name);
        if(result==null)return null;
        return result.toString();
    }


    @Override
    public ChannelHandlerContext context() {
        return ctx;
    }


    @Override
    public String[] splat() {
        return uriParams.splat();
    }


    @Override
    public HRouteMap.StrKey routeKey() {
        return uriParams;
    }


    @Override
    public String queryParams(String label) {
        return uriParams.param(label);
    }


    @Override
    public HWorker worker() {
        return worker;
    }


    @Override//TODO
    public Cookie getCookie(String key) {
        if(cookieHashMap==null) {
            List<CharSequence> items = resHeaders.getAll("cookie");
            if(items.size()>0) {
                cookieHashMap = new HashMap<>();
                for (CharSequence item : items) {
                    CookieCoder.decode(cookieHashMap, item.toString());
                }
            } else {
                cookieHashMap = Collections.emptyMap();
            }
        }
        return cookieHashMap.get(key);
    }


    @Override
    public void setCookie(Cookie cookie) {
        headers.add(HttpHeaderNames.SET_COOKIE, CookieCoder.code(cookie));
    }


    @Override
    public String getDomain() {
        return usedDomain;
    }


    @Override
    public String path() {
        return resHeaders.path().toString();
    }


    @Override
    public String method() {
        return resHeaders.method().toString();
    }


    private static final class Std2Headers extends DefaultHttp2Headers {
        public Std2Headers() {
            super(false);
            add(HttpHeaderNames.SERVER, SERVER_NAME);
        }
    }
}