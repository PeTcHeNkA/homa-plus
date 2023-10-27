import plus.te4net.core.event.EventBus;
import plus.te4net.core.api.IEventHandler;
import plus.te4net.core.event.ObjectBus;

public class TestEvent{
    static EventBus bus = new EventBus();
    static ObjectBus obus = new ObjectBus();

    public static void main(String[] args) {
        bus.addEventType(ETest.class);
        bus.addEventHandler(new IEventHandler<ETest>() {
            @Override
            public boolean handle(ETest event) {
                System.out.println(event.val+":"+obus.remStr("test"));

                return false;
            }
        });

        obus.put("test", "val2");

        bus.throwEvent(new ETest("val"));


    }


    static class ETest {
        public final String val;

        ETest(String val) {
            this.val = val;
        }
    }
}
