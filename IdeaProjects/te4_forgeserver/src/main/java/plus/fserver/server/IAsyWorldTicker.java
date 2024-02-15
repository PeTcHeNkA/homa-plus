package plus.fserver.server;


public interface IAsyWorldTicker {
    void stop();
    boolean isEnabled();
    void markNeedSave();
    void tick() throws Throwable;
    default void runAsync(Runnable r){throw new UnsupportedOperationException();};
}