package plus.te4net.core.fm;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.node.HWorker;
import plus.te4net.core.html.StaticHtmlFile;
import plus.te4net.utl.Te4GZIPOutStream;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;
import plus.tson.utl.Tuple;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;


public class RouteTextFormatter extends TextFormatter<IRequestSession> {
    private final boolean needPress;
    private final byte[][] bitems;


    public static RouteTextFormatter ofHtml(HWorker worker, String str){
        return new RouteTextFormatter(worker, StaticHtmlFile.optimizeHtml(worker, str));
    }


    public RouteTextFormatter(HWorker worker, String format){
        this(compileFormat(format, worker));
    }


    protected RouteTextFormatter(Tuple<String[], IVarGetter<IRequestSession>[]> data){
        super(new Tuple<>(null, data.B));
        String[] items0 = data.A;
        bitems = new byte[items0.length][];
        int size = 0;
        for(int i=0;i<bitems.length;i++){
            byte[] bData = items0[i].getBytes(StandardCharsets.UTF_8);
            bitems[i] = bData;
            size += bData.length;
        }
        needPress = size>128;
    }


    public void writeFormatted(IRequestSession session){
        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if(needPress){
                session.setHeader(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP);

                Te4GZIPOutStream gzip = new Te4GZIPOutStream(out, 512, Deflater.BEST_SPEED);
                for (short i = 0; i < getters.length; i++) {
                    gzip.write(bitems[i]);
                    String result = getters[i].get(session);
                    if (result != null) {
                        gzip.write(result.getBytes(StandardCharsets.UTF_8));
                    }
                }
                gzip.write(bitems[bitems.length-1]);
                gzip.close();
            } else {
                for (short i = 0; i < getters.length; i++) {
                    out.write(bitems[i]);
                    String result = getters[i].get(session);
                    if (result != null) {
                        out.write(result.getBytes(StandardCharsets.UTF_8));
                    }
                    out.write(bitems[bitems.length-1]);
                }
            }
            session.setHeader(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
            session.send(out.toByteArray(), HttpResponseStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Redirect ex){
            ex.handle(session);
        }
    }


    public static final class Redirect extends RuntimeException implements IRequestProcessor {
        private final String to;
        public Redirect(String to) {
            super();
            this.to = to;
        }
        @Override
        public void handle(IRequestSession session){
            session.setHeader(HttpHeaderNames.CACHE_CONTROL, "no-cache");
            session.redirect(to);
        }
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}