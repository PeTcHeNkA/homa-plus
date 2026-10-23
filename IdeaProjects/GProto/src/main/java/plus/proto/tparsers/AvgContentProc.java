package plus.proto.tparsers;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.proto.lang.TableData;


public class AvgContentProc implements IntFuncExecutor{
    public static final boolean SEARCH_LORE_USEGES = true;

    private TableData.AvgContentGroup data;
    private static Int2ObjectMap<TableData.AvgContent> index0;

    @Override
    public void onReadFrom(byte[] data) throws Exception {
        this.data = TableData.AvgContentGroup.parseFrom(data);

        if(SEARCH_LORE_USEGES == false)return;
        Int2ObjectMap<TableData.AvgContent> index = new Int2ObjectOpenHashMap<>();
        index0 = index;

        for (TableData.AvgContent line: this.data.getDataList()){
            index.put(line.getId(), line);
        }
    }


    public static TableData.AvgContent findById(int id){
        if(index0 == null)return null;
        return index0.get(id);
    }


    @Override
    public void apply(IntConsumer consumer) {
        TableData.AvgContentGroup data0 = this.data;
        int count = data0.getDataCount();

        for (int i = 0; i < count; i++){
            TableData.AvgContent data = data0.getData(i);

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