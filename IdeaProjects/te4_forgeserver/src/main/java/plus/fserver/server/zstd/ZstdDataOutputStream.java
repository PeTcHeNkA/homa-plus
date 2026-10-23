package plus.fserver.server.zstd;

import com.github.luben.zstd.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class ZstdDataOutputStream extends DataOutputStream {
    private ZstdOutputStreamNoFinalizer inner;

    /** @deprecated */
    @Deprecated
    public ZstdDataOutputStream(OutputStream var1, int var2, boolean var3, boolean var4) throws IOException {
        super(var1);
        this.inner = new ZstdOutputStreamNoFinalizer(var1, var2);
        this.inner.setCloseFrameOnFlush(var3);
        this.inner.setChecksum(var4);
    }

    /** @deprecated */
    @Deprecated
    public ZstdDataOutputStream(OutputStream var1, int var2, boolean var3) throws IOException {
        super(var1);
        this.inner = new ZstdOutputStreamNoFinalizer(var1, var2);
        this.inner.setCloseFrameOnFlush(var3);
    }


    public ZstdDataOutputStream(OutputStream var1, int var2) throws IOException {
        this(var1, NoPool.INSTANCE);
        this.inner.setLevel(var2);
    }


    public ZstdDataOutputStream(OutputStream var1) throws IOException {
        this(var1, NoPool.INSTANCE);
    }


    public ZstdDataOutputStream(OutputStream var1, BufferPool var2, int var3) throws IOException {
        this(var1, var2);
        this.inner.setLevel(var3);
    }


    public ZstdDataOutputStream(OutputStream var1, BufferPool var2) throws IOException {
        super(var1);
        this.inner = new ZstdOutputStreamNoFinalizer(var1, var2);
    }


    protected void finalize() throws Throwable {
        this.close();
    }


    public static long recommendedCOutSize() {
        return ZstdOutputStreamNoFinalizer.recommendedCOutSize();
    }

    public ZstdDataOutputStream setChecksum(boolean var1) throws IOException {
        this.inner.setChecksum(var1);
        return this;
    }


    public ZstdDataOutputStream setLevel(int var1) throws IOException {
        this.inner.setLevel(var1);
        return this;
    }


    public ZstdDataOutputStream setLong(int var1) throws IOException {
        this.inner.setLong(var1);
        return this;
    }


    public ZstdDataOutputStream setWorkers(int var1) throws IOException {
        this.inner.setWorkers(var1);
        return this;
    }


    public ZstdDataOutputStream setOverlapLog(int var1) throws IOException {
        this.inner.setOverlapLog(var1);
        return this;
    }


    public ZstdDataOutputStream setJobSize(int var1) throws IOException {
        this.inner.setJobSize(var1);
        return this;
    }


    public ZstdDataOutputStream setTargetLength(int var1) throws IOException {
        this.inner.setTargetLength(var1);
        return this;
    }


    public ZstdDataOutputStream setMinMatch(int var1) throws IOException {
        this.inner.setMinMatch(var1);
        return this;
    }


    public ZstdDataOutputStream setSearchLog(int var1) throws IOException {
        this.inner.setSearchLog(var1);
        return this;
    }


    public ZstdDataOutputStream setChainLog(int var1) throws IOException {
        this.inner.setChainLog(var1);
        return this;
    }


    public ZstdDataOutputStream setHashLog(int var1) throws IOException {
        this.inner.setHashLog(var1);
        return this;
    }


    public ZstdDataOutputStream setWindowLog(int var1) throws IOException {
        this.inner.setWindowLog(var1);
        return this;
    }


    public ZstdDataOutputStream setStrategy(int var1) throws IOException {
        this.inner.setStrategy(var1);
        return this;
    }


    public ZstdDataOutputStream setCloseFrameOnFlush(boolean var1) {
        this.inner.setCloseFrameOnFlush(var1);
        return this;
    }


    public ZstdDataOutputStream setDict(byte[] var1) throws IOException {
        this.inner.setDict(var1);
        return this;
    }


    public ZstdDataOutputStream setDict(ZstdDictCompress var1) throws IOException {
        this.inner.setDict(var1);
        return this;
    }


    @Override
    public void write(byte[] var1, int var2, int var3) throws IOException {
        this.inner.write(var1, var2, var3);
    }


    @Override
    public void write(int var1) throws IOException {
        this.inner.write(var1);
    }


    @Override
    public void flush() throws IOException {
        this.inner.flush();
    }


    @Override
    public void close() throws IOException {
        this.inner.close();
    }
}
