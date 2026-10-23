package plus.fserver.server.zstd;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class ZStdCoder {
    private final int pressLvl, keySize;
    private byte[] keyBuf;
    private ArrayList<byte[]> samples;

    public ZStdCoder(){
        this.keySize = 256;
        pressLvl = 3;
    }

    public ZStdCoder(int keySize, int pressLvl){
        this.keySize = keySize;
        this.pressLvl = pressLvl;
    }


    public void feed(Object... samples){
        if(this.samples ==null) this.samples = new ArrayList<>();
        for(Object obj:samples)
            this.samples.add(obj.toString().getBytes());
    }


    public void feed(byte[] bytes){
        if(samples==null)samples = new ArrayList<>();
        samples.add(bytes);
    }


    public void feed(byte[]... samples){
        for(byte[] bytes:samples) feed(bytes);
    }


    public ZStdCoder clean(){
        samples = null;
        return this;
    }


    public ZStdCoder cleanAll(){
        samples = null;
        keyBuf = null;
        return this;
    }


    public int samples(){
        return samples==null?0:samples.size();
    }


    public int totalSize(){
        if(samples==null||samples.isEmpty())return 0;
        int sum = 0;
        for (int i=0;i<samples.size();i++){
            sum += samples.get(i).length;
        }
        if(keyBuf != null) sum += keyBuf.length;
        return sum;
    }


    public ZStdCoder loadKey(byte[] data){
        if(data==null || data.length == 0){
            keyBuf = null;
            return this;
        }
        if(data.length != keySize)
            throw new IllegalArgumentException("Key should be ["+keySize+"], not ["+data.length+"] (current key size)");
        if(keyBuf ==null){
            keyBuf = new byte[keySize];
        }
        System.arraycopy(data, 0, keyBuf, 0, data.length);
        return this;
    }


    public byte[] dumpKey(){
        if(keyBuf ==null)return null;
        byte[] data = new byte[keySize];
        System.arraycopy(keyBuf, 0, data, 0, data.length);
        return data;
    }


    public InputStream decompress(InputStream src) throws IOException {
        ZstdInputStream stream = new ZstdInputStream(src);
        if(keyBuf != null)
            stream.setDict(keyBuf);
        return stream;
    }


    public OutputStream compress(OutputStream src) throws IOException {
        ZstdOutputStream stream = new ZstdOutputStream(src, pressLvl, true, false);
        if(keyBuf != null)
            stream.setDict(keyBuf);
        return stream;
    }


    public byte[] compressStr(String msg) throws IOException {
        return compress(msg.getBytes(StandardCharsets.UTF_8));
    }


    public byte[] compress(byte... src) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream stream1 = compress(out);
        stream1.write(src);
        stream1.close();
        return out.toByteArray();
    }


    public byte[] decompress(byte... src) throws IOException {
        InputStream in = decompress(new ByteArrayInputStream(src));
        byte[] res = IOUtils.toByteArray(in);
        in.close();
        return res;
    }


    public String decompressStr(byte...cMsg) throws IOException {
        return new String(decompress(cMsg), StandardCharsets.UTF_8);
    }


    private static void write4b(int offset, int i3b, byte[] bytes){
        bytes[offset++] = (byte) (i3b>>24 & 255);
        bytes[offset++] = (byte) (i3b>>16 & 255);
        bytes[offset++] = (byte) (i3b>>8  & 255);
        bytes[offset]   = (byte) (i3b     & 255);
    }


    private static int from4b(int offset, byte... bytes){
        return (bytes[offset++]&255)<<24 | (bytes[offset++]&255)<<16 | (bytes[offset++]&255)<<8 | (bytes[offset]&255);
    }


    public ZStdCoder train(){
        if(samples==null||samples.size()<=10){
            throw new RuntimeException("Samples should be more 10!");
        }
        if(keyBuf ==null){
            keyBuf = new byte[keySize];
        }
        Zstd.trainFromBuffer(samples.toArray(new byte[0][0]), keyBuf);
        return this;
    }
}