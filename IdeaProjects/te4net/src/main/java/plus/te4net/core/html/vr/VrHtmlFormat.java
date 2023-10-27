package plus.te4net.core.html.vr;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.node.HWorker;
import plus.te4net.utl.Te4GZIPOutStream;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;
import plus.tson.utl.Tuple;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.Deflater;


public class VrHtmlFormat implements IRequestProcessor {
    protected final IVarGetter<IRequestSession>[] getters;
    private final boolean needPress;
    private final byte[][] bitems;

    public VrHtmlFormat(VrHtml vrHtml, HWorker worker) {
        StringBuilder builder = new StringBuilder();
        ArrayList<String> items = new ArrayList<>();
        ArrayList<String> vars   = new ArrayList<>();

        vrHtml.toString(builder, (b, parent, item) -> {
            if(item instanceof SField field){
                if(parent != null){
                    if(parent instanceof HtmlNode node){
                        if(node.name.equals("script") || node.name.equals("style"))return false;
                    }
                }
                String val = field.value.trim();
                if(val.charAt(0)=='{' && val.charAt(val.length()-1)=='}'){
                    if(val.startsWith("{ssr:")){
                        vars.add(val.substring(5, val.length()-1));
                        items.add(b.toString());
                        b.setLength(0);
                    }
                    return false;
                }
            }
            return true;
        });

        items.add(builder.toString());
        Tuple<String[], IVarGetter<IRequestSession>[]> res =
                TextFormatter.buildFormat(worker, items, vars);

        bitems = new byte[res.A.length][];
        int size = 0;
        for(int i=0;i<bitems.length;i++){
            byte[] bData = res.A[i].getBytes(StandardCharsets.UTF_8);
            bitems[i] = bData;
            size += bData.length;
        }
        needPress = size>128;

        this.getters = res.B;
    }

    public static VrHtmlFormat ofHtml(HWorker worker, String str){
        return new VrHtmlFormat(new VrHtml(str), worker);
        //return new RouteTextFormatter(worker, StaticHtmlFile.optimizeHtml(worker, str));
    }


    @Override
    public void handle(IRequestSession session) {
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