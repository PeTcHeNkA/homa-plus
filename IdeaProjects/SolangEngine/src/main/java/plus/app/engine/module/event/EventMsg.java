package plus.app.engine.module.event;


public class EventMsg implements IEvent{
    public final String[] args;

    public EventMsg(String[] args) {
        this.args = args;
    }

    @Override
    public Type getType() {
        return Type.MSG;
    }
}
