package plus.asyutl.shoulder;


public class TaskShoulder extends TaskConsumer implements ITaskConsumer{
    private final Thread thread;
    private final long cd;

    public TaskShoulder(){
        this(50);
    }

    public TaskShoulder(int cd) {
        this.cd = cd;
        thread = new Thread(this::tick, "as-shoulder");
        thread.setDaemon(true);
        thread.start();
    }


    @Override
    protected void tick(){
        while (true){
            try {
                long delta = System.currentTimeMillis();
                super.tick();
                delta = System.currentTimeMillis() - delta;
                if(delta<cd) {
                    Thread.sleep(cd-delta);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}