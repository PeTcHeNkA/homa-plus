package plus.proto.tparsers;

import plus.proto.lang.TableData;


public class VideoSubtitleProc implements IntFuncExecutor{
    private TableData.VideoSubtitleGroup data;
    @Override
    public void onReadFrom(byte[] data) throws Exception {
        this.data = TableData.VideoSubtitleGroup.parseFrom(data);
    }


    @Override
    public void apply(IntConsumer consumer) {
        int count = data.getDataCount();
        for (int i = 0; i < count; i++){
            TableData.VideoSubtitle data = this.data.getData(i);
            consumer.setIndex(data.getId());
            consumer.apply(consumer.gefGroup(), data.getId());
        }
    }
}