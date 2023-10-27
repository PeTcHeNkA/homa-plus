package plus.te4net.core.html.vr;


@FunctionalInterface
public interface IToStrAgent {
    boolean onAdd(StringBuilder b, ICitem parent, ICitem item);
}