package plus.te4net.core.node;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import plus.te4net.core.api.IResponse;
import plus.te4net.core.api.IResponseProcessor;
import java.util.ArrayList;


final class H1ClientHandler extends SimpleChannelInboundHandler<HttpObject> implements IResponse{
    private final IResponseProcessor processor;
    private HttpHeaders headers;
    private HttpResponseStatus status;
    private ArrayList<byte[]> items;
    private byte[] data;

    public H1ClientHandler(IResponseProcessor processor) {
        this.processor = processor;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            status = response.status();
            headers = response.headers();
        }
        if (msg instanceof HttpContent) {
            byte[] bytes = ByteBufUtil.getBytes(((HttpContent) msg).content());

            if (msg instanceof LastHttpContent) {
                ctx.close();
                if(items==null){
                    data = bytes;
                } else {
                    int size = 0;
                    for (int i=0;i<items.size();i++){
                        size += items.get(i).length;
                    }
                    data = new byte[size];
                    size = 0;

                    for (int i=0;i<items.size();i++){
                        byte[] src = items.get(i);
                        System.arraycopy(src, 0, data, size, src.length);
                        size += src.length;
                    }
                }
                processor.handle(this);
            } else {
                if(items==null)items = new ArrayList<>();
                items.add(bytes);
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }


    @Override
    public String getRHeader(CharSequence name) {
        return headers.get(name);
    }


    @Override
    public byte[] encodeBody() {
        return data;
    }


    @Override
    public HttpResponseStatus status() {
        return status;
    }
}