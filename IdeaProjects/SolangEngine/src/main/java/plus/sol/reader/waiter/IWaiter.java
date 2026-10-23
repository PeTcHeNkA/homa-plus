package plus.sol.reader.waiter;


public interface IWaiter {
    void reset();
    boolean wait(char c);
    boolean isActive();
    String getValue();
}
