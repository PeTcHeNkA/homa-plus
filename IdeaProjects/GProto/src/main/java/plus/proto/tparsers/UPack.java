package plus.proto.tparsers;

import com.google.protobuf.ByteString;
import com.google.protobuf.UnknownFieldSet;
import plus.proto.lang.TableData;
import java.util.List;
import java.util.Map;


public class UPack implements IntFuncExecutor {
    public TableData.UnknownGroup uData;

    @Override
    public void onReadFrom(byte[] data) throws Exception {
        uData = TableData.UnknownGroup.parseFrom(data);
    }


    @Override
    public void apply(IntConsumer consumer) {
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
            try {
                TableData.TRef ref = TableData.TRef.parseFrom(bs);
                if(ref.hasId())
                    consumer.apply(ref.getId());
                checkAll(ref.getUnknownFields(), consumer);
            } catch (Exception err){
                continue;
            }
        }
    }


    @Override
    public String toString() {
        return uData.toString();
    }
}