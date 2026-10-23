package plus.te4net.core.node.http3;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.charset.StandardCharsets;


public class H3Handler extends Http3RequestStreamInboundHandler {
    private byte[] content = "HELLO WORLD".getBytes(StandardCharsets.UTF_8);

    public H3Handler(){
        System.out.println("NEN!");
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        ReferenceCountUtil.release(frame);
    }


    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        ReferenceCountUtil.release(frame);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {
        Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
        headersFrame.headers().status("404");
        headersFrame.headers().add("server", "netty");
        headersFrame.headers().addInt("content-length", content.length);
        ctx.write(headersFrame);
        ctx.writeAndFlush(new DefaultHttp3DataFrame(Unpooled.wrappedBuffer(content))).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);
    }
}