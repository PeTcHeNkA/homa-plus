import plus.te4net.core.Tehnet;
import plus.te4net.core.node.TehnetSimple;


public class TestTehnet {
    public static void main(String[] args) {
        new Tehnet().setDefault(session -> {

            session.send("""
                    Hello World!
                    """);
            TehnetSimple.getLogger().info(session);

        }).useDomain("example.ru").start();
    }
}