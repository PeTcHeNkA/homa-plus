package plus.te4net.utl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


public class Te4GZIPOutStream extends DeflaterOutputStream {
    protected CRC32 crc = new CRC32();
    private static final int GZIP_MAGIC = 0x8b1f;
    private static final int TRAILER_SIZE = 8;
    private static final byte OS_UNKNOWN = (byte) 255;


    public Te4GZIPOutStream(OutputStream out, int size) throws IOException {
        this(out, size, -1);
    }


    public Te4GZIPOutStream(OutputStream out, int size, int lvl)
            throws IOException {
        super(out, out != null ? new Deflater(lvl, true) : null,
                size,
                false);

        writeHeader();
        crc.reset();
    }


    public Te4GZIPOutStream(OutputStream out) throws IOException {
        this(out, 512, -1);
    }


    public Te4GZIPOutStream(OutputStream out, boolean syncFlush)
            throws IOException {
        this(out, 512, -1);
    }


    public synchronized void write(byte[] buf, int off, int len)
            throws IOException {
        super.write(buf, off, len);
        crc.update(buf, off, len);
    }


    public void finish() throws IOException {
        if (!def.finished()) {
            def.finish();
            while (!def.finished()) {
                int len = def.deflate(buf, 0, buf.length);
                if (def.finished() && len <= buf.length - TRAILER_SIZE) {
                    writeTrailer(buf, len);
                    len = len + TRAILER_SIZE;
                    out.write(buf, 0, len);
                    return;
                }
                if (len > 0)
                    out.write(buf, 0, len);
            }
            byte[] trailer = new byte[TRAILER_SIZE];
            writeTrailer(trailer, 0);
            out.write(trailer);
        }
    }


    private void writeHeader() throws IOException {
        out.write(new byte[] {
                (byte) GZIP_MAGIC,          // Magic number (short)
                (byte) (GZIP_MAGIC >> 8),   // Magic number (short)
                Deflater.DEFLATED,          // Compression method (CM)
                0,                          // Flags (FLG)
                0,                          // Modification time MTIME (int)
                0,                          // Modification time MTIME (int)
                0,                          // Modification time MTIME (int)
                0,                          // Modification time MTIME (int)
                0,                          // Extra flags (XFLG)
                OS_UNKNOWN                  // Operating system (OS)
        });
    }


    private void writeTrailer(byte[] buf, int offset){
        writeInt((int)crc.getValue(), buf, offset);        // CRC-32 of uncompr. data
        writeInt(def.getTotalIn(), buf, offset + 4); // Number of uncompr. bytes
    }


    private void writeInt(int i, byte[] buf, int offset){
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }


    private void writeShort(int s, byte[] buf, int offset){
        buf[offset] = (byte)(s & 0xff);
        buf[offset + 1] = (byte)((s >> 8) & 0xff);
    }
}