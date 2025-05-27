package plus.proto;


import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.proto.lang.TableData;
import plus.tson.TsonFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;


public class LangPackV2<T extends LangPackV2.Line> {
    private Line[] lines;
    private HashMap<String, Object> index = new HashMap<>();
    private Int2ObjectOpenHashMap<Line> idIndex = new Int2ObjectOpenHashMap<>();
    private int dist = 10;
    private boolean sect = true;

    public void read(File file, boolean readIndex) throws InvalidProtocolBufferException {
        byte[] data = TsonFile.readFile(file);
        int len = readInt(data);

        byte[] chunk1 = copy(data, 4, len);
        byte[] chunk2 = copy(data, len + 4, data.length - (len + 4));

        if(readIndex) {
            TableData.Index index = TableData.Index.parseFrom(chunk1);
            dist = index.getDist();
            sect = index.getSect();
        }

        TableData.Translations tabData = TableData.Translations.parseFrom(chunk2);

        lines = readLines(tabData);

        makeIndex();
    }


    public void write(File file) throws Exception {
        WriteCallback callback = new WriteCallback();
        ByteArrayOutputStream tableOut = new ByteArrayOutputStream();
        CodedOutputStream out = CodedOutputStream.newInstance(tableOut);

        TableData.Index.Builder indexBuilder = TableData.Index.newBuilder();
        indexBuilder.setDist(dist).setSect(sect);

        int cursor = 0;

        while (cursor < lines.length){
            writeNext10(cursor, lines, out, callback);

            cursor += callback.count;

            TableData.IndexItem itm = TableData.IndexItem.newBuilder()
                    .setBeginOff(callback.startOff)
                    .setLength(callback.bLen)
                    .build();

            indexBuilder.putIndex(callback.index, itm);
        }
        out.flush();

        byte[] indexBytes = indexBuilder.build().toByteArray();
        byte[] tableBytes = tableOut.toByteArray();

        byte[] all = new byte[indexBytes.length + tableBytes.length + 4];
        writeInt(all, indexBytes.length);

        System.arraycopy(indexBytes, 0, all, 4, indexBytes.length);
        System.arraycopy(tableBytes, 0, all, 4 + indexBytes.length, tableBytes.length);

        TsonFile.write(file, all);
    }


    public static void writeNext10(int from, Line[] lines, CodedOutputStream out, WriteCallback c) throws IOException {
        c.startOff = out.getTotalBytesWritten();

        int indexId = lines[from].id / 10;
        int count = 0;

        for (int i = from; i < from + 10 && i < lines.length; i++){
            Line line = lines[i];
            if (line.id / 10 != indexId) break;

            out.writeMessage(1, TableData.Item.newBuilder().setId(line.id).setName(line.getActual()).build());
            count += 1;
        }

        c.index    = indexId;
        c.count    = count;

        c.bLen = out.getTotalBytesWritten() - c.startOff;
    }


    public static class WriteCallback{
        int index;
        int count    = 0;
        int bLen = 0;
        int startOff = 0;
    }


    private void makeIndex(){
        HashMap<String, Object> index = this.index;
        Int2ObjectOpenHashMap<Line> idIndex = this.idIndex;

        index.clear();
        for (Line line : lines){
            idIndex.put((int)line.id, line);
            Object prev = index.put(line.src, line);
            if(prev != null){
                if (prev instanceof Line){
                    Line prevLine = (Line) prev;
                    index.put(line.src, new Line[]{prevLine, line});
                } else {
                    Line[] prevLines = (Line[]) prev;
                    prevLines = Arrays.copyOf(prevLines, prevLines.length + 1);
                    prevLines[prevLines.length - 1] = line;
                    index.put(line.src, prevLines);
                }
            }
        }
    }


    public Line[] readLines(TableData.Translations translations) {
        Line[] result = new Line[translations.getDataCount()];
        for (int i = 0; i < translations.getDataCount(); i++) {
            TableData.Item data = translations.getData(i);
            result[i] = createLine((int)data.getId(), i, data.getName());
        }
        return result;
    }


    public T createLine(int id, int index, String src){
        return (T)new Line(id, index, src);
    }


    public T getLineById(int id){
        return (T)idIndex.get(id);
    }


    public T getLineByIndex(int index){
        return (T)lines[index];
    }


    public int size(){
        return lines.length;
    }


    static byte[] copy(byte[] data, int from, int len){
        byte[] result = new byte[len];
        System.arraycopy(data, from, result, 0, len);
        return result;
    }


    public static void writeInt(byte[] buf, int data) {
        buf[3] = (byte) ((data >> 24) & 0xff);
        buf[2] = (byte) ((data >> 16) & 0xff);
        buf[1] = (byte) ((data >> 8) & 0xff);
        buf[0] = (byte) (data & 0xff);
    }


    public static int readInt(byte[] data){
        return    (data[3] & 0xff) << 24
                | (data[2] & 0xff) << 16
                | (data[1] & 0xff) << 8
                | (data[0] & 0xff);
    }


    public static class Line implements Comparable<Line> {
        public final int id, index;
        public final String src;
        public String actual;

        public Line(int id, int index, String src) {
            this.id = id;
            this.index = index;
            this.src = src;
        }


        @Override
        public int compareTo(Line o) {
            return Long.compare(id, o.id);
        }


        public String getActual(){
            String actual = this.actual;
            if(actual == null)
                return src;
            return actual;
        }
    }


    public Line[] getLines(String key){
        Object value = index.get(key);
        if (value == null)return null;
        if (value instanceof Line) return new Line[]{(Line) value};
        return (Line[]) value;
    }


    public Line getLine(String key){
        Line[] lines = getLines(key);
        if (lines == null)return null;
        return lines[0];
    }


    public boolean setLine(String key, String value){
        Object value1 = index.get(key);
        if(value1 == null) {
            return false;
        }
        if (value1 instanceof Line){
            Line line = (Line) value1;
            line.actual = value;
        } else {
            Line[] lines = (Line[]) value1;
            for (Line line : lines)
                line.actual = value;
        }
        return true;
    }
}