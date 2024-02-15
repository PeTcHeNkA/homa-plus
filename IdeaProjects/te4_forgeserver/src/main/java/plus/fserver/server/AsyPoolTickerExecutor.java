package plus.fserver.server;

import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;


public class AsyPoolTickerExecutor extends ScheduledThreadPoolExecutor {
    public AsyPoolTickerExecutor(int corePoolSize, @NotNull ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }


    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if(t != null){
            t.printStackTrace();
        }
    }
}