package plus.te4net.mds.html;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.util.Te4GZIPOutStream;
import plus.tson.TsonFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


@PublicUsage
public class SimpleCompressedTextFile implements IRequestProcessor{
    public static final String TYPE_HTML = "text/html;charset=utf-8",
                               TYPE_TEXT = "text",
                               TYPE_CSS  = "text/css",
                               TYPE_JS   = "text/javascript";

    private final byte[] brotli, data;
    private final int hs;
    private final String type;

    public SimpleCompressedTextFile(HEWorker worker, String name, String data) {
        this(worker, name, data, true);
    }


    public SimpleCompressedTextFile(HEWorker worker, String type, File file){
        this(worker, file.getName(), TsonFile.read(file,""), true, type);
    }


    public SimpleCompressedTextFile(HEWorker worker, File file){
        this(worker, file.getName(), TsonFile.read(file,""), true, TYPE_TEXT);
    }


    public SimpleCompressedTextFile(HEWorker worker, String name, String data, boolean compress) {
        this(worker, name, data, compress, TYPE_TEXT);
    }


    public SimpleCompressedTextFile(HEWorker worker, String name, String data, boolean compress, String type) {
        this.type = type;
        if(data.isBlank()){
            this.data = new byte[0];
            this.brotli = this.data;
            hs = 0;
        } else {
            String optimized = compress?replace(worker, data):data;
            this.data = gZip(optimized);
            hs = Arrays.hashCode(this.data);
            this.brotli = createOrCompressBrotli(worker, name,hashCode(),optimized);
            printStatistic(worker, name, data, this.brotli);
        }
    }


    public SimpleCompressedTextFile(IDataProvider worker, String name, String data){
        this(worker, TYPE_TEXT, name, data);
    }


    public SimpleCompressedTextFile(IDataProvider worker, String type, String name, String data){
        this.type = type;
        this.data = gZip(data);
        hs = Arrays.hashCode(this.data);
        this.brotli = createOrCompressBrotli(worker, name,hashCode(), data);
    }


    protected static byte[] createOrCompressBrotli(IDataProvider provider, String name, int hs, String optimized){
        byte[] result = provider.getRaw(name);
        if(result!=null){
            int orHs = bytesToInt(result);
            if(hs==orHs){
                byte[] fResult = new byte[result.length-4];
                System.arraycopy(result, 4, fResult, 0, fResult.length);
                return fResult;
            }
        }
        result = brotli(optimized);
        saveBroil(provider, name, hs, result);
        return result;
    }


    public static void printStatistic(HEWorker worker, String path, String raw, byte[] pressed){
        String domain = worker.getMainDomain();
        if(domain==null)domain = "*";
        HEWorker.getLogger().info(String.format("[%s/%s] (%s) -> %s (%s%%)", domain, path, raw.length(),
                pressed.length, ((int)(((float)pressed.length/raw.length())*100))));
    }


    protected static void saveBroil(IDataProvider provider, String name, int hashCode, byte[] brData){
        byte[] hs = intToBytes(hashCode);
        byte[] data0 = new byte[brData.length+hs.length];
        System.arraycopy(hs, 0, data0, 0, hs.length);
        System.arraycopy(brData, 0, data0, hs.length, brData.length);
        provider.saveRaw(data0, name);
    }


    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                ((bytes[3] & 0xFF));
    }


    private static byte[] intToBytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i);
        return result;
    }


    public static byte[] gZip(String str){
        if (str == null || str.length() == 0) {
            return new byte[0];
        }
        return gZip(str.getBytes(StandardCharsets.UTF_8), Deflater.BEST_COMPRESSION);
    }


    public static byte[] gZip(byte[] str, int lvl) {
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        DeflaterOutputStream gzip = null;
        try {
            gzip = new Te4GZIPOutStream(obj, 512, lvl);
            gzip.write(str);
            gzip.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return obj.toByteArray();
    }


    public static byte[] brotli(String str){
        return brotli(str.getBytes(StandardCharsets.UTF_8), 11);
    }


    public static byte[] brotli(byte[] str, int lvl) {
        Encoder.Parameters params = new Encoder.Parameters().setQuality(lvl);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            BrotliOutputStream brotliOutputStream = new BrotliOutputStream(bOut, params);
            brotliOutputStream.write(str);
            brotliOutputStream.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        return bOut.toByteArray();
    }


    @Override
    public void handle(RequestSession session) {
        String etag = session.getRHeader(HttpHeaderNames.IF_NONE_MATCH);
        boolean flag = false;
        if(etag!=null){
            try {
                flag = Integer.parseInt(etag) == hs;
            } catch (NumberFormatException ignored){}
        }
        if(flag){
            session.notModified();
        } else {
            session.setHeader(HttpHeaderNames.CONTENT_TYPE, type());
            session.setHeader(HttpHeaderNames.ETAG, String.valueOf(hs));
            if (maybeBroil(session)) {
                session.setHeader(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.BR);
                session.send(brotli, HttpResponseStatus.OK);
            } else {
                session.setHeader(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP);
                session.send(data, HttpResponseStatus.OK);
            }
        }
    }


    public final String type() {
        return type;
    }


    public static boolean maybeBroil(RequestSession session){
        char[] chars = session.getRHeader(HttpHeaderNames.ACCEPT_ENCODING).toCharArray();
        int ct = chars.length;
        for(int i=0;i<ct;i++){
            if(chars[i]=='b' && chars[i+1]=='r')return true;
        }
        return false;
    }


    public String replace(HEWorker worker, String data){
        return data;
    }


    @Override
    public int hashCode() {
        return hs;
    }


    static {
        Brotli4jLoader.ensureAvailability();
    }
}