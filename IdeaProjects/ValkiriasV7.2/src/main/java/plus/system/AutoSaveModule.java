package plus.system;

import plus.Main;


public class AutoSaveModule {

    public AutoSaveModule(final long delay){
        final long finalDelay = delay*60000;
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(finalDelay);
                } catch (InterruptedException ignored) {
                }
                Main.floader.saveallusers();
            }
        });
        t.setDaemon(true);
        t.start();
    }
}