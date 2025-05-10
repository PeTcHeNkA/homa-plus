package plus.proto;

import com.google.protobuf.InvalidProtocolBufferException;
import plus.proto.lang.TableData;
import plus.tson.TsonFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Objects;


public class LangPack {
    private byte[] header;
    private TableData.Data table;
    private HashMap<String,Node> replaceRules = new HashMap<>();

    public LangPack(){}


    public void read(File file){
        byte[] data = TsonFile.readFile(file);

        int shift = 8;
        byte[] data2 = new byte[data.length - shift];
        byte[] pre   = new byte[shift];

        System.arraycopy(data, shift, data2, 0, data2.length);
        System.arraycopy(data, 0, pre, 0, shift);

        try {
            table = TableData.Data.parseFrom(data2);
            header = pre;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }


    public void write(File file){
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            header[0] = (byte) 0xff;
            header[1] = (byte) 0xff;

            out.write(header);
            table.writeTo(out);
            TsonFile.write(file, out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static class Node{
        int id;
        String value;

        public Node(int id, String value) {
            this.id = id;
            this.value = value;
        }

        void set(int id, String value){
            this.id = id;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;
            return id == node.id && value.equals(node.value);
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + value.hashCode();
            return result;
        }
    }


    public void replace(int id, String key, String value){
        Node node = new Node(id, value);
        replaceRules.put(key, node);
    }


    public void replace(String key, String value){
        Node node = new Node(-1, value);
        replaceRules.put(key, node);
    }


    public byte[] findData(int id){
        for (int i = 0, len = table.getBdatCount(); i < len; i++){
            TableData.BItem item = table.getBdat(i);
            if (item.getId() == id){
                return item.getData().toByteArray();
            }
        }
        return null;
    }


    public void applyChanges(){
        TableData.Data prevTable = this.table;
        TableData.Data.Builder newTable = TableData.Data.newBuilder();

        for (int i = 0, len = prevTable.getBdatCount(); i < len; i++){
            TableData.BItem item = prevTable.getBdat(i);
            newTable.addBdat(i, item);
        }

        //Node temp = new Node(-1, null, null);
        HashMap<String,Node> replaceRules = this.replaceRules;

        for (int i = 0, len = prevTable.getItemCount(); i < len; i++){
            TableData.Item item = prevTable.getItem(i);

            Node node = replaceRules.get(item.getName());

            if (node != null && (node.id == -1 || node.id == item.getId())){
                item = item.toBuilder()
                        .setId(node.id)
                        .setName(node.value)
                        .build();
            }

            newTable.addItem(i, item);
        }

        this.table = newTable.build();
    }
}