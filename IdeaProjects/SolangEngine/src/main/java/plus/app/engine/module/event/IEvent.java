package plus.app.engine.module.event;

public interface IEvent {
    enum Type{
        MSG, OTHER
    }
    Type getType();
}
