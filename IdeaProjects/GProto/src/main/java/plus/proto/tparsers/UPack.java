package plus.proto.tparsers;

import com.google.protobuf.ByteString;
import com.google.protobuf.UnknownFieldSet;
import plus.proto.lang.TableData;
import java.util.List;
import java.util.Map;


public class UPack implements IntFuncExecutor {
    public TableData.UnknownGroup uData;
    private final String curClazz;

    public UPack(String curClazz) {
        if(curClazz == null)curClazz = "default";
        this.curClazz = curClazz;
    }

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

            //todo temp
            if(AvgContentProc.SEARCH_LORE_USEGES){
                List<Long> lst = value.getVarintList();
                if(!lst.isEmpty()){
                    for (Long l: lst){
                        if(l > 100000000) {
                            TableData.AvgContent ln = AvgContentProc.findById(l.intValue());
                            if (ln != null) {
                                System.out.println("ID: " + l + " founded in " + curClazz);
                            }
                        }
                    }
                }
            }
            //todo end temp

            if (dList.size() != 1) continue;
            ByteString bs = dList.get(0);
            try {
                try {
                    TableData.TRef ref = TableData.TRef.parseFrom(bs);
                    if(ref.hasId())
                        consumer.apply(consumer.gefGroup(), (int)ref.getId());

                    checkAll(ref.getUnknownFields(), consumer);
                } catch (Exception err0){

                    TableData.Unknown ref = TableData.Unknown.parseFrom(bs);
                    checkAll(ref.getUnknownFields(), consumer);
                }
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