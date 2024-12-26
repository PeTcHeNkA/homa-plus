package plus.fserver.world.save;

import java.io.IOException;
import java.io.RandomAccessFile;


public class ERegionFile {
    private final VarIntBuf free = new VarIntBuf();
    private final RandomAccessFile raf;
    private final byte[] offsets1 = new byte[1024];
    private byte[] offsets2, offsets3;

    public ERegionFile(RandomAccessFile raf) throws IOException {
        this.raf = raf;
        rescanSectors();
    }


    public synchronized void close() throws IOException {
        this.raf.close();
    }


    private synchronized void rescanSectors() throws IOException {
        long cursor = 0;
        RandomAccessFile raf = this.raf;
        VarIntBuf free = this.free;
        long len = raf.length();
        while (cursor < len){
            raf.seek(cursor);
            readMeta();
            if(MM_isStartChunk()){
                setPos(MM_getRelX(), MM_getRelZ(), (int)(cursor>>9));
            } else if(MM_isFree()){
                free.add((int)(cursor>>9));
            }
            cursor += 512;
        }
    }


    public synchronized void write(int x, int z, byte[] bytes) throws IOException {
        write(x, z, bytes, bytes.length);
    }


    public synchronized void write(int x, int z, byte[] bytes, int length) throws IOException {
        int pos, newPos;

        if((pos = getPos(x, z)) != -1)
            invalidate(pos);
        if(bytes == null){
            if(pos != -1) setPos(x, z, -1);
            return;
        }
        if((newPos = write(new IOChunkBuf(bytes, length, x, z))) != pos)
            setPos(x, z, newPos);
    }


    public synchronized byte[] read(int x, int z) throws IOException {
        int pos;
        if((pos = getPos(x, z)) == -1)return null;
        return read(pos).bytes;
    }


    public int getPos(int x, int z){
        int bPos;
        int result = offsets1[bPos = bPos(x, z)] & 255;
        byte[] off;
        if((off = offsets2) != null){
            result |= ((off[bPos] & 255) << 8);

            if((off = offsets3) != null)
                result |= ((off[bPos] & 255) << 16);
        }
        return --result;
    }


    public void setPos(int x, int z, int value){
        ++value;
        int bPos = bPos(x, z);
        offsets1[bPos] = (byte) (value & 255);
        if(value > 255){
            byte[] off;
            if((off = offsets2) == null)
                off = offsets2 = new byte[1024];

            off[bPos] = (byte) ((value >>= 8) & 255);

            if(value > 255){
                if((off = offsets3) == null)
                    off = offsets3 = new byte[1024];

                off[bPos] = (byte) ((value >> 8) & 255);
            }
        }
    }


    private static int bPos(int x, int z){
        return (x & 0x1F) << 5 | (z & 0x1F);
    }


    private void invalidate(int pos) throws IOException {
        RandomAccessFile raf = this.raf;
        VarIntBuf free = this.free;
        int cursor = pos;
        raf.seek(cursor*512L);
        readMeta();
        free.add(pos);

        while (!MM_isLastChunk()){
            if(MM_isFree())break;
            int cur = cursor;
            cursor = MM_getNextOffset();
            free.add(cursor);
            MM_free();
            raf.seek(cur*512L);
            writeMeta();
            raf.seek(cursor*512L);
            readMeta();
        }
        MM_free();
        raf.seek(cursor*512L);
        writeMeta();
    }


    private IOChunkBuf read(int pos) throws IOException {
        RandomAccessFile raf = this.raf;

        raf.seek(pos*512L);
        readMeta();
        if(!MM_isStartChunk())throw new RuntimeException("Format error");
        //int read = 494; //start offset

        int x = MM_getRelX(), z = MM_getRelZ(), bufCursor = 0;

        byte[] buf = new byte[MM_size()];

        while (bufCursor != buf.length){
            int pre = MM_nonMeta2Buf(buf, bufCursor);
            bufCursor += pre;
            if(MM_isLastChunk()){
                raf.read(buf, bufCursor, MM_getNextOffset()-pre);
                break;
            } else {
                raf.read(buf, bufCursor, 502);
                bufCursor += 502;
            }
            raf.seek(MM_getNextOffset()*512L);
            readMeta();
        }

        return new IOChunkBuf(buf, buf.length, x, z);
    }


    private int write(IOChunkBuf request) throws IOException {
        VarIntBuf free = this.free;
        RandomAccessFile raf = this.raf;
        int prevCursor = -1;
        int head = -1;
        boolean canNext = true;
        int cursor = 0;

        while (request.needWrite()){
            boolean read = true;
            if(canNext){
                cursor = free.remMax();
                if(cursor == -1) {
                    cursor = (int)(raf.length()/512L);
                    read = false;
                    MM_free();
                    canNext = false;
                }
            }
            if(read) {
                raf.seek(cursor*512L);
                readMeta();
            }
            if(MM_isFree()){
                if(head == -1)head = cursor;

                if(request.isSingle()) {
                    raf.seek(cursor * 512L);
                    request.write(prevCursor);
                    break;
                } else {
                    if(prevCursor != -1){
                        raf.seek(prevCursor * 512L);
                        request.write(cursor);
                        if(request.needLast()){
                            raf.seek(cursor * 512L);
                            request.write(-1);
                        }
                    }
                }
                prevCursor = cursor;
            }
            ++cursor;
        }
        return head;
    }


