package plus.proto.clo;

import com.google.protobuf.CodedOutputStream;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import plus.proto.LangPackV2;
import plus.proto.lang.TableData;
import plus.proto.tparsers.IntConsumer;
import plus.proto.tparsers.IntFuncExecutor;
import plus.tson.TsonFile;
import plus.tson.utl.Te4HashSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static plus.proto.LangPackV2.*;


public class CloData implements IntFuncExecutor {
    private int dist = 10;
    private boolean sect = true;

    //private TableData.ClothesU2 data;
    private TableData.ClothesU2.Builder dataB;

    @Override
    public void readFrom(File file) throws Exception {
        byte[] data = TsonFile.readFile(file);
        int len = readInt(data);

        byte[] chunk1 = copy(data, 4, len);
        byte[] chunk2 = copy(data, len + 4, data.length - (len + 4));
        onReadFrom(chunk2);

        //reaUsongIndex(chunk1, chunk2);

        TableData.Index index = TableData.Index.parseFrom(chunk1);
        dist = index.getDist();
        sect = index.getSect();

        //System.out.println(index);
    }

    private void reaUsongIndex(byte[] chunk1, byte[] chunk2) throws Exception {
        TableData.Index index = TableData.Index.parseFrom(chunk1);

        ArrayList list0 = new ArrayList<>(index.getIndexMap().entrySet());
        ArrayList<Map.Entry<Object, TableData.IndexItem>> list = list0;

        list.sort(new Comparator<Map.Entry<Object, TableData.IndexItem>>() {
            @Override
            public int compare(Map.Entry<Object, TableData.IndexItem> o1, Map.Entry<Object, TableData.IndexItem> o2) {
                return Integer.compare((int) o1.getValue().getBeginOff(), (int) o2.getValue().getBeginOff());
            }
        });

        for (Map.Entry<Object, TableData.IndexItem> e:list){
            TableData.IndexItem item = e.getValue();

            byte[] data = new byte[(int) item.getLength()];
            System.arraycopy(chunk2, (int)item.getBeginOff(), data, 0, data.length);

            TableData.ClothesU2 data1 = TableData.ClothesU2.parseFrom(data);

            System.out.println("\nIDX:"+e.getKey()+" Offset: "+item.getBeginOff()+", Length: "+data.length);
            System.out.println("-----------------------------------------------");
            System.out.println(data1);
        }
    }


    @Override
    public void onReadFrom(byte[] data) throws Exception {
        TableData.ClothesU2 data1 = TableData.ClothesU2.parseFrom(data);
        this.dataB = data1.toBuilder();
    }

    //todo
    @Override
    public void apply(IntConsumer consumer) {}


    public CharacterSkinData search(String key){
        LinkedList<LItem> list = null;
        int count = dataB.getItemCount();
        for (int i = 0; i < count; i++){
            TableData.ClotheUItem item = dataB.getItem(i);
            if(item.getSki1().startsWith(key)){
                if(list == null){
                    list = new LinkedList<>();
                }
                list.add(new LItem(i, item));
            }
        }
        if(list == null)return null;
        return new CharacterSkinData(list);
    }


    public Set<String> scanNames(){
        Set<String> names = new Te4HashSet<>();
        int count = dataB.getItemCount();
        for (int i = 0; i < count; i++){
            TableData.ClotheUItem item = dataB.getItem(i);
            String rawName = item.getSki1();

            if(rawName.endsWith("R")){
                if(rawName.endsWith("SSR")){
                    names.add(rawName.substring(0, rawName.length()-3));
                } else {
                    if(rawName.endsWith("SR")){
                        names.add(rawName.substring(0, rawName.length()-2));
                    } else {
                        names.add(rawName.substring(0, rawName.length()-1));
                    }
                }
            }
        }
        return names;
    }


    public final class LItem{
        public final int index;
        private TableData.ClotheUItem item;

        private LItem(int index, TableData.ClotheUItem item) {
            this.index = index;
            this.item = item;
        }

        public TableData.ClotheUItem get(){
            return item;
        }

        public void set(TableData.ClotheUItem item0){
            this.item = item0;
            dataB.setItem(index, item);
        }

