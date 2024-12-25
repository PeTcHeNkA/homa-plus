package plus.fserver.world.save;

import java.io.IOException;
import java.io.RandomAccessFile;


public class ERegionFile {
    private final RandomAccessFile raf;
    private final byte[] offsets1 = new byte[1024];
    private byte[] offsets2, offsets3;
    private short cursor = 0;

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
        long len = raf.length();
        boolean firstFree = true;

        while (cursor < len){
            raf.seek(cursor);
            readMeta();
            if(MM_isStartChunk()){
                setPos(MM_getRelX(), MM_getRelZ(), (int)(cursor>>9));
            }
            else if(firstFree){
                this.cursor = (short) (((int)(cursor>>9)) & 0xFFFF);
                firstFree = false;
            }
            cursor += 512;
        }
    }


    public synchronized void write(int x, int z, byte[] bytes) throws IOException {
        write(x, z, bytes, bytes.length);
    }


    public synchronized void write(int x, int z, byte[] bytes, int length) throws IOException {
        int pos, newPos;
        if((pos = getPos(x, z)) != -1) invalidate(pos);
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
        int minCursor = this.cursor;
        if(pos < minCursor)minCursor = pos;
        int cursor = pos;
        raf.seek(cursor*512L);
        readMeta();
        while (!MM_isLastChunk()){
            int cur = cursor;
            cursor = MM_getNextOffset();
            if(cursor < minCursor)minCursor = cursor;
            MM_free();
            raf.seek(cur*512L);
            writeMeta();
            raf.seek(cursor*512L);
            readMeta();
        }
        this.cursor = (short) minCursor;
        MM_free();
        raf.seek(cursor*512L);
        writeMeta();
    }


    private IOChunkBuf read(int pos) throws IOException {
        RandomAccessFile raf = this.raf;

        raf.seek(pos*512L);
        readMeta();
        if(!MM_isStartChunk())throw new RuntimeException("Format error");
        int read = 494; //start offset

        int x = MM_getRelX(), z = MM_getRelZ(), bufCursor = 0;

        byte[] buf = new byte[MM_size()];

        while (bufCursor != buf.length){

            if(MM_isLastChunk()){
                raf.read(buf, bufCursor, MM_getNextOffset());
                break;
            } else {
                raf.read(buf, bufCursor, read);
                bufCursor += read;
                read = 508; //next offset
            }

            raf.seek(MM_getNextOffset()*512L);
            readMeta();
        }

        return new IOChunkBuf(buf, buf.length, x, z);
    }


    private int write(IOChunkBuf request) throws IOException {
        int cursor = this.cursor;
        RandomAccessFile raf = this.raf;
        int prevMeta = -1;
        int head = -1;

        while (request.needWrite()){
            raf.seek(cursor*512L);
            readMetaSafe();

            if(MM_isFree()){
                if(prevMeta != -1){
                    raf.seek(prevMeta*512L);
                    readMeta();
                    MM_setNextOffset(cursor);
                    raf.seek(prevMeta*512L);
                    writeMeta();
                    raf.seek(cursor*512L);
                } else {
                    head = cursor;
                    raf.seek(cursor*512L);
                }

                request.write();
                prevMeta = cursor;
            }
            ++cursor;
        }
        this.cursor = (short)(cursor & 0xFFFF);
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


        void write() throws IOException {
            int toNextSector;
            if(cursor == 0){
                toNextSector = 494;
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
            } else {
                MM_setLastChunk(true);
                MM_setNextOffset(count);
            }
            writeMeta();

            raf.write(bytes, cursor, count);

            cursor += count;
        }
    }


    //Metadata
    private static final int HEAD_BIT1 = 1 << 31;
    private static final int HEAD_BIT2 = 1 << 30;
    private static final int HEAD_BIT3 = ~(HEAD_BIT1 | HEAD_BIT2);
    private int mm_meta, mm_size;
    private short mm_subData;

    private void writeMeta() throws IOException {
        raf.writeInt(mm_meta);
        if(MM_isStartChunk()){
            raf.writeShort(mm_subData);
            raf.writeInt(mm_size);
        }
    }


    private void readMetaSafe() throws IOException {
        RandomAccessFile raf;
        if((raf = this.raf).getFilePointer() >= raf.length()){
            mm_meta = 0;
            mm_subData = 0;
            mm_size = 0;
        } else {
            readMeta();
        }
    }

    private void readMeta() throws IOException {
        RandomAccessFile raf;
        int meta = mm_meta = (raf = this.raf).readInt();
        if(MM_isStartChunk(meta)){
            mm_subData = raf.readShort();
            mm_size    = raf.readInt();
        }
    }
    private void MM_free(){
        mm_meta = 0;
    }
    private int MM_size(){
        return this.mm_size;
    }
    private void MM_writeStart(int size, int x, int z){
        this.mm_size = size;
        this.mm_subData = (short) (((x & 0x1F) << 8) | z & 0x1F);
    }
    private int MM_getRelX(){
        return mm_subData >> 8;
    }
    private int MM_getRelZ(){
        return mm_subData & 0x1F;
    }
    private boolean MM_isFree(){
        return mm_meta == 0;
    }
    private boolean MM_isStartChunk(){
        return (mm_meta & HEAD_BIT1) == HEAD_BIT1;
    }
    private static boolean MM_isStartChunk(int meta){
        return (meta & HEAD_BIT1) == HEAD_BIT1;
    }
    private void MM_setStartChunk(boolean b){
        if(b) mm_meta |= HEAD_BIT1;
        else mm_meta &= ~HEAD_BIT1;
    }
    private boolean MM_isLastChunk(){
        return (mm_meta & HEAD_BIT2) == HEAD_BIT2;
    }
    private void MM_setLastChunk(boolean b){
        if(b) mm_meta |= HEAD_BIT2;
        else mm_meta &= ~HEAD_BIT2;
    }
    private int MM_getNextOffset(){
        return mm_meta & HEAD_BIT3;
    }
    private void MM_setNextOffset(int value){
        mm_meta = (mm_meta & (HEAD_BIT1 | HEAD_BIT2)) | value & HEAD_BIT3;
    }
}