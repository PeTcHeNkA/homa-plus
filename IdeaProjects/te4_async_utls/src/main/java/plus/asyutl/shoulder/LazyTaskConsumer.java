package plus.asyutl.shoulder;


public class LazyTaskConsumer extends TaskConsumer{
    private byte decr = 10;
    private byte curDecr = 0;


    public void setDecr(byte d){
        decr = d;
    }


    @Override
    protected void tick(){
        if(enabled) {
            if (++curDecr >= decr) {
                enabled = false;
                for (IBasicTask task : tasks) {
                    enabled = true;
                    if (task.needRun()) {
                        task.run();
                        tasks.remove(task);
                    }
                }
                curDecr = -1;
            }
        }
    }
}