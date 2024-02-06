package test1;

import plus.te4net.core.Tehnet;


public class TestTehnet {
    public static void main(String[] args) {
        new Tehnet().setDefault(session -> {

            session.send("""
                    Hello World!
                    """);
            Tehnet.getLogger().info(session);

        }).useDomain("example.ru").start();
    }
}