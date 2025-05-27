package plus.proto.tparsers;

import plus.proto.lang.TableData;


public class ItemGetListProc implements IntFuncExecutor{
    private TableData.ItemGetListGroup group;

    @Override
    public void onReadFrom(byte[] data) throws Exception {
        group = TableData.ItemGetListGroup.parseFrom(data);
    }

    @Override
    public void apply(IntConsumer consumer) {
        int count = group.getDataCount();
        for (int i = 0; i < count; i++){
            TableData.ItemGetList data = group.getData(i);
            if(data.hasTitle())
                consumer.apply(data.getTitle().getId());
        }
    }
}