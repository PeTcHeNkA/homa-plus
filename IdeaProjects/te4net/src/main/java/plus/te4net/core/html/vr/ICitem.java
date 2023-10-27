package plus.te4net.core.html.vr;


public interface ICitem {
    void toString(StringBuilder b);

    default void toString(StringBuilder b, IToStrAgent agent){
        toString(b, null, agent);
    }

    default void toString(StringBuilder b, ICitem parent, IToStrAgent agent){
        if(agent.onAdd(b, parent, this)){
            toString(b);
        }
    }
}