package plus.te4net.core.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http2.*;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.mapping.HRouteMap;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static plus.te4net.core.node.HServer.SERVER_NAME;


@ChannelHandler.Sharable
final class H2Handler extends ChannelDuplexHandler implements RequestSession {
    private Map<String, Cookie> cookieHashMap;
    private Std2Headers headers;
    private final String usedDomain;
    private final HWorker worker;
    private ChannelHandlerContext ctx;
    private HRouteMap.StrKey uriParams;
    private Http2DataFrame data;
    private Http2HeadersFrame headersFrame;

    public H2Handler(HWorker worker, String domain) {
        this.worker = worker;
        this.usedDomain = domain;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if(!ctx.isRemoved()) ctx.close();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Http2HeadersFrame) {
            this.ctx = ctx;
            onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            onDataRead(ctx, (Http2DataFrame) msg);
        } else {
            try {
                super.channelRead(ctx, msg);
            } catch (Exception e){}
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    private void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) {
        if(data.isEndStream()){
            this.data = data;
            this.cookieHashMap = null;
            this.headers = new Std2Headers();
            this.worker.handle(this);
        } else {
            data.release();
        }
    }


    private void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers) {
        this.headersFrame = headers;
        this.uriParams = new HRouteMap.StrKey(headers.headers().path().toString());
        if (headers.isEndStream()) {
            this.cookieHashMap = null;
            this.headers = new Std2Headers();
            this.worker.handle(this);
        }
    }


    @Override
    public void send(byte[] data, HttpResponseStatus status) {
        headers.status(status.codeAsText());
        ByteBuf content = ctx.alloc().buffer();
        content.writeBytes(data);
        Http2FrameStream stream = headersFrame.stream();
        ctx.write(new DefaultHttp2HeadersFrame(headers).stream(stream));
        ctx.write(new DefaultHttp2DataFrame(content, true).stream(stream));
    }


    @Override
    public void send(InputStream stream, HttpResponseStatus status, int chunkSize) {
        headers.status(status.codeAsText());
        ctx.write(new DefaultHttp2HeadersFrame(headers).stream(headersFrame.stream()));
        try {
            Http2FrameStream stream0 = headersFrame.stream();
            ChannelFutureListener listener = new ChannelFutureListener() {
                byte[] res = stream.readNBytes(chunkSize);
                @Override
                public void operationComplete(ChannelFuture future) throws IOException {
                    byte[] newRes = stream.readNBytes(chunkSize);
                    ByteBuf content = ctx.alloc().buffer();
                    content.writeBytes(res);
                    if(newRes.length>0) {
                        ctx.writeAndFlush(new DefaultHttp2DataFrame(content).stream(stream0)).addListener(this);
                        res = newRes;
                    } else {
                        ctx.writeAndFlush(new DefaultHttp2DataFrame(content, true).stream(stream0));
                        stream.close();
                    }
                }
            };
            listener.operationComplete(null);
        } catch (Exception e) {
            try {
                stream.close();
            } catch (IOException ignored) {}
        }
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
        CharSequence result = headersFrame.headers().get(name);
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
            List<CharSequence> items = headersFrame.headers().getAll("cookie");
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
        return headersFrame.headers().path().toString();
    }


    @Override
    public String method() {
        return headersFrame.headers().method().toString();
    }


    private static final class Std2Headers extends DefaultHttp2Headers {
        public Std2Headers() {
            super(false);
            add(HttpHeaderNames.SERVER, SERVER_NAME);
        }
    }


    @Override
    public String toString() {
        return "["+worker+"]:HTTPS/2 "+method()+" "+path();
    }
}