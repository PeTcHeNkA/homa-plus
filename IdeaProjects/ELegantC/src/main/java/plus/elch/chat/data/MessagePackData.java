package plus.elch.chat.data;

import plus.elch.chat.ElMessage;
import plus.elch.utl.FastExitException;
import plus.elch.utl.IoUtils;
import java.io.*;


//todo fix
public class MessagePackData {
    private long fromId   = Long.MAX_VALUE, toId   = Long.MIN_VALUE;
    private long fromTime = Long.MAX_VALUE, toTime = Long.MIN_VALUE;

    private ElMessage[] cachedMessages = new ElMessage[64];
    private int countMessages;
    private int needSaveMessages = 0;
    private boolean saveMeta;
    private final RandomAccessFile file;

    public MessagePackData(File file) throws IOException, FastExitException {
        this(new RandomAccessFile(file, "rw"));
    }


    public MessagePackData(RandomAccessFile file) throws IOException, FastExitException {
        this.file = file;

        byte[] meta = new byte[32];
        file.seek(0);
        file.read(meta);
        ByteArrayInputStream is = new ByteArrayInputStream(meta);

        fromId   = IoUtils.readLong(is);
        toId     = IoUtils.readLong(is);
        fromTime = IoUtils.readLong(is);
        toTime   = IoUtils.readLong(is);
    }


    public synchronized void updateMeta() throws IOException {
        long curCursor = file.getFilePointer();
        file.seek(0);

        ByteArrayOutputStream os = new ByteArrayOutputStream(32);
        IoUtils.writeLong(os, fromId);
        IoUtils.writeLong(os, toId);
        IoUtils.writeLong(os, fromTime);
        IoUtils.writeLong(os, toTime);

        file.write(os.toByteArray());
        file.seek(curCursor);
    }


    public synchronized void addMessage(ElMessage message) {
        if (cachedMessages.length == countMessages) {
            ElMessage[] tmp = new ElMessage[cachedMessages.length + 64];
            System.arraycopy(cachedMessages, 0, tmp, 0, countMessages);
            cachedMessages = tmp;
        }
        cachedMessages[countMessages++] = message;
        ++needSaveMessages;

        if(fromId > message.getId()) {
            fromId   = message.getId();
            fromTime = message.getTime();
            saveMeta = true;
        }
        if(toId < message.getId()) {
            toId = message.getId();
            toTime = message.getTime();
            saveMeta = true;
        }
    }


    public synchronized void close() throws IOException, FastExitException {
        flush();
        file.close();
    }


    public synchronized void flush() throws IOException, FastExitException {
        if (needSaveMessages > 0) {
            ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            for (int i = 0; i < needSaveMessages; i++) {
                ElMessage message = cachedMessages[countMessages + i];
                message.write(os);
            }
            file.write(os.toByteArray());
            needSaveMessages = 0;

            if(cachedMessages.length > 256) {
                cachedMessages = new ElMessage[cachedMessages.length-64];
            }
        }
        if(saveMeta) {
            updateMeta();
            saveMeta = false;
        }
    }
}