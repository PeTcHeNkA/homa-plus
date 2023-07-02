package plus.te4net;

import plus.te4net.core.node.HServer;


public class Main {
    public static void main(String[] args) {
        Args.insert(args);
        new HServer(Args.ARGS);
    }
}