package plus.proto.tparsers;

import plus.proto.lang.TableData;


public class AvgContentProc implements IntFuncExecutor{
    private TableData.AvgContentGroup data;
    @Override
    public void onReadFrom(byte[] data) throws Exception {
        this.data = TableData.AvgContentGroup.parseFrom(data);
    }

    @Override
    public void apply(IntConsumer consumer) {
        int count = data.getDataCount();

        for (int i = 0; i < count; i++){
            TableData.AvgContent data = this.data.getData(i);

            int curId = data.getId();

            String group;
            if(curId > 100000000){
                int clazz0 = curId / 1000;
                int group0 = clazz0 / 100000;

                if(group0 == 1){
                    group = "lore/main";
                } else {
                    group = "lore/event_"+group0;
                }
                group = group + "/c_" + (clazz0 % 100000);
            } else {
                group = "lore/names";
            }

            consumer.setIndex(curId);
            consumer.apply(group+"_m", (int)data.getData1().getId());
            consumer.setIndex(curId);
            consumer.apply(group+"_f", (int)data.getData2().getId());
        }
    }
}