    private class IOChunkBuf {
        byte[] bytes;
        int cursor, length, x, z;

        private IOChunkBuf(byte[] bytes, int length, int x, int z) {
            this.bytes = bytes;
            this.length = length;
            this.x = x;
            this.z = z;
        }


        boolean needWrite(){
            return cursor < length;
        }


        boolean needLast(){
            return length - cursor <= (cursor == 0 ? 502:508);
        }


        boolean isSingle(){
            return length <= 502;
        }


        void write(int next) throws IOException {
            int toNextSector;
            if(cursor == 0){
                toNextSector = 502;
                MM_setStartChunk(true);
                MM_writeStart(length, x, z);
            } else {
                toNextSector = 508;
                MM_setStartChunk(false);
            }
            int count = length - cursor;

            if(count > toNextSector){
                count = toNextSector;
                MM_setLastChunk(false);
                MM_setNextOffset(next);
            } else {
                MM_setLastChunk(true);
                MM_setNextOffset(count);
            }
            int min = MM_isStartChunk()?10:4;

            int cursor = this.cursor;
            if(cursor >= min){
                byte[] temp = new byte[min];
                System.arraycopy(bytes, cursor-min, temp, 0, min);
                System.arraycopy(mm_metb, 0, bytes, cursor-min, min);
                raf.write(bytes, cursor-min, count+min);
                System.arraycopy(temp, 0, bytes, cursor-min, min);
            } else {
                writeMeta();
                raf.write(bytes, cursor, count);
            }
            this.cursor += count;
        }
    }


    //Metadata
    byte[] mm_metb = new byte[10];

    private void writeMeta() throws IOException {
        if(MM_isStartChunk())
            raf.write(mm_metb, 0, 10);
        else
            raf.write(mm_metb, 0, 4);
    }
    private void readMeta() throws IOException {
        byte[] m;
        int count = raf.read(m = this.mm_metb);
        if(count == -1) for (int i = 0; i < 10; i++) m[i] = 0;
    }


    private int MM_nonMeta2Buf(byte[] buf, int cursor){
        byte[] m = this.mm_metb;
        if(MM_isStartChunk()){
            return 0;
        } else {
            buf[cursor  ] = m[4];
            buf[cursor+1] = m[5];
            buf[cursor+2] = m[6];
            buf[cursor+3] = m[7];
            buf[cursor+4] = m[8];
            buf[cursor+5] = m[9];
            return 6;
        }
    }

    private void MM_free(){
        byte[] m = this.mm_metb;
        for (int i = 0; i < 10; i++) m[i] = 0;
    }
    private int MM_size(){
        byte[] m = this.mm_metb;
        return (((m[6] & 255) << 24) + ((m[7] & 255) << 16) + ((m[8] & 255) << 8) + (m[9] & 255));
    }
    private void MM_writeStart(int v, int x, int z){
        byte[] m = this.mm_metb;
        m[4] = (byte) (x & 0x1F);
        m[5] = (byte) (z & 0x1F);
        m[6] = (byte) ((v >>> 24) & 0xFF);
        m[7] = (byte) ((v >>> 16) & 0xFF);
        m[8] = (byte) ((v >>>  8) & 0xFF);
        m[9] = (byte) ((v)        & 0xFF);
    }
    private int MM_getRelX(){
        return mm_metb[4];
    }
    private int MM_getRelZ(){
        return mm_metb[5];
    }
    private boolean MM_isFree(){
        byte[] m = this.mm_metb;
        for (int i = 0; i < 4; i++) if(m[i] != 0)return false;
        return true;
    }
    private boolean MM_isStartChunk(){
        return (mm_metb[0] & -128) == -128;
    }
    private void MM_setStartChunk(boolean b){
        if(b) mm_metb[0] |= -128;
        else mm_metb[0] &= ~-128;
    }
    private boolean MM_isLastChunk(){
        return (mm_metb[0] & 64) == 64;
    }
    private void MM_setLastChunk(boolean b){
        if(b) mm_metb[0] |= 64;
        else mm_metb[0] &= ~64;
    }
    private int MM_getNextOffset(){
        byte[] m = this.mm_metb;
        return (((m[0] & 63) << 24) + ((m[1] & 255) << 16) + ((m[2] & 255) << 8) + (m[3] & 255));
    }
    private void MM_setNextOffset(int v){
        byte[] m = this.mm_metb;
        m[0] = (byte) ((m[0] & -64) | ((v >>> 24) & 0xFF));
        m[1] = (byte) ((v >>> 16) & 0xFF);
        m[2] = (byte) ((v >>>  8) & 0xFF);
        m[3] = (byte) ((v)        & 0xFF);
    }
}