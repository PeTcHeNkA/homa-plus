package plus.fserver.world.save;

import java.io.IOException;
import java.io.RandomAccessFile;


public class ERegionFile {
    private int absX, absZ;
    private final RandomAccessFile raf;
    private byte[] offsets1 = new byte[1024];
    private byte[] offsets2, offsets3;


    public ERegionFile(RandomAccessFile raf) throws IOException {
        this.raf = raf;
        rescanSectors();
    }


    public void close() throws IOException {
        this.raf.close();
    }


    private void rescanSectors() throws IOException {
        long cursor = 0;
        RandomAccessFile raf = this.raf;
        long len = raf.length();

        while (cursor < len){
            raf.seek(cursor);
            readMeta();
            if(MM_isStartChunk()){
                setPos(getRelX(), getRelZ(), (int)(cursor>>9));
            }
            cursor += 512;
        }
    }


    public void write(int x, int z, byte[] bytes) throws IOException {
        int pos = getPos(x, z);
        if(pos != 0){
            invalidate(pos);
        }
        int newPos = write(1, new IOChunkBuf(bytes, bytes.length, x, z));

        if(newPos != pos) setPos(x, z, newPos);
    }


    public byte[] read(int x, int z) throws IOException {
        int pos = getPos(x, z);
        if(pos == 0)return null;
        return read(pos).bytes;
    }


    public int getPos(int x, int z){
        int bPos = bPos(x, z);
        int result = offsets1[bPos] & 255;

        byte[] off = offsets2;
        if(off != null){
            result |= ((off[bPos] & 255) << 8);
        }

        off = offsets3;
        if(off != null){
            result |= ((off[bPos] & 255) << 16);
        }
        return result;
    }


    public void setPos(int x, int z, int value){
        int bPos = bPos(x, z);
        offsets1[bPos] = (byte) (value & 255);
        if(value > 255){
            value >>= 8;
            byte[] off = offsets2;
            if(off == null){
                off = offsets2 = new byte[1024];
            }
            off[bPos] = (byte) (value & 255);

            if(value > 255){
                value >>= 8;
                off = offsets3;
                if(off == null){
                    off = offsets3 = new byte[1024];
                }
                off[bPos] = (byte) (value & 255);
            }
        }
    }


    private static int bPos(int x, int z){
        return (x & 0x1F) << 5 | (z & 0x1F);
    }


    private void invalidate(int pos) throws IOException {
        RandomAccessFile raf = this.raf;
        int cursor = pos;
        raf.seek(cursor*512L);
        readMeta();
        while (!MM_isLastChunk()){
            int cur = cursor;
            cursor = MM_getNextOffset();
            freeMeta();
            raf.seek(cur*512L);
            writeMeta();
            raf.seek(cursor*512L);
            readMeta();
        }
        freeMeta();
        raf.seek(cursor*512L);
        writeMeta();
    }


    private IOChunkBuf read(int pos) throws IOException {
        RandomAccessFile raf = this.raf;

        raf.seek(pos*512L);
        readMeta();
        int x = getRelX();
        int z = getRelZ();

        if(!MM_isStartChunk())throw new RuntimeException("Format error");

        byte[] buf = new byte[MM_size()];
        int bufCursor = 0;

        while (bufCursor != buf.length){

            if(MM_isLastChunk()){
                raf.read(buf, bufCursor, MM_getNextOffset());
                break;
            } else {
                int readed;
                raf.read(buf, bufCursor, readed = (MM_isStartChunk() ? 494 : 508));
                bufCursor += readed;
            }

            raf.seek(MM_getNextOffset()*512L);
            readMeta();
        }

        return new IOChunkBuf(buf, buf.length, x, z);
    }


    private int write(int cursor, IOChunkBuf request) throws IOException {
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
            return cursor < bytes.length;
        }

        void write() throws IOException {
            //raf.seek(raf.getFilePointer()-4);
            if(cursor == 0){
                MM_setStartChunk(true);
                MM_writeStart(length, x, z);
            } else {
                MM_setStartChunk(false);
            }

            int toNextSector = MM_isStartChunk() ? 494 : 508;

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


    private static final int HEAD_BIT1 = 1 << 31;
    private static final int HEAD_BIT2 = 1 << 30;
    private static final int HEAD_BIT3 = ~(HEAD_BIT1 | HEAD_BIT2);
    int mm_meta, mm_size;
    short mm_subData;
    //short toNextSector;

    private void writeMeta() throws IOException {
        raf.writeInt(mm_meta);
        if(MM_isStartChunk()){
            raf.writeShort(mm_subData);
            raf.writeInt(mm_size);
        }
    }


    private void readMetaSafe() throws IOException {
        if(raf.getFilePointer() >= raf.length()){
            mm_meta = 0;
            mm_subData = 0;
            mm_size = 0;
        } else {
            readMeta();
        }
    }

    private void readMeta() throws IOException {
        mm_meta = raf.readInt();
        if(MM_isStartChunk()){
            mm_subData = raf.readShort();
            mm_size = raf.readInt();
        }
    }
    private void freeMeta(){
        mm_meta = 0;
    }
    private int MM_size(){
        return this.mm_size;
    }
    private void MM_writeStart(int size, int x, int z){
        this.mm_size = size;
        this.mm_subData = (short) (((x & 0x1F) << 8) | z & 0x1F);
    }
    private int getRelX(){
        return mm_subData >> 8;
    }
    private int getRelZ(){
        return mm_subData & 0x1F;
    }
    private boolean MM_isFree(){
        return mm_meta == 0;
    }
    private boolean MM_isStartChunk(){
        return (mm_meta & HEAD_BIT1) == HEAD_BIT1;
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