        public void copySkinOf(LItem item0){
            TableData.ClotheUItem input = item0.item;

            TableData.ClotheUItem.Builder item = this.item.toBuilder();

            item.setModelId(input.getModelId());
            item.setSki1(input.getSki1());
            item.setSki2(input.getSki2());
            item.setSki3(input.getSki3());
            item.setSki4(input.getSki4());
            item.setSki5(input.getSki5());
            item.setSki6(input.getSki6());
            item.setSki7(input.getSki7());
            item.setSki8(input.getSki8());

            set(item.build());
        }

        @Override
        public String toString() {
            return item.toString();
        }
    }


    public static final class CharacterSkinData{
        private final List<LItem> items;

        private CharacterSkinData(List<LItem> items) {
            this.items = items;
        }

        public IntList getOptions(){
            return CloData.getOptions(items);
        }

        public boolean modifySkin(int to){
            return modifySkins(items, to);
        }

        public void modifySkin(LItem item){
            getMain().copySkinOf(item);
        }

        public LItem getSkin(int num){
            if (num == 0)return getMain();
            String tos = Integer.toString(num);
            for (LItem item: items){
                if(item.item.getSki1().endsWith(tos)){
                    return item;
                }
            }
            return null;
        }

        public List<LItem> getAll(){
            return items;
        }

        public LItem getMain(){
            for (LItem item: items){
                if(item.item.getSki1().endsWith("R")){
                    return item;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return toString(true);
        }


        public String toString(boolean trimName) {
            LItem main = getMain();
            if(main == null)return "UNKNOWN DATA";
            String name = main.item.getSki1();
            if(trimName){
                if(name.endsWith("R")){
                    if(name.endsWith("SSR")){
                        name = name.substring(0, name.length()-3);
                    } else {
                        if(name.endsWith("SR")){
                            name = name.substring(0, name.length()-2);
                        } else {
                            name = name.substring(0, name.length()-1);
                        }
                    }
                }
            }

            String pre = "ID:" +main.item.getId()+", name: "+ name;
            IntList options = getOptions();
            if(options.isEmpty()){
                pre += ", no options";
            } else {
                pre += ", options: "+options;
            }
            return pre;
        }
    }


    @Override
    public String toString() {
        return dataB.toString();
    }


    public static IntList getOptions(List<LItem> items){
        IntList list = new IntArrayList();
        for (LItem item: items){
            String str = item.item.getSki1();
            if(str.endsWith("R")){
                continue;
            }
            try {
                int num = Integer.valueOf(""+str.charAt(str.length()-1));
                if(list.isEmpty()){
                    list.add(0);
                }
                list.add(num);
            } catch (Exception e){};
        }
        return list;
    }


    public static boolean modifySkins(List<LItem> items, int to){
        String tos = Integer.toString(to);
        LItem main = null, src = null;
        for (LItem item: items){
            if(item.item.getSki1().endsWith("R")){
                main = item;
            }
            if(item.item.getSki1().endsWith(tos)){
                src = item;
            }
        }
        if(main == null || src == null)return false;

        main.copySkinOf(src);

        return true;
    }


    public void write(File file) throws Exception {
        LangPackV2.WriteCallback callback = new LangPackV2.WriteCallback();
        ByteArrayOutputStream tableOut = new ByteArrayOutputStream();
        CodedOutputStream out = CodedOutputStream.newInstance(tableOut);

        TableData.Index.Builder indexBuilder = TableData.Index.newBuilder();
        indexBuilder.setDist(dist);
        indexBuilder.setSect(sect);

        int cursor = 0;

        while (cursor < dataB.getItemCount()){
            writeNext10(cursor, out, callback);

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


    private void writeNext10(int from, CodedOutputStream out, LangPackV2.WriteCallback c) throws IOException {
        c.startOff = out.getTotalBytesWritten();

        int indexId = dataB.getItem(from).getId() / 10;
        int count = 0;

        for (int i = from; i < from + 10 && i < dataB.getItemCount(); i++){
            TableData.ClotheUItem item = dataB.getItem(i);
            if (item.getId() / 10 != indexId) break;

            out.writeMessage(1, item);
            count += 1;
        }

        c.index    = indexId;
        c.count    = count;

        c.bLen = out.getTotalBytesWritten() - c.startOff;
    }
}
