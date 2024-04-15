package plus.fserver.server;

import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class AsyPoolTickerExecutor extends ScheduledThreadPoolExecutor {
    private final AsyWorldTickerV2 tickerV2;

    public AsyPoolTickerExecutor(int corePoolSize, @NotNull AsyWorldTickerV2 ticker) {
        super(corePoolSize, ticker);
        this.tickerV2 = ticker;
    }


    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if(t != null) tickerV2.onError(null, t);
    }
}