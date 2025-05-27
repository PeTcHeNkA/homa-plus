package plus.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.UnknownFieldSet;
import plus.proto.lang.TableData;
import plus.tson.TsonFile;
import java.io.File;
import java.util.List;
import java.util.Map;
import static plus.proto.LangPackV2.copy;
import static plus.proto.LangPackV2.readInt;


public class UPack {
    public TableData.UnknownGroup uData;

    public UPack(String name) throws Exception {
        this(new File(name));
    }


    public UPack(File file) throws Exception {
        byte[] data = TsonFile.readFile(file);
        int len = readInt(data);

        byte[] chunk2 = copy(data, len + 4, data.length - (len + 4));
        uData = TableData.UnknownGroup.parseFrom(chunk2);
    }


    public void tryFindStrIDS(IntConsumer consumer) {
        int count = uData.getDataCount();
        for (int i = 0; i < count; i++) {
            checkAll(uData.getData(i).getUnknownFields(), consumer);
        }
    }


    private void checkAll(UnknownFieldSet fields, IntConsumer consumer){
        for (Map.Entry<Integer, UnknownFieldSet.Field> e : fields.asMap().entrySet()) {
            UnknownFieldSet.Field value = e.getValue();
            List<ByteString> dList = value.getLengthDelimitedList();

            if (dList.size() != 1) continue;
            ByteString bs = dList.get(0);
            if (bs.size() != 4) continue;
            UnknownFieldSet set;
            try {
                set = UnknownFieldSet.parseFrom(bs);
            } catch (Exception err){
                continue;
            }
            if(bs.size() == 4) {
                List<Long> lst = set.getField(1).getVarintList();
                if (lst.size() == 1) {
                    try {
                        consumer.apply(lst.get(0).intValue());
                    } catch (Exception err) {
                        continue;
                    }
                }
            } else {
//                try{
//                    checkAll(set, consumer);
//                } catch (Exception err){
//                }
            }
        }
    }


    //private static class


    @Override
    public String toString() {
        return uData.toString();
    }


    public interface IntConsumer{
        void apply(int id);
    }
}