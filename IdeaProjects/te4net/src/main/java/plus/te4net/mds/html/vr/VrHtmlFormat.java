package plus.te4net.mds.html.vr;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.mds.html.events.VrHtmlEvent;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.util.Te4GZIPOutStream;
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

    public VrHtmlFormat(VrHtml vrHtml, HEWorker worker) {
        StringBuilder builder = new StringBuilder();
        ArrayList<String> items = new ArrayList<>();
        ArrayList<String> vars   = new ArrayList<>();

        vrHtml.toString(builder, (b, parent, item) -> {
            if(item instanceof SField field){
                if(parent != null){
                    if(parent instanceof HtmlNode node){
                        if(node.name==null)return true;
                        if(node.name.equals("script") || node.name.equals("style"))
                            return true;
                    }
                }
                String val = field.value.trim();
                if(val.isEmpty())return true;

                addFormat(items, vars, val, b);

                return false;
            }
            return true;
        });

        items.add(items.remove(items.size()-1)+builder.toString());

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


    private void addFormat(ArrayList<String> items, ArrayList<String> vars,
                           String format, StringBuilder b){
        Tuple<ArrayList<String>, ArrayList<String>> res =
                TextFormatter.prepare(format, "{%", "%}");
        String s;
        if(items.size()>0) {
            s = items.remove(items.size() - 1)
                    + b.toString() + res.A.remove(0);
        } else {
            s = b.toString() + res.A.remove(0);
        }
        b.setLength(0);
        items.add(s);
        items.addAll(res.A);
        vars.addAll(res.B);
    }


    public static VrHtmlFormat ofHtml(HEWorker worker, String data){
        VrHtml vrHtml = new VrHtml(data).uptCss(worker);
        worker.throwEvent(new VrHtmlEvent(worker, VrHtmlEvent.Type.HTML, vrHtml));
        return new VrHtmlFormat(vrHtml, worker);
    }


    @Override
    public void handle(IRequestSession session) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if(needPress){
                session.setHeader(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP);

                Te4GZIPOutStream gzip = new Te4GZIPOutStream(out, 512, Deflater.BEST_SPEED);
                for (int i = 0; i < getters.length; i++) {
                    gzip.write(bitems[i]);
                    String result = getters[i].get(session);
                    if (result != null) {
                        gzip.write(result.getBytes(StandardCharsets.UTF_8));
                    }
                }
                gzip.write(bitems[bitems.length-1]);
                gzip.close();
            } else {
                for (int i = 0; i < getters.length; i++) {
